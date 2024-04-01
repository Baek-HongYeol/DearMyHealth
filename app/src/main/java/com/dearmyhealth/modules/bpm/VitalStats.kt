package com.dearmyhealth.modules.bpm

import java.time.temporal.ChronoUnit

enum class VitalType {
    BPM,
    TEMPERATURE,
    WEIGHT,
    STEPS
}

enum class PERIOD(val unit: ChronoUnit) {
    DAY(ChronoUnit.DAYS),
    WEEK(ChronoUnit.WEEKS),
    MONTH(ChronoUnit.MONTHS),
    YEAR(ChronoUnit.YEARS)
}