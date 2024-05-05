package com.dearmyhealth.modules.bpm

import android.os.RemoteException
import android.util.Log
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.modules.healthconnect.GroupedAggregationResult
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Period
import java.time.temporal.ChronoUnit

class VitalViewModel(val healthConnectManager: HealthConnectManager) : ViewModel() {
    val TAG = javaClass.simpleName

    val client by lazy { healthConnectManager.healthConnectClient }

    val today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
    var currentDate = OffsetDateTime.now()
    var currentRange = PERIOD.DAY
        set(value) {
            field = value
            endDateOfRange = today
        }
    var endDateOfRange = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        set(value) {
            field = value
            val startOfDay = value.truncatedTo(ChronoUnit.DAYS)
            endOfDay = startOfDay.plusDays(1)
            startOfRange = when(currentRange) {
                PERIOD.DAY -> startOfDay
                PERIOD.WEEK -> startOfDay.minusDays(6)
                PERIOD.MONTH -> startOfDay.minusMonths(1).plusDays(1)
                PERIOD.YEAR -> startOfDay.minusYears(1)
            }
        }

    var endOfDay = endDateOfRange.truncatedTo(ChronoUnit.DAYS).plusDays(1)
        private set

    var startOfRange = endDateOfRange.truncatedTo(ChronoUnit.DAYS)
        private set

    var bpmSeries : MutableLiveData<List<HeartRateRecord>> = MutableLiveData(listOf())
        private set
    var stepsIntoSlices : MutableLiveData<List<GroupedAggregationResult>> =
        MutableLiveData(listOf())
        private set

    var weightRecords : MutableLiveData<List<WeightRecord>> = MutableLiveData(listOf())
        private set

    var tempRecords : MutableLiveData<List<BodyTemperatureRecord>> = MutableLiveData(listOf())
        private set

    var permitted: MutableLiveData<Boolean> = MutableLiveData(false)
        private set

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
        )

    /**************  권한 관리  ****************/

    suspend fun checkPermission() {
        val result = healthConnectManager.hasAllPermissions(permissions)
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "checkPermission: $result")
            permitted.value = result
        }
    }

    suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        val isPermitted = healthConnectManager.hasAllPermissions(permissions)
        CoroutineScope(Dispatchers.Main).launch {
            permitted.value = isPermitted
        }

        try {
            if (permitted.value!!) {
                block()
            }
            else {

            }
        } catch (remoteException: RemoteException) {
            Log.e(TAG, remoteException.toString())
        } catch (securityException: SecurityException) {
            Log.e(TAG, securityException.toString())
            securityException.printStackTrace()
        } catch (ioException: IOException) {
            Log.e(TAG, ioException.toString())
            ioException.printStackTrace()
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.toString())
        }
    }

    /**************  데이터 읽기 / 쓰기  **************/

    fun readHeartRateRecordsForDay(date: Instant) {
        // 날짜의 시작 시간과 종료 시간을 계산합니다.
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)

        // 시작 시간부터 종료 시간까지의 데이터를 가져옵니다.
        CoroutineScope(Dispatchers.IO).launch {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
            val response = client.readRecords(request)
            withContext(Dispatchers.Main) {
                bpmSeries.value = response.records
            }
        }
    }

    /** read steps record for a given day. */
    suspend fun readStepsRecordsForDay(date: Instant) : List<StepsRecord>{
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)
        return healthConnectManager.readStepsRecords(startOfDay, endOfDay)
    }

    /**
     * read aggregate result of steps record for a day with given units.
     * @param date the target date for reading records
     * @param timeRangeSlicer timeUnit which steps records are sliced into.
     * @return [List]<[Long]>
     *
     * this function requests the aggregate data for StepsRecord.COUNT_TOTAL
     * and gathering the values from AggregateResultGroupedByDuration into list.
     */
    suspend fun readStepsForDayIntoSlices(date: Instant, timeRangeSlicer: Duration): List<Long?> {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)
        aggregateStepsIntoSlices(startOfDay, endOfDay, timeRangeSlicer).join()
        val result = stepsIntoSlices.value!!.map { v ->
            v.result[StepsRecord.COUNT_TOTAL]
        }
        return if (result.isEmpty() || (result.all{it == null}))
            listOf()
        else
            result
    }


    suspend fun readStepsForDay(date: Instant) : Long?{
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)

        val request = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            TimeRangeFilter.between(startOfDay, endOfDay)
        )
        return client.aggregate(request)[StepsRecord.COUNT_TOTAL]
    }


    /**
     * this function request for aggregate data for [StepsRecord.COUNT_TOTAL] into [Duration] Slices.
     */
    suspend fun aggregateStepsIntoSlices(
        startTime: Instant,
        endTime: Instant,
        timeRangeSlicer: Duration
    ): Job {
        try {
            val result = client.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = timeRangeSlicer
                )
            )
            val list = mutableListOf<GroupedAggregationResult>()
            for(record in result) {
                list.add(
                    GroupedAggregationResult(
                        record.result,
                        record.startTime,
                        record.endTime
                    )
                )
            }
            return CoroutineScope(Dispatchers.Main).launch {
                stepsIntoSlices.value = list
            }
        }
        catch(e: SecurityException) {
            return Job()
        }
    }

    /**
     * this function request for aggregate data for [StepsRecord.COUNT_TOTAL] into [Period] Slices.
     */
    suspend fun aggregateStepsIntoSlices(
        startTime: LocalDateTime = startOfRange.toLocalDateTime(),
        endTime: LocalDateTime,
        timeRangeSlicer: Period
    ): Job {
        try {
            val result = client.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = timeRangeSlicer
                )
            )
            val list = mutableListOf<GroupedAggregationResult>()
            val zoneOffset = OffsetDateTime.now().offset
            for(record in result) {
                list.add(
                    GroupedAggregationResult(
                        record.result,
                        record.startTime.toInstant(zoneOffset),
                        record.endTime.toInstant(zoneOffset)
                    )
                )
            }
            return CoroutineScope(Dispatchers.Main).launch {
                stepsIntoSlices.value = list
            }
        }
        catch(e: SecurityException) {
            return Job()
        }
    }

}

class VitalViewModelFactory(val healthConnectManager: HealthConnectManager): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
        if (modelClass.isAssignableFrom(VitalViewModel::class.java)) {
            return VitalViewModel(
                healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}