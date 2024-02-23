package com.dearmyhealth.modules.Diet

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.data.db.entities.Food
import java.util.Calendar

class DietViewModel(val lifecycleOwner:LifecycleOwner, val foodRepository: FoodRepository, val dietRepository: DietRepository) : ViewModel() {
    val TAG: String = "DietViewModel"
    private val _todayNutritions = MutableLiveData<Nutrients>()
    val todayNutritions: LiveData<Nutrients> get() = _todayNutritions

    private val todayDiets: LiveData<List<Diet>> =
        dietRepository.findByPeriodLive(getTodayMsec()).distinctUntilChanged()

    suspend fun searchFood(name: String): List<Food> {
        Log.d(TAG, "query: $name")
        return if (name.isNotBlank())
            foodRepository.search(name)
        else
            foodRepository.getAll()
    }
    fun observeTodayDiet() {
        todayDiets.observe(lifecycleOwner) { value ->
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
    }

    fun setTodayDietChart() {

    }

    fun getTodayMsec(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }

    fun getCalendarFromMsec(msec: Long) : Calendar {
        var cal = Calendar.getInstance()
        cal.timeInMillis = msec
        return cal
    }


    class Factory(val context: Context, private val lifecycleOwner: LifecycleOwner): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
                val foodDao = AppDatabase.getDatabase(context).foodDao()
                val dietDao = AppDatabase.getDatabase(context).dietDao()
                return DietViewModel(
                    lifecycleOwner,
                    foodRepository = FoodRepository(
                        dataSource = foodDao
                    ),
                    dietRepository = DietRepository(
                        datasource = dietDao
                    )
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}