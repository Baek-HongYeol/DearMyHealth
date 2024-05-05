package com.dearmyhealth.modules.Diet.viewmodel

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.modules.Diet.DietRepository
import com.dearmyhealth.modules.Diet.model.DietStats
import com.dearmyhealth.modules.Diet.model.DietStats.PERIOD.*
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class DietStatsViewModel(val dietRepository: DietRepository) : ViewModel() {
    private val TAG = "DietStatsVM"

    private var _ranges: LiveData<List<DietStats.PERIOD>> = MutableLiveData(listOf(WEEK, MONTH, ThreeMONTH, YEAR))
    val ranges: LiveData<List<DietStats.PERIOD>> get() = _ranges

    var currentPeriodText: MutableLiveData<String> = MutableLiveData("")
    var currentPosition: Int = 0
        set(value) {
            field = value
            endDateOfRange = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        }

    var endDateOfRange = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        private set(value) {
            field = value
            val startOfDay = value.truncatedTo(ChronoUnit.DAYS)
            endOfDay = startOfDay.plusDays(1)
            startOfRange = when(currentPosition) {
                0 -> startOfDay.minusDays(6)
                1 -> startOfDay.withDayOfMonth(1)
                2 -> startOfDay.withDayOfMonth(1).minusMonths(2)
                3 -> startOfDay.withDayOfMonth(1).minusYears(1).plusMonths(1)
                else -> startOfDay.withDayOfMonth(1).minusYears(1).plusMonths(1)
            }
        }

    var endOfDay = endDateOfRange.truncatedTo(ChronoUnit.DAYS).plusDays(1)
        private set

    var startOfRange = endDateOfRange.truncatedTo(ChronoUnit.DAYS)
        private set

    private var _data: MutableLiveData<List<Diet>> = MutableLiveData(mutableListOf())
    val data:LiveData<List<Diet>> get() = _data

    fun selectRange(position: Int) {
        if(_ranges.value!!.getOrNull(position) == null) return
        currentPosition = position

        val dtf = when (position) {
            0 -> DateTimeFormatter.ofPattern("dd일", Locale.getDefault())
            1 -> DateTimeFormatter.ofPattern("M월 dd일", Locale.getDefault())
            2 -> DateTimeFormatter.ofPattern("M월 dd일", Locale.getDefault())
            else -> DateTimeFormatter.ofPattern("yyyy년 MM월", Locale.getDefault())
        }

        currentPeriodText.value = "${dtf.format(startOfRange)} ~ ${dtf.format(endDateOfRange)}"
    }

    suspend fun setRangedData(start: OffsetDateTime, end: OffsetDateTime) {
        val data = dietRepository.findByPeriod(start.toEpochSecond()*1000, end.toEpochSecond()*1000)
        Log.d(TAG, "data size: ${data.size}")

        withContext(Dispatchers.Main) {
            _data.value = data
        }
    }



    class Factory(val context: Context): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietStatsViewModel::class.java)) {
                val dietDao = AppDatabase.getDatabase(context).dietDao()
                return DietStatsViewModel(
                    dietRepository = DietRepository(datasource = dietDao)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}