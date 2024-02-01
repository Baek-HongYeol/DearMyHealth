package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dearmyhealth.data.db.entities.Food

@Dao
interface FoodDao {
    @Query("SELECT * FROM food LIMIT :offset,20")
    suspend fun getAll(offset:Int=0): List<Food>

    @Query("SELECT Count(*) FROM food")
    fun countAll(): Int


    @Query("SELECT * FROM food WHERE 식품명 LIKE :name LIMIT :offset,20")
    suspend fun findByName(name: String, offset:Int=0): List<Food>

    @Query("SELECT * FROM food WHERE code=:code")
    suspend fun getFood(code: String): Food

    @Insert
    fun insert(food: Food)

    @Delete
    fun delete(user: Food)
}