package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val dietId: Int,
    val time: Long,
    val type: MealType,
    val imageURI: String,
) {
    enum class MealType {
        MEAL_TYPE_UNKNOWN,
        MEAL_TYPE_BREAKFAST,
        MEAL_TYPE_LUNCH,
        MEAL_TYPE_DINNER,
        MEAL_TYPE_SNACK,
    }
}
