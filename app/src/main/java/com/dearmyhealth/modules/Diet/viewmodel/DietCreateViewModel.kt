package com.dearmyhealth.modules.Diet.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.data.db.entities.Food
import com.dearmyhealth.modules.Diet.DietRepository
import com.dearmyhealth.modules.Diet.FoodRepository
import com.dearmyhealth.modules.Diet.model.DietCreateResult
import com.dearmyhealth.modules.Diet.model.FoodSearchResult
import com.dearmyhealth.modules.Diet.model.Nutrients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

class DietCreateViewModel(
    private val foodRepository: FoodRepository,
    private val dietRepository: DietRepository
): ViewModel() {
    private val TAG = javaClass.simpleName


    val foodSearchResult: MutableLiveData<FoodSearchResult> = MutableLiveData(FoodSearchResult())

    private var isNameFromFood = false
    val targetName: MutableLiveData<String?> = MutableLiveData()
    val targetFood: MutableLiveData<Food?> = MutableLiveData(null)
    val targetNutrition: MutableLiveData<Nutrients?> = MutableLiveData(null)
    val targetType: MutableLiveData<Diet.MealType> = MutableLiveData(Diet.MealType.MEAL_TYPE_BREAKFAST)
    val targetDateTime: MutableLiveData<Calendar> = MutableLiveData(Calendar.getInstance(
        TimeZone.getTimeZone(ZoneId.systemDefault())
    ).also {
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    })


    private val _dietResult = MutableLiveData<DietCreateResult>()
    val dietResult: LiveData<DietCreateResult> = _dietResult

    /** 이름으로 음식 검색
     *
     * code 오름차순으로 기본 20개 검색을 한다.
     * if name이 blank라면 전체 데이터 중 code가 가장 앞에 있는 것부터 20개를 반환.
     *
     * success: FoodSearchResult(success)로 결과를 전달한다.
     * error: FoodSearchResult(error) 로 오류 내용을 전달한다.
     */
    suspend fun searchFood(name: String) {
        Log.d(TAG, "query: $name")
        lateinit var result: List<Food>
        try {
            result = if (name.isNotBlank())
                foodRepository.search(name)
            else
                foodRepository.getAll()
        }
        catch (e: Exception) {
            withContext(Dispatchers.Main) {
                foodSearchResult.value = FoodSearchResult(error = e)
            }
            return
        }
        withContext(Dispatchers.Main) {
            foodSearchResult.value = FoodSearchResult(success = result)
        }
    }

    /** 식단 이름 설정
     *  사용자가 입력한 이름을 식단의 이름으로 설정한다. */
    fun setName(name: String) {
        targetName.value = name
        isNameFromFood = false
    }

    /** 음식 설정
     *  FoodSearchResult에 존재하는 food를 식단의 음식으로 설정한다.
     *  음식과 영양 정보를 설정하고
     *  사용자가 식단에 이름을 지정하지 않았다면 식품명으로 이름을 지정한다.
     */
    fun setFood(pos: Int) {
        val food = foodSearchResult.value?.success?.get(pos)
        if(food == null) {
            Log.d(TAG, "cannot set food because there is no search data.")
            return
        }
        targetFood.value = food
        val nuts = food.toNutrients()
        targetNutrition.value = nuts
        if(targetName.value == null || isNameFromFood) {
            targetName.value = food.식품명
            isNameFromFood = true
        }
    }

    /** 음식 지정 취소
     * 현재 지정된 음식의 영양 성분을 모두 삭제하고 기본값으로 되돌린다.
     * 만일 식단 이름이 음식으로 지정된 것이라면 초기화한다.*/
    fun removeFood() {
        targetFood.value = null
        targetNutrition.value = null
        if (isNameFromFood){
            targetName.value = null
            isNameFromFood = false
        }
    }

    /** 식단 유형 설정
     * MealType enum class에 존재하는 유형의 index를 전달받아 해당 유형으로 설정한다.
     */
    fun setType(index: Int) {
        targetType.value = Diet.MealType.values()[index]
    }

    /** 식단 취급 시간 설정
     * TimePicker를 통해 지정한 시간, 분을 targetDateTime에 설정한다.
     */
    fun setTime(hour: Int, minute: Int) {
        targetDateTime.value!!.set(Calendar.HOUR_OF_DAY, hour)
        targetDateTime.value!!.set(Calendar.MINUTE, minute)
    }

    /** 식단 취급 날짜 설정
     * DatePicker를 통해 지정한 날짜를 targetDateTime에 설정한다.
     */
    fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        targetDateTime.value!!.set(Calendar.YEAR, year)
        targetDateTime.value!!.set(Calendar.MONTH, month)
        targetDateTime.value!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }

    /** 설정된 데이터를 repository를 통해 저장한다.
     * 칼로리, 탄수화물, 단백질, 지방, 콜레스테롤은 nullable하게 접근해 저장.
     */
    fun save() {
        dietRepository.insert(
            targetDateTime.value!!.timeInMillis, targetFood.value?.code, targetType.value!!,
            targetName.value ?: "null", null, targetNutrition.value?.calories,
            targetNutrition.value?.nutrients?.get(Nutrients.Names.carbohydrate),
            targetNutrition.value?.nutrients?.get(Nutrients.Names.protein),
            targetNutrition.value?.nutrients?.get(Nutrients.Names.fat),
            targetNutrition.value?.nutrients?.get(Nutrients.Names.cholesterol),
        )
    }


    class Factory(private val application: Application): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietCreateViewModel::class.java)) {
                val foodDao = AppDatabase.getDatabase(application).foodDao()
                val dietDao = AppDatabase.getDatabase(application).dietDao()
                return DietCreateViewModel(
                    FoodRepository(foodDao),
                    DietRepository(dietDao)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}