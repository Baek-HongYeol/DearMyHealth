package com.dearmyhealth.data.db.dao

import androidx.lifecycle.LiveData
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
    suspend fun loadAllByUids(uids: IntArray): List<Diet>

    @Query("SELECT * FROM diet WHERE user=:uid AND dietId LIKE :dietId LIMIT 1")
    suspend fun findById(uid: Int, dietId: Long): List<Diet>

    @Query("SELECT * FROM DIET WHERE user=:uid AND (time BETWEEN :start AND :end)")
    suspend fun findByPeriod(uid: Int, start: Long, end: Long): List<Diet>

    @Query("SELECT * FROM DIET WHERE user=:uid AND (time BETWEEN :start AND :end)")
    fun findByPeriodLive(uid: Int, start: Long, end: Long): LiveData<List<Diet>>

    @Insert
    fun insertAll(vararg diets: Diet) : List<Long>

    @Update
    fun updateUser(user: Diet)

    @Delete
    fun delete(user: Diet)

}