package com.dearmyhealth.modules.Diet.model

import java.time.temporal.ChronoUnit

object DietStats {

    enum class PERIOD(val displayName: String, val unit: ChronoUnit, val amount: Int) {
        WEEK("1주", ChronoUnit.DAYS, 7),
        MONTH("1개월", ChronoUnit.DAYS, 30),
        ThreeMONTH("3개월", ChronoUnit.WEEKS, 12),
        YEAR("1년", ChronoUnit.MONTHS, 12)
    }

}