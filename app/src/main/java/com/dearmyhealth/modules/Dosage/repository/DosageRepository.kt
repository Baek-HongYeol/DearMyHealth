package com.dearmyhealth.modules.Dosage.repository

import com.dearmyhealth.data.db.dao.DosageDao
import com.dearmyhealth.data.db.entities.Dosage

class DosageRepository(private val dosageDao: DosageDao) {
    suspend fun insertDosage(dosage: Dosage) {
        dosageDao.insert(dosage)
    }

    // 다른 CRUD 연산 메소드 추가...
}
