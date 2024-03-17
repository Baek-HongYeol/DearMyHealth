package com.dearmyhealth.modules.Diet.model

import com.dearmyhealth.data.db.entities.Food

data class FoodSearchResult(
    val success: List<Food>? = null,
    val error: Exception? = null
)