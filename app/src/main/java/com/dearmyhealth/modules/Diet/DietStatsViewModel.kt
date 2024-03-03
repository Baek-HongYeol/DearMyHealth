package com.dearmyhealth.modules.Diet

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
import com.dearmyhealth.modules.Diet.model.DietStats
import com.dearmyhealth.modules.Diet.model.DietStats.PERIOD.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DietStatsViewModel(val dietRepository: DietRepository) : ViewModel() {
    private val TAG = "DietStatsVM"

    private var _ranges: LiveData<List<DietStats.PERIOD>> = MutableLiveData(listOf(WEEK, MONTH, ThreeMONTH, YEAR))
    val ranges: LiveData<List<DietStats.PERIOD>> get() = _ranges

    var currentPeriod: MutableLiveData<String> = MutableLiveData("")
    var currentPosition: Int = 0

    private var _data: MutableLiveData<List<Diet>> = MutableLiveData(mutableListOf())
    val data:LiveData<List<Diet>> get() = _data

    suspend fun selectRange(position: Int) {
        if(_ranges.value!!.getOrNull(position) == null) return
        currentPosition = position
        val now = Calendar.getInstance()

        val target = Calendar.getInstance()
        target.add(_ranges.value!![position].unit, _ranges.value!![position].amount * (-1))

        val sdf = SimpleDateFormat("yy년 M월 d일", Locale.KOREA)
        sdf.timeZone = TimeZone.getDefault()
        withContext(Dispatchers.Main) {
            currentPeriod.value = "${sdf.format(target.time)} ~ ${sdf.format(now.time)}"
        }

        setRangedData(target, now)
    }

    private suspend fun setRangedData(start: Calendar, end: Calendar) {
        val data = dietRepository.findByPeriod(start.timeInMillis, end.timeInMillis)
        Log.d(TAG, "data size: ${data.size}")

        withContext(Dispatchers.Main) {
            _data.value = data
        }
    }

    fun changeDateText(): List<String> {
        val dataList = data.value
        val dataTextList = ArrayList<String>()
        val WEEK_FORMATTER = SimpleDateFormat("d", Locale.getDefault())
        val ThreeMONTH_FORMATTER = SimpleDateFormat("M월-W주", Locale.getDefault())
        val YEAR_FORMATTER = SimpleDateFormat("M월", Locale.getDefault())
        for (i in dataList!!.indices) {
            val date = Date(dataList[i].time)
            val formatter: SimpleDateFormat = when(_ranges.value!![currentPosition])
            {
                WEEK -> WEEK_FORMATTER
                MONTH -> WEEK_FORMATTER
                ThreeMONTH -> ThreeMONTH_FORMATTER
                YEAR -> YEAR_FORMATTER
                else -> SimpleDateFormat("M월 d일", Locale.getDefault())
            }
            dataTextList.add(formatter.format(date))

        }
        Log.d(TAG, dataTextList.toString())
        return dataTextList
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