package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.Diet

@Dao
interface DietDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<Diet>

    @Query("SELECT * FROM user WHERE uid IN (:dietIds)")
    fun loadAllByUids(dietIds: IntArray): List<Diet>

    @Query("SELECT * FROM diet WHERE dietId LIKE :id LIMIT 1")
    fun findByUserId(id: Int): Diet

    @Insert
    fun insertAll(vararg diets: Diet)

    @Update
    fun updateUser(user: Diet)

    @Delete
    fun delete(user: Diet)

}