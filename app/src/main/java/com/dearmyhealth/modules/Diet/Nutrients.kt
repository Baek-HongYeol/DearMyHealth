package com.dearmyhealth.modules.Diet

import com.dearmyhealth.R

data class Nutrients(
    val user: Int,
    val date: Long,
    var calories: Double?,
    var nutrients: MutableMap<Names, Double?>
) {
    enum class Names {
        carbohydrate,
        protein,
        fat,
        cholesterol
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
    }
}