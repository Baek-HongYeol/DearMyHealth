package com.dearmyhealth.home

import android.content.Context
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
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class HomeViewModel(val lifecycleOwner: LifecycleOwner, val dietRepository: DietRepository, ) : ViewModel() {
    val TAG = javaClass.simpleName


    private val _todayNutrients = MutableLiveData<Nutrients>()
    val todayNutrients: LiveData<Nutrients> get() = _todayNutrients

    private val todayDiets: LiveData<List<Diet>> =
        dietRepository.findByPeriodLive(getTodayMSec())

    fun observeTodayDiet() {
        todayDiets.observe(lifecycleOwner) { value ->
            Log.d(TAG, "diet size is : ${value.size}")
            var nuts: Nutrients = Nutrients(0, getTodayMSec(),0.0, mutableMapOf())
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
            _todayNutrients.value = nuts
        }
    }

    private fun getTodayMSec(): Long = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).toEpochSecond()*1000

    class Factory(val context: Context, private val lifecycleOwner: LifecycleOwner): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                val dietDao = AppDatabase.getDatabase(context).dietDao()
                return HomeViewModel(
                    lifecycleOwner,
                    dietRepository = DietRepository(
                        datasource = dietDao
                    )
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}