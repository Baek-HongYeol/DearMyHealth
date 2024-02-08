package com.dearmyhealth.modules.Diet

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietViewModel(val foodRepository: FoodRepository) : ViewModel() {
    var count: Int =0
    val target = MutableLiveData<Food>()
    var list: MutableLiveData<List<Food>>  = MutableLiveData()
    val searchResult: MutableLiveData<List<Food>> = MutableLiveData()
    val TAG: String = "DietViewModel"

    suspend fun searchFood(name: String): List<Food> {
        Log.d(TAG, "query: $name")
        return if (name.isNotBlank())
            foodRepository.search(name)
        else
            foodRepository.getAll()
    }

    fun listFood() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = foodRepository.getAll()
            withContext(Dispatchers.Main) {
                list.value = result
            }
        }
    }

    class Factory(val context: Context): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, ): T {
            if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
                val foodDao = AppDatabase.getDatabase(context).foodDao()
                return DietViewModel(
                    foodRepository = FoodRepository(
                        dataSource = foodDao
                    )
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}