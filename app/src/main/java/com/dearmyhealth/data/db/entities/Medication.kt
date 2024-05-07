package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medId: Int = 0,
    val id: String?,
    val prodName: String = "", //이름
    val entpName: String= "", //제조사
    val dosage: Double= 0.0, //복용량
    val units: String= "", //단위
    val description: String= "", //약품 설명 -> CHART(성상)
    val warning: String= "", //경고사항
    val typeCode: String= "", //유형코드
    val typeName: String= "" //DUR 유형
) {
    enum class UNITS {
        ML,
        MG,
        PILL
    }
}