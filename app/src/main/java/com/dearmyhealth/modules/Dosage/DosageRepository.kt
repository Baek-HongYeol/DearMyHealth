package com.dearmyhealth.modules.Dosage

import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.dao.DosageDao
import com.dearmyhealth.data.db.dao.MedicationDao
import com.dearmyhealth.data.db.entities.Dosage
import java.time.Instant

class DosageRepository(private val datasource: DosageDao, private val medicationSource: MedicationDao) {
    private fun insert(dosage: Dosage): Int {
        return datasource.insert(dosage).toInt()
    }
    suspend fun insert(medicationCode: String?=null, name:String, startTime: Long, endTime: Long,
                       dosageTime: Long, dosage: Double?=1.0, user:Int =0): Result<Dosage> {
        var medicationId = 0
        if (medicationCode!=null) {
            try {
                medicationId = medicationSource.findByCode(medicationCode).id
            }
            catch (e:Exception) {
                medicationId = 0
            }
        }

        try {
            val id = insert(
                Dosage(
                    0, Instant.now().toEpochMilli(), medicationId, user, name,
                    startTime, endTime, dosageTime, dosage, ""
                )
            )
            return Result.Success(
                Dosage(
                    id, Instant.now().toEpochMilli(), medicationId, user, name,
                    startTime, endTime, dosageTime, dosage, ""
                )
            )
        }
        catch (e: Exception) {
            return Result.Error(e)
        }


    }
}