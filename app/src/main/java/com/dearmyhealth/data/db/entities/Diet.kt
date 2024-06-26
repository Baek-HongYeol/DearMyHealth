package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/**
 * @property dietId 데이터 identity key
 * @property foodCode 음식 코드
 * @property user 사용자 uid
 * @property time milliseconds 단위의 unixtime
 * @property type [Diet.MealType]에 따른 식사 유형
 * @property name 별명 혹은 음식 이름
 * @property imageURI 사진 파일 URI
 */
@Entity(
    indices = [Index(value=["user", "foodCode"])],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uid"], childColumns = ["user"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Food::class, parentColumns = ["code"], childColumns = ["foodCode"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class Diet(
    @PrimaryKey(autoGenerate = true)
    var dietId: Long,
    val foodCode: String?,
    val user: Int,
    val time: Long,
    val type: MealType,
    val name: String,
    val amount: Float,
    val imageURI: String?,
    val calories: Double?,
    val carbohydrate: Double?,
    val protein: Double?,
    val fat: Double?,
    val cholesterol: Double?
) {
    enum class MealType(val displayName: String) {
        MEAL_TYPE_UNKNOWN("기타"),
        MEAL_TYPE_BREAKFAST("아침"),
        MEAL_TYPE_LUNCH("점심"),
        MEAL_TYPE_DINNER("저녁"),
        MEAL_TYPE_SNACK("간식"),
    }
}
