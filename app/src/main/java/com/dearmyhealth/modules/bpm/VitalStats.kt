package com.dearmyhealth.modules.bpm

import java.time.temporal.ChronoUnit

enum class VitalType {
    BPM,
    TEMPERATURE,
    WEIGHT,
    STEPS
}

enum class PERIOD(val unit: ChronoUnit, val unitValue: Long, val subunit: ChronoUnit, val subunitValue: Int) {
    DAY(ChronoUnit.DAYS, 1, ChronoUnit.HOURS, 1),
    WEEK(ChronoUnit.DAYS, 7, ChronoUnit.DAYS, 1),
    MONTH(ChronoUnit.MONTHS, 1, ChronoUnit.DAYS, 1),
    YEAR(ChronoUnit.YEARS, 1, ChronoUnit.MONTHS, 1)
}