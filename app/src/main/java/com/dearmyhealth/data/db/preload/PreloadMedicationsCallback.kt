package com.dearmyhealth.data.db.preload

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dearmyhealth.R
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.entities.Medication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * 약물 데이터를 앱 첫 실행 시 로드합니다.
 * comment: load 중에 앱이 종료되면 load가 중단되고 더이상 불러와지지 않는 것 같습니다.
 */
class PreloadMedicationsCallback(private val context: Context): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            preloadMedications(context)
        }
    }
    suspend fun preloadMedications(context: Context) {
        try {
            val medDao = AppDatabase.getDatabase(context).medicationDao()

            val medList: JSONArray =
                context.resources.openRawResource(R.raw.dur_product_list).bufferedReader().use {
                    JSONArray(it.readText())
                }

            medList.takeIf { it.length() > 0 }?.let { list ->
                for (index in 0 until list.length()) {
                    val medObj = list.getJSONObject(index)
                    medDao.insert(
                        Medication(
                            0,
                            medObj.getString("ITEM_SEQ"),
                            medObj.getString("ITEM_NAME"),
                            medObj.getString("ENTP_NAME"),
                            0.0,
                            "",
                            medObj.getString("CHART"),
                            "",
                            medObj.getString("TYPE_CODE"),
                            medObj.getString("TYPE_NAME")
                        )
                    )

                }
                Log.e("Preload_Med", "successfully pre-load medications into database")
            }
        } catch (exception: Exception) {
            Log.e(
                "Preload_Med",
                exception.localizedMessage ?: "failed to pre-load medications into database"
            )
        }
    }
}