package com.dearmyhealth.modules.Diet.model

import com.dearmyhealth.MyApplication
import com.dearmyhealth.R
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.data.db.dao.NutrientStandardDao
import com.dearmyhealth.data.db.entities.User

/**
 * 영양 정보만 다루기 위한 class.
 */
data class Nutrients(
    val user: Int,
    val date: Long,
    var calories: Double? = null,
    var nutrients: MutableMap<Names, Double?> = mutableMapOf()
) {
    /**
     * Nutrients class에서 다룰 수 있는 영양소를 제한.
     */
    enum class Names(var displayedName:String) {
        carbohydrate("탄수화물"),
        protein("단백질"),
        fat("지방"),
        cholesterol("콜레스테롤"),
        water_liquid("액체 수분량"),
        water_total("총 수분량")
    }
    fun getPresentNutrientsNames(): List<String>{
        return nutrients.keys.toList().map{ v -> v.name }
    }
    fun getNutrientValueByName(name: String): Double? {
        return try {
            val cons = Names.valueOf(name)
            nutrients[cons]
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    companion object {
        val resourceIds = mapOf(
            Names.carbohydrate.name to R.string.carbohydrate,
            Names.protein.name to R.string.protein,
            Names.fat.name to R.string.fat,
            Names.cholesterol.name to R.string.cholesterol
        )
        private val nutrientStandardDao: NutrientStandardDao by lazy {
            AppDatabase.getDatabase(MyApplication.ApplicationContext())
                    .nutrientStandardDao()
        }

        val RECOMMENDED_NUTRIENT_INTAKE: Nutrients = Nutrients(0,0,
            2200.toDouble(),
            mutableMapOf(
                Names.carbohydrate to 130.toDouble(),
                Names.protein to 55.toDouble(),
                Names.fat to 55.toDouble()
            ))

        // 평균 필요 섭취량
        suspend fun loadNutrientEAR(gender: User.Gender, age: Int): Nutrients {
            val res = nutrientStandardDao.loadEAR(gender.name, age.toFloat())
            val mapping = mutableMapOf(
                Names.carbohydrate to res.carbohydrate?.toDouble(),
                Names.protein to res.protein?.toDouble(),
                Names.fat to res.fat?.toDouble()
            )
            return Nutrients(0, 0, res.calories?.toDouble(), mapping)
        }

        // 권장 섭취량 (영아는 충분 섭취량)
        suspend fun loadNutrientRNI(gender: User.Gender, age: Int): Nutrients {
            val res = nutrientStandardDao.loadRNI(gender.name, age.toFloat())
            val mapping = mutableMapOf(
                Names.carbohydrate to res.carbohydrate?.toDouble(),
                Names.protein to res.protein?.toDouble(),
                Names.fat to res.fat?.toDouble(),
                Names.water_liquid to res.water_liquid?.toDouble(),
                Names.water_total to res.water_total?.toDouble()
            )
            return Nutrients(0, 0, res.calories?.toDouble(), mapping)
        }
    }
}