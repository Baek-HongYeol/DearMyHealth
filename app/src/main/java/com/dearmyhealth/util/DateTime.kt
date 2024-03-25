package com.dearmyhealth.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateTime {

    fun calToFormattedString(calendar: Calendar = Calendar.getInstance(), format: String = "yy년 M월 d일"): String {
        val sdf = SimpleDateFormat(format, Locale.KOREA)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(calendar.time)
    }
}