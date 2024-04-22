package com.dearmyhealth.modules.Dosage.repository

import android.util.Log
import com.dearmyhealth.api.Apiservice
import com.dearmyhealth.api.RetrofitObject.API_KEY
import com.dearmyhealth.data.db.dao.MedicationDao
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MedicationRepository(private val apiService: Apiservice, private val medicationDao: MedicationDao) {
    suspend fun fetchMedicationDetails(itemSeq: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDurItemInfo(
                ItemSeq = itemSeq,
                ServiceKey = API_KEY,
                PageNo = 1,
                NumOfRows = 10,
                Type = "json",
                ItemName = "itemName",
                EntpName = "entpName",
                StartChangeDate = "20200101",
                EndChangeDate = "20201231",
                Bizrno = "bizrno",
                TypeName = "typeName",
                IngrCode = "ingrCode"
                ///수정 nullable, 시퀀스로 하나, 이름으로 검색하면 이름으로 검색되게 하나.
            )
            if (response.isSuccessful) {
                response.body()?.response?.body?.items?.forEach { item ->
                    val medication = Medication(
                        medId = 0,
                        id = item.itemSeq,
                        prodName = item.itemName,
                        entpName = item.entpName,
                        dosage = parseDosage(item.chart),
                        units = parseUnits(item.chart),
                        description = item.chart,
                        warning = item.storageMethod
                    )
                    medicationDao.insert(medication)
                    fetchAdditionalMedicationInfo(item.itemSeq)
                }
            } else {
                Log.e("API Error", "Could not fetch medication details: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Failed to fetch and update medication: ${e.message}")
        }
    }

    private suspend fun fetchAdditionalMedicationInfo(itemSeq: String) {
        coroutineScope {
            val tasks = listOf(
                async { apiService.getContraindicationInfo(itemSeq,API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231", "bizrno") },
                async { apiService.getElderlyCautionInfo(itemSeq,API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231") },
                async { apiService.getAgeGroupContraindicationInfo(itemSeq,API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231") },
                async { apiService.getDosageCautionInfo(API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231", itemSeq) },
                async { apiService.getPeriodCautionInfo(API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231", itemSeq) },
                async { apiService.getEfficacyGroupDuplicationInfo(API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231", itemSeq, "bizrno") },
                async { apiService.getReleaseTabletSplittingInfo(API_KEY, 1, 10, "json", "typeName", "itemName", "20200101", "20201231", itemSeq, "entpName") },
                async { apiService.getPregnancyCautionInfo(API_KEY, 1, 10, "json", "typeName", "ingrCode", "itemName", "20200101", "20201231", itemSeq) }
            )
            tasks.forEach { task ->
                val additionalResponse = task.await()
                if (additionalResponse.isSuccessful) {
                    additionalResponse.body()?.response?.body?.items?.forEach { detail ->
                        // 추가 정보 처리
                        // 예: 데이터베이스 업데이트 또는 다른 처리
                    }
                } else {
                    Log.e("API Error", "Failed to fetch additional info: ${additionalResponse.errorBody()?.string()}")
                }
            }
        }
    }

    private fun parseDosage(dosageInfo: String?): Double {
        return dosageInfo?.toDoubleOrNull() ?: 0.0
    }

    private fun parseUnits(dosageInfo: String?): Medication.UNITS {
        return when {
            dosageInfo?.contains("mg", ignoreCase = true) == true -> Medication.UNITS.MG
            dosageInfo?.contains("ml", ignoreCase = true) == true -> Medication.UNITS.ML
            else -> Medication.UNITS.PILL
        }
    }
    suspend fun searchMedications(query: String): List<Medication> {
        return medicationDao.findByProdName("%$query%")
    }
}
