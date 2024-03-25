package com.dearmyhealth.modules.Dosage.model


data class DURWarning (
    val type: DURType,
    val code: String,
    val description: String
) {
    enum class DURType{
        JOINT,
        PREGNANT,
        CAPACITY,
        PERIOD,
        OLD,
        AGE,
        DUPLICATED
    }
}