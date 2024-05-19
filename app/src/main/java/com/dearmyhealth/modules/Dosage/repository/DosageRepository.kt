package com.dearmyhealth.modules.Dosage.repository

import MedicationRepository
import android.util.Log
import androidx.lifecycle.LiveData
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.dao.DosageDao
import com.dearmyhealth.data.db.entities.Dosage
import java.time.Instant

class DosageRepository(private val dosageDao: DosageDao, private val medicationSource: MedicationRepository) {

    suspend fun insertDosage(dosage: Dosage) {
        dosageDao.insert(dosage)
    }

    private fun insert(dosage: Dosage): Int {
        return dosageDao.insert(dosage).toInt()
    }
    suspend fun insert(medItemSeq: String?=null, name:String, startTime: Long, endTime: Long,
                       dosageTime: List<Long>, dosage: Double?=1.0, user:Int =0): Result<Dosage> {
        var itemSeq = "0"
        if (medItemSeq!=null) {
            try {
                val result = medicationSource.findMedication(medItemSeq, name)
                if (result != null) {
                    itemSeq = result.itemSeq
                    Log.d("DosageRepository", "insert: medication exists")
                }
                else
                    medicationSource.insertMedication(medItemSeq, name)
            }
            catch (e:Exception) {
                itemSeq = "0"
            }
        }

        try {
            val id = insert(
                Dosage(
                    0, Instant.now().toEpochMilli(), itemSeq, user, name,
                    startTime, endTime, dosageTime, dosage, ""
                )
            )
            return Result.Success(
                Dosage(
                    id, Instant.now().toEpochMilli(), itemSeq, user, name,
                    startTime, endTime, dosageTime, dosage, ""
                )
            )
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun updateDosage(dosage: Dosage) {
        dosageDao.update(dosage)
    }

    suspend fun deleteDosage(dosage: Dosage) {
        dosageDao.delete(dosage)
    }

    // 다른 CRUD 연산 메소드 추가...

    suspend fun getDosage(id: Int): Dosage? {
        return dosageDao.getDosage(id)
    }
    suspend fun listDosage(): List<Dosage> {
        return dosageDao.findDosageForAfter(0, Instant.now().toEpochMilli())
    }

    fun listLiveDosage(): LiveData<List<Dosage>> {
        return dosageDao.findLiveDosageForAfter(0, Instant.now().toEpochMilli())
    }
}
