package com.dearmyhealth.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.dearmyhealth.data.db.views.DosageAlarm

@Dao
interface DosageAlarmDao {
    @Query("SELECT * FROM DosageAlarm WHERE isEnabled=:enabled")
    fun findIsEnabledLive(enabled: Boolean): LiveData<List<DosageAlarm>>

    @Query("SELECT * FROM DosageAlarm WHERE requestCode=:requestCode")
    fun findByRequestCode(requestCode: Int): DosageAlarm?
}