import android.util.Log
import com.dearmyhealth.api.Apiservice
import com.dearmyhealth.api.DurItem
import com.dearmyhealth.api.RetrofitObject
import com.dearmyhealth.data.db.dao.AttentionDetailDao
import com.dearmyhealth.data.db.dao.MedicationDao
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Response

class MedicationRepository(
    private val apiService: Apiservice,
    private val medicationDao: MedicationDao,
    private val attentionDetailDao: AttentionDetailDao
) {
    companion object {
        private const val PAGE_NO = 1
        private const val NUM_OF_ROWS = 100
        private const val TYPE_JSON = "json"
    }

    // 약물 정보를 입력한 검색 조건에 따라 조회하여 저장
    suspend fun fetchAndStoreMedications(itemSeq: String? = null, prodName: String? = null, entpName: String? = null) {
        try {
            val response = apiService.getDurItemInfo(
                ItemSeq = itemSeq, ServiceKey = RetrofitObject.API_KEY, PageNo = PAGE_NO, NumOfRows = NUM_OF_ROWS, ItemName = prodName, EntpName = entpName, StartChangeDate = null, EndChangeDate = null, Bizrno = null, TypeName = null, IngrCode = null
            )

            if (response.isSuccessful) {
                response.body()?.response?.body?.items?.forEach { item ->
                    val medication = Medication(
                        id = item.itemSeq,
                        prodName = item.itemName,
                        entpName = item.entpName,
                        description = item.chart,
                        dosage = parseDosage(null), // TO DO: 복용량 파싱 로직 구현하기
                        units = item.udDocID,
                        warning = item.nbDocId,
                        typeName = item.typeName,
                        typeCode = item.typeCode
                    )
                    medicationDao.insert(medication)

                    // 병용금기, 효능군중복주의, 서방정분할주의, 그리고 기타 주의사항들을 병렬로 조회
                    fetchAllAttentionDetails(item.itemSeq)
                }
            } else {
                Log.e("API Error", "Could not fetch medications: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error fetching medications: ${e.message}")
        }
    }

    suspend fun searchMedications(searchText: String): List<Medication> {
        // 먼저 DB에서 제품 이름 || 일련번호로 검색
        var medications = medicationDao.findByNameOrSeq("%$searchText%", searchText)

        if (medications.isEmpty()) {
            // DB에 결과가 없다면 API에서 검색
            val response = apiService.getDurItemInfo(
                ItemSeq = null, ServiceKey = RetrofitObject.API_KEY, PageNo = PAGE_NO,
                NumOfRows = NUM_OF_ROWS, ItemName = searchText, EntpName = null,
                StartChangeDate = null, EndChangeDate = null, Bizrno = null, TypeName = null, IngrCode = null
            )
            if (response.isSuccessful && response.body() != null) {
                medications = response.body()!!.response.body.items.map { item ->
                    Medication(
                        id = item.itemSeq,
                        prodName = item.itemName,
                        entpName = item.entpName,
                        description = item.chart,
                        dosage = parseDosage(item.udDocID),  // TO DO: 파싱 로직
                        units = item.udDocID,
                        warning = item.nbDocId,
                        typeName = item.typeName,
                        typeCode = item.typeCode
                    )
                }
                // DB에 결과 저장
                medicationDao.insertAll(medications)
            }
        }
        return medications
    }
    // 각 유형에 따라 병렬로 주의사항 정보를 가져오기
    private suspend fun fetchAllAttentionDetails(itemSeq: String) = coroutineScope {
        val attentionTypes = mapOf(
            "병용금기" to { apiService.getJointTabooInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null,null) },
            "효능군중복" to { apiService.getEfficacyDuplicationInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null,null) },
            "서방정분할주의" to { apiService.getReleaseTabletSplittingInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "B" to { apiService.getSpecificAgeTabooInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },  // 특정연령대금기
            "C" to { apiService.getPregnancyCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },  // 임부금기
            "D" to { apiService.getCapacityAttentionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },  // 용량주의
            "E" to { apiService.getPeriodCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },  // 투여기간주의
            "F" to { apiService.getElderlyCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) }   // 노인주의
        )

        attentionTypes.map { (typeCode, fetchFunction) ->
            async { genericFetchAttentionInfo(itemSeq, typeCode, fetchFunction) }
        }.awaitAll()
    }

    private suspend fun genericFetchAttentionInfo(
        itemSeq: String,
        typeCode: String,
        fetchFunction: suspend () -> Response<DurItem>
    ) {
        val response = fetchFunction()
        if (response.isSuccessful && response.body() != null) {
            response.body()!!.response.body.items.forEach { detail ->
                val attentionDetail = AttentionDetail(
                    itemseq = detail.itemSeq,
                    typeCode = typeCode,
                    prhbtContent = detail.prohibitContent
                )
                attentionDetailDao.insert(attentionDetail)
            }
        }
    }

    /* TO DO:용량 값을 파싱하는 함수*/
    private fun parseDosage(dosageInfo: String?): Double {
        return dosageInfo?.toDoubleOrNull() ?: 0.0
    }
}
