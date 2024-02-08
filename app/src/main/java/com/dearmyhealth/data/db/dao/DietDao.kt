package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.Diet

@Dao
interface DietDao {
    @Query("SELECT * FROM diet")
    fun getAll(): List<Diet>

    @Query("SELECT * FROM diet WHERE user IN (:uids)")
    fun loadAllByUids(uids: IntArray): List<Diet>

    @Query("SELECT * FROM diet WHERE dietId LIKE :dietId LIMIT 1")
    fun findById(dietId: Int): List<Diet>

    @Insert
    fun insertAll(vararg diets: Diet)

    @Update
    fun updateUser(user: Diet)

    @Delete
    fun delete(user: Diet)

}