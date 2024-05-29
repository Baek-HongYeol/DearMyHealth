package com.dearmyhealth.data.db

import androidx.annotation.IntRange
import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun listToJson(value: List<Int>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()

    @TypeConverter
    fun boolToInt(value: Boolean): Int = if(value) 1 else 0

    @TypeConverter
    fun intToBool(@IntRange(0,1) value: Int) = value>0
}