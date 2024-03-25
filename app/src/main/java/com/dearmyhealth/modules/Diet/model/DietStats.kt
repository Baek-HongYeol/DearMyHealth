package com.dearmyhealth.modules.Diet.model

import java.util.Calendar

object DietStats {

    enum class PERIOD(val displayName: String, val unit: Int, val amount: Int) {
        WEEK("1주", Calendar.DAY_OF_MONTH, 7),
        MONTH("1개월", Calendar.MONTH, 1),
        ThreeMONTH("3개월", Calendar.MONTH, 3),
        YEAR("1년", Calendar.YEAR, 1)
    }

}