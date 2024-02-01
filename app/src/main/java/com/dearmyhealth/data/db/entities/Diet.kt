package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    indices = [Index(value=["user", "foodCode"])],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uid"], childColumns = ["user"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Food::class, parentColumns = ["code"], childColumns = ["foodCode"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val dietId: Int,
    val foodCode: Int,
    val user: Int,
    val time: Long,
    val type: MealType,
    val name: String,
    val imageURI: String,
    val calories: Double?,
    val carbohydrate: Double?,
    val frotein: Double?,
    val fat: Double?,
    val cholesterol: Double?
) {
    enum class MealType {
        MEAL_TYPE_UNKNOWN,
        MEAL_TYPE_BREAKFAST,
        MEAL_TYPE_LUNCH,
        MEAL_TYPE_DINNER,
        MEAL_TYPE_SNACK,
    }
}
