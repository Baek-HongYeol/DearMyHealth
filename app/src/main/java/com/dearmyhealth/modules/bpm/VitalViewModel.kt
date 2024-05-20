package com.dearmyhealth.modules.bpm

import android.content.Context
import android.content.SharedPreferences
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
import com.dearmyhealth.R
import com.dearmyhealth.modules.healthconnect.GroupedAggregationResult
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class VitalViewModel(val healthConnectManager: HealthConnectManager) : ViewModel() {
    val TAG = javaClass.simpleName

    val client by lazy { healthConnectManager.healthConnectClient }

    val today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
    var currentDate = OffsetDateTime.now()
    var currentRange = PERIOD.DAY
        set(value) {
            field = value
            startOfRange = when(currentRange) {
                PERIOD.DAY -> today
                PERIOD.WEEK -> today.minusDays(7)
                PERIOD.MONTH -> today.withDayOfMonth(1)
                PERIOD.YEAR -> today.withDayOfMonth(1).minusYears(1).plusMonths(1)
            }
        }
    var startOfRange = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        set(value) {
            field = value

            endOfRange = when(currentRange) {
                PERIOD.DAY -> value.plusDays(1).minusNanos(1)
                PERIOD.WEEK -> value.plusDays(7).minusNanos(1)
                PERIOD.MONTH -> value.plusMonths(1).minusNanos(1)
                PERIOD.YEAR -> value.plusYears(1).minusNanos(1)
            }
        }
    var endOfRange = startOfRange
        .withHour(23)
        .withMinute(59)
        .withSecond(59)
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
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getWritePermission(BodyTemperatureRecord::class)
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

    /*** Weight ***/
    suspend fun readWeightRecords(startTime: Instant, endTime: Instant) {
        try {
            val response = client.readRecords<WeightRecord>(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    //dataOriginFilter = setOf(DataOrigin("com.dearmyhealth"))
                )
            )
            withContext(Dispatchers.Main) {
                weightRecords.value = response.records
            }
        }
        catch (e: Exception) {
            // Run error handling here
        }
    }

    suspend fun readTempRecords(startTime: Instant, endTime: Instant) {
        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = BodyTemperatureRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
            withContext(Dispatchers.Main) {
                tempRecords.value = response.records
            }
        }
        catch (e: Exception) {
            // Run error handling here
        }
    }


    suspend fun preloadSteps(context: Context) {
        if(!healthConnectManager.hasAllPermissions(setOf(
                HealthPermission.getWritePermission(StepsRecord::class)
            )))
        {
            Log.e(TAG, "preload steps count data failed caused by permission denied.")
            return
        }

        // Preference에 저장한 값 조회
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("preload steps", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("already_preload", false))
            return
        val preloadCount = sharedPreferences.getInt("step_preload_count", 0)
        var writtenCount = preloadCount

        try {
            val stepsList: JSONArray =
                context.resources.openRawResource(R.raw.preload_steps).bufferedReader().use {
                    JSONArray(it.readText())
                }
            val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val offset = OffsetDateTime.now().offset

            Log.d("Preload_Step", "list size: ${stepsList.length()}")
            Log.d("Preload_Step", stepsList.getJSONObject(0).getString("start_time"))
            stepsList.takeIf { it.length() > 0 }?.let { list ->

                for (index in preloadCount until list.length()) {
                    val stepObj = list.getJSONObject(index)
                    val count = stepObj.getInt("count")
                    if(count==0)
                        continue
                    healthConnectManager.writeStepsRecord(
                        stepObj.getInt("count").toLong(),
                        LocalDateTime.parse(stepObj.getString("start_time"), dtf).toInstant(offset),
                        LocalDateTime.parse(stepObj.getString("end_time"), dtf).toInstant(offset)
                    )
                    writtenCount++
                }
                Log.e("Preload_Step", "successfully pre-load steps into database")
                //  Preference에 로드한 값 저장
                val editor = sharedPreferences.edit()
                editor.putBoolean("already_preload", true)
                editor.apply()
            }
        } catch (exception: Exception) {
            Log.e(
                "Preload_Step",
                exception.localizedMessage ?: "failed to pre-load steps into database"
            )
            val editor = sharedPreferences.edit()
            editor.putInt("step_preload_count", writtenCount)
            editor.apply()
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