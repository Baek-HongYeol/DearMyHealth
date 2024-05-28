package com.dearmyhealth.modules.Diet

import androidx.lifecycle.LiveData
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.dao.DietDao
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.modules.login.Session
import java.util.Calendar

class DietRepository(private val datasource: DietDao) {

    /** 기간 내 데이터 검색
     *
     * TODO - 로그인 기능 구현 후 Session.uid에 해당하는 데이터만 호출하도록
     * @param start 검색할 기간의 시작 시간 (milliseconds)
     * @param end 검색할 기간의 종료 시간 (milliseconds)
     * @return [List]<[Diet]> for query result.
     */
    suspend fun findByPeriod(start: Long=0, end:Long=Calendar.getInstance().timeInMillis): List<Diet> {
        return datasource.findByPeriod(0, start, end)
    }

    /** 기간 내 데이터 검색
     *
     * TODO - 로그인 기능 구현 후 Session.uid에 해당하는 데이터만 호출하도록
     * @param start 검색할 기간의 시작 시간 (milliseconds)
     * @param end 검색할 기간의 종료 시간 (milliseconds)
     * @return [LiveData]<[List]<[Diet]>> for query result distinct until changed.
     */
    fun findByPeriodLive(start:Long = 0, end:Long = Calendar.getInstance().timeInMillis):LiveData<List<Diet>> {
        return datasource.findByPeriodLive(Session.currentUser.uid, start, end)
    }

    /**
     * valid한 diet 데이터를 저장한다.
     */
    private fun insert(diet: Diet): Long {
        return datasource.insertAll(diet)[0]
    }
    fun insert(time: Long, foodcode: String?, type: Diet.MealType, name: String, amount:Float,
               imageURI:String?, calories: Int?, carbohydrate: Int?, protein: Int?, fat: Int?,
               cholesterol: Int?): Result<Diet> {
        try {
            val diet = Diet(
                0, foodcode, Session.currentUser.uid, time, type, name, amount,
                imageURI, calories?.toDouble(), carbohydrate?.toDouble(),
                protein?.toDouble(), fat?.toDouble(), cholesterol?.toDouble()
            )
            val id = insert(diet)
            return Result.Success(diet.apply { dietId = id })
        }
        catch (e: Exception) {
            return Result.Error(e)
        }


    }
    fun insert(time: Long, foodcode: String?, type: Diet.MealType, name: String, amount: Float,
               imageURI:String?, calories: Double?, carbohydrate: Double?, protein: Double?, fat: Double?,
               cholesterol: Double?): Result<Diet> {
        try {
            val diet = Diet(
                0, foodcode, Session.currentUser.uid, time, type, name, amount,
                imageURI, calories, carbohydrate, protein, fat, cholesterol
            )
            val id = insert(diet)
            return Result.Success(
                diet.apply { dietId = id }
            )
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }

}