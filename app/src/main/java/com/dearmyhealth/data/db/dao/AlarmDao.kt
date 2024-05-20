package com.dearmyhealth.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dearmyhealth.data.db.entities.Alarm

@Dao
interface AlarmDao {
    @Insert
    fun insert(alarm: Alarm)

    @Update
    fun update(alarm: Alarm)

    @Query("SELECT * FROM Alarm WHERE requestCode=:code")
    suspend fun findByRequestCode(code: Int) : Alarm?

    @Query("SELECT * FROM Alarm WHERE time=:time")
    suspend fun findByTime(time:Long) : List<Alarm>

    @Query("SELECT * FROM Alarm WHERE dosageId=:dosageId")
    suspend fun findByDosageId(dosageId: Int) : Alarm

    @Query("SELECT * FROM Alarm WHERE isEnabled=:enabled")
    suspend fun findIsEnabled(enabled: Boolean): List<Alarm>

    @Delete
    fun delete(alarm: Alarm)
}
