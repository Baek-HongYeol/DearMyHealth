package com.dearmyhealth.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val medId: Int,
    val id: String,
    val prodName: String, //이름
    val entpName: String, //제조사
    val dosage: Double?=null, //복용량
    val units: UNITS, //단위
    val description: String?, //약품 설명 -> CHART(성상)
    //val interactions: String?, //상호작용
    val warning: String? //경고사항
) {
    enum class UNITS {
        ML,
        MG,
        PILL
    }
}