package com.dearmyhealth.modules.Diet

import android.util.Log
import com.dearmyhealth.data.db.dao.FoodDao
import com.dearmyhealth.data.db.entities.Food

class FoodRepository(val dataSource: FoodDao) {


    suspend fun get(code: String): Food {

        return dataSource.getFood(code)
    }
    suspend fun getAll(): List<Food> {
        return dataSource.getAll()
    }
    suspend fun search(name: String): List<Food> {
        return dataSource.findByName("%$name%")
    }
    fun countAll(): Int {
        val count = dataSource.countAll()
        Log.d("FoodRepository", "count: $count")
        return count
    }
}