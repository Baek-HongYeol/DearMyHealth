import android.util.Log
import com.dearmyhealth.api.Apiservice
import com.dearmyhealth.api.DurResponse
import com.dearmyhealth.api.Item
import com.dearmyhealth.api.RetrofitObject
import com.dearmyhealth.data.db.dao.AttentionDetailDao
import com.dearmyhealth.data.db.dao.MedicationDao
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

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

    private suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insert(medication)
    }
    suspend fun insertMedication(
        itemSeq: String = "",   // ItemSeq
        prodName: String = "", //이름
        entpName: String= "", //제조사
        dosage: Double= 0.0, //복용량
        units: String= "", //단위
    ) : Long {
        return insertMedication(Medication(0, itemSeq, prodName, entpName, dosage, units))
    }

    // 약물 정보를 입력한 검색 조건에 따라 조회하여 저장
    suspend fun fetchAndStoreMedications(itemSeq: String? = null, prodName: String? = null, entpName: String? = null):List<Medication> {
        try {
            val response = apiService.getDurItemInfo(
                ItemSeq = itemSeq, ServiceKey = RetrofitObject.API_KEY, PageNo = PAGE_NO, NumOfRows = NUM_OF_ROWS, ItemName = prodName, EntpName = entpName, StartChangeDate = null, EndChangeDate = null, Bizrno = null, TypeName = null, IngrCode = null
            ).awaitResponse()

            if (response.isSuccessful) {
                val medications = response.body()?.body?.items?.map { item ->
                    val medication = Medication(
                        itemSeq = item.itemSeq,
                        prodName = item.itemName,
                        entpName = item.entpName,
                        description = item.chart,
                        dosage = parseDosage(item.udDocID), // TO DO: 복용량 파싱 로직 구현하기
                        units = item.udDocID,
                        warning = item.nbDocId,
                        typeName = item.typeName,
                        typeCode = item.typeCode,
                    )
                    medicationDao.insert(medication)
                    medication
                } ?: emptyList()
                CoroutineScope(Dispatchers.IO).launch {
                    medications.forEach { medication ->
                        fetchAllAttentionDetails(medication.itemSeq)
                    }
                }
                return medications
            } else {
                Log.e("API Error", "Could not fetch medications: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error fetching medications: ${e.message}")
        }
        return emptyList()
    }

    /** 정확한 이름 혹은 일련번호로 검색 **/
    suspend fun findMedication(itemSeq: String, name: String?=null): Medication? {
        val searchName = name ?: itemSeq
        val medications = medicationDao.findByNameOrSeq(searchName, itemSeq)
        return if(medications.isEmpty()) null else medications[0]
    }

    /** 이름 혹은 일련번호 검색 **/
    suspend fun searchMedications(searchText: String): List<Medication> {
        // 먼저 DB에서 제품 이름 || 일련번호로 검색
        var medications = medicationDao.findByNameOrSeq("%$searchText%", searchText)

        if (medications.isEmpty()) {
            // DB에 결과가 없다면 API에서 검색
            medications=fetchAndStoreMedications(prodName = searchText)
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                medications.forEach { medication ->
                    fetchAllAttentionDetails(medication.itemSeq)
                }
            }
        }
        return medications
    }
    // 각 유형에 따라 병렬로 주의사항 정보를 가져오기
    private suspend fun fetchAllAttentionDetails(itemSeq: String) = coroutineScope {
        val attentionTypes = mapOf(
            "병용금기" to { apiService.getJointTabooInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS,TYPE_JSON,null,null,null,null,null,null) },
            "효능군중복" to { apiService.getEfficacyDuplicationInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null,null) },
            "서방정분할주의" to { apiService.getReleaseTabletSplittingInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "특정연령대금기" to { apiService.getSpecificAgeTabooInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "임부금기" to { apiService.getPregnancyCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "용량주의" to { apiService.getCapacityAttentionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "투여기간주의" to { apiService.getPeriodCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) },
            "노인주의" to { apiService.getElderlyCautionInfo(itemSeq, RetrofitObject.API_KEY, PAGE_NO, NUM_OF_ROWS, TYPE_JSON,null,null,null,null,null) }
        )

        attentionTypes.map { (typeName, fetchFunction) ->
            async { genericFetchAttentionInfo(itemSeq, typeName, fetchFunction) }
        }.awaitAll().flatten()
    }

    private suspend fun genericFetchAttentionInfo(
        itemSeq: String,
        typeName: String,
        fetchFunction: suspend () -> Call<DurResponse<List<Item>>>
    ): List<AttentionDetail> {
        val attentionDetails = mutableListOf<AttentionDetail>()
        try {
            Log.d("FetchAttentionInfo", "Fetching attention details for $typeName with itemSeq: $itemSeq")
            val response = fetchFunction().awaitResponse()
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("FetchAttentionInfo", "Full Response: ${responseBody.toString()}")
                val items = responseBody?.body?.items
                if (items != null && items.isNotEmpty()) {
                    items.forEach { detail ->
                        Log.d("FetchAttentionInfo", "Item: $detail")
                        val attentionDetail = AttentionDetail(
                            itemSeq = detail.itemSeq,
                            typeName = typeName,
                            prhbtContent = detail.prohibitContent
                        )
                        attentionDetailDao.insert(attentionDetail)
                        attentionDetails.add(attentionDetail)
                    }
                } else {
                    Log.e("API Error", "No items found for $typeName, items: $items")
                }
            } else {
                Log.e("API Error", "Could not fetch attention details for $typeName: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MedicationRepository", "Error fetching attention details for $typeName: ${e.message}")
        }
        return attentionDetails
    }

    suspend fun getMedication(medId: Int): Medication? {
        return medicationDao.find(medId)
    }

    /** 약물의 주의사항 정보를 가져오기 **/
    suspend fun getAttentionDetailsByItemSeq(itemSeq: String): List<AttentionDetail> {
        Log.d("MedicationRepository", "Fetching all attention details for itemSeq: $itemSeq")
        return attentionDetailDao.findByItemSeq(itemSeq)
    }

    /** TO DO:용량 값을 파싱하는 함수 **/
    private fun parseDosage(dosageInfo: String?): Double {
        return dosageInfo?.toDoubleOrNull() ?: 0.0
    }
}
