package com.dearmyhealth.modules.Alarm

import android.content.Context
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.dao.AlarmDao
import com.dearmyhealth.data.db.entities.Alarm

class AlarmRepository(private val alarmDao: AlarmDao) {

    fun insertAlarm(requestId: Int, dosageId: Int, time:Long, desc:String) {
        alarmDao.insert(Alarm(0, dosageId, requestId, time, desc, true))
    }

    suspend fun findByRequestId(requestId: Int): Alarm? {
        return alarmDao.findByRequestCode(requestId)
    }

    suspend fun findByDosageId(dosageId: Int): Alarm? {
        return alarmDao.findByDosageId(dosageId)
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.findIsEnabled(true)
    }

    fun updateAlarm(alarm:Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun deleteAlarms(vararg alarm: Alarm){
        for(item in alarm) {
            alarmDao.delete(item)
        }
    }


    companion object {
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository {
            if(INSTANCE==null)
                INSTANCE = AlarmRepository(AppDatabase.getDatabase(context.applicationContext).alarmDao())
            return INSTANCE!!
        }
    }
}