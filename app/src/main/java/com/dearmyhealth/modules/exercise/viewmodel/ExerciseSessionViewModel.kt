package com.dearmyhealth.modules.exercise.viewmodel

import android.app.Application
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dearmyhealth.MyApplication
import com.dearmyhealth.modules.Diet.model.DietStats
import com.dearmyhealth.modules.exercise.model.ExerciseSessionData
import com.dearmyhealth.modules.healthconnect.HealthConnectAvailability
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class ExerciseSessionViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    val TAG = javaClass.simpleName

    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    val availability: LiveData<HealthConnectAvailability> = healthConnectManager.availability

    var sessionsList: MutableLiveData<List<ExerciseSessionRecord>> = MutableLiveData(listOf())
        private set

    var sessionDataList: MutableLiveData<List<ExerciseSessionData>> = MutableLiveData(listOf())
        private set

    private var _ranges: LiveData<List<DietStats.PERIOD>> = MutableLiveData(listOf(
        DietStats.PERIOD.WEEK,
        DietStats.PERIOD.MONTH,
        DietStats.PERIOD.ThreeMONTH,
        DietStats.PERIOD.YEAR
    ))
    val ranges: LiveData<List<DietStats.PERIOD>> get() = _ranges

    var currentPeriodText: MutableLiveData<String> = MutableLiveData("")
    var currentPosition: Int = 0
        set(value) {
            field = value
            currentRange = _ranges.value!![value]
        }

    var currentRange = ranges.value!![currentPosition]
        set(value) {
            field = value
            val today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
            startOfRange = when(currentRange) {
                DietStats.PERIOD.WEEK-> today.minusDays(6)
                DietStats.PERIOD.MONTH -> today.withDayOfMonth(1)
                DietStats.PERIOD.ThreeMONTH -> today.withDayOfMonth(1).minusMonths(2)
                DietStats.PERIOD.YEAR -> today.withDayOfMonth(1).minusYears(1).plusMonths(1)
            }
        }
    var startOfRange = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        set(value) {
            field = value

            endOfRange = when(currentRange) {
                DietStats.PERIOD.WEEK -> value.plusDays(7).minusNanos(1)
                DietStats.PERIOD.MONTH -> value.plusMonths(1).minusNanos(1)
                DietStats.PERIOD.ThreeMONTH -> value.plusMonths(3).minusNanos(1)
                DietStats.PERIOD.YEAR -> value.plusYears(1).minusNanos(1)
            }
        }
    var endOfRange = startOfRange
        .withHour(23)
        .withMinute(59)
        .withSecond(59)
        private set

    suspend fun checkPermission(): Boolean {
        return healthConnectManager.hasAllPermissions(permissions)
    }


    fun insertExerciseSession() {
        viewModelScope.launch {
            healthConnectManager.tryWithPermissionsCheck(permissions) {
                val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
                val offset = Random.nextDouble()

                // Generate random start time between the start of the day and (now - 30mins).
                val startOfSession = startOfDay.plusSeconds(
                    (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
                )
                val endOfSession = startOfSession.plusMinutes(30)

                healthConnectManager.writeExerciseSession(startOfSession, endOfSession)
                readExerciseSessions()
            }
        }
    }

    fun readExerciseSessions() {
        CoroutineScope(Dispatchers.IO).launch {

            healthConnectManager.tryWithPermissionsCheck(permissions) {
                var mylist =
                    healthConnectManager.readExerciseSessions(startOfRange.toInstant(), endOfRange.toInstant())
                withContext(Dispatchers.Main) {
                    sessionsList.value = mylist
                }
            }

            var dataList = mutableListOf<ExerciseSessionData>()
            for(record in sessionsList.value!!) {
                dataList.add(readAssociatedSessionData(record.metadata.id))
            }
            withContext(Dispatchers.Main) {
                sessionDataList.value = dataList
            }
        }
    }


    /**
     * Reads aggregated data and raw data for selected data types, for a given [ExerciseSessionRecord].
     */
    suspend fun readAssociatedSessionData(
        uid: String
    ): ExerciseSessionData {
        val exerciseSession = healthConnectManager.readRecord<ExerciseSessionRecord>(uid)
        // Use the start time and end time from the session, for reading raw and aggregate data.
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = exerciseSession.record.startTime,
            endTime = exerciseSession.record.endTime
        )
        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
            StepsRecord.COUNT_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HeartRateRecord.BPM_AVG,
            HeartRateRecord.BPM_MAX,
            HeartRateRecord.BPM_MIN,
        )
        // Limit the data read to just the application that wrote the session. This may or may not
        // be desirable depending on the use case: In some cases, it may be useful to combine with
        // data written by other apps.
        val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )
        val aggregateData = healthConnectManager.aggregate(aggregateRequest)
        val heartRateData = healthConnectManager.readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)

        return ExerciseSessionData(
            uid = uid,
            startTime = exerciseSession.record.startTime,
            endTime = exerciseSession.record.endTime,
            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
            totalSteps = aggregateData[StepsRecord.COUNT_TOTAL],
            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
            minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
            avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG],
            heartRateSeries = heartRateData,
        )
    }


    class Factory(private val application: Application): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(ExerciseSessionViewModel::class.java)) {

                return ExerciseSessionViewModel(
                    MyApplication.INSTANCE.healthConnectManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}