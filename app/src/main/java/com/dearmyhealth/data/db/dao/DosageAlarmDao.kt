package com.dearmyhealth.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.dearmyhealth.data.db.views.DosageAlarm
import java.time.Instant

@Dao
interface DosageAlarmDao {
    @Query("SELECT * FROM DosageAlarm WHERE isEnabled=:enabled AND endTime > :endTime")
    fun findIsEnabledLive(enabled: Boolean, endTime: Long = Instant.now().toEpochMilli()): LiveData<List<DosageAlarm>>

    @Query("SELECT * FROM DosageAlarm WHERE endTime>:endTime")
    fun list(endTime: Long = Instant.now().toEpochMilli()): LiveData<List<DosageAlarm>>

    @Query("SELECT * FROM DosageAlarm WHERE requestCode=:requestCode")
    fun findByRequestCode(requestCode: Int): DosageAlarm?
}