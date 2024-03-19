package com.dearmyhealth.modules.Diet.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.modules.Diet.DietRepository
import com.dearmyhealth.modules.Diet.model.Nutrients
import java.util.Calendar

class DietTodayViewModel(lifecycleOwner: LifecycleOwner, dietRepository: DietRepository): ViewModel() {
    private val TAG = javaClass.simpleName
    val todayDiets: LiveData<List<Diet>> =
        dietRepository.findByPeriodLive(getTodayMsec())

    private val _todayNutritions = MutableLiveData<Nutrients>()
    val todayNutritions: LiveData<Nutrients> get() = _todayNutritions

    fun calcNutrients()  {
        val value = todayDiets.value!!
        Log.d(TAG, "diet size is : ${value.size}")
        var nuts: Nutrients = Nutrients(0, getTodayMsec(),0.0, mutableMapOf())
        for(diet in value) {
            nuts.calories = nuts.calories!! + (diet.calories ?: 0.0)
            nuts.nutrients.putAll( mutableMapOf(
                Nutrients.Names.carbohydrate to
                        (nuts.getNutrientValueByName("carbohydrate")?:0.0) + (diet.carbohydrate ?: 0.0),
                Nutrients.Names.protein to
                        (nuts.getNutrientValueByName("protein")?:0.0) + (diet.protein ?: 0.0),
                Nutrients.Names.fat to
                        (nuts.getNutrientValueByName("fat")?:0.0) + (diet.fat ?: 0.0)
            ))
        }
        _todayNutritions.value = nuts
    }

    fun getTodayMsec(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }


    class Factory(val application: Application, val lifecycleOwner: LifecycleOwner) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietTodayViewModel::class.java)) {
                val dietDao = AppDatabase.getDatabase(application).dietDao()
                return DietTodayViewModel(lifecycleOwner,
                    dietRepository = DietRepository(
                        datasource = dietDao
                    )
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}