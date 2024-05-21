package com.dearmyhealth.data.db

import androidx.annotation.IntRange
import androidx.room.TypeConverter
import com.dearmyhealth.data.db.entities.AttentionDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun listToJson(value: List<Int>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()

    @TypeConverter
    fun boolToInt(value: Boolean): Int = if(value) 1 else 0

    @TypeConverter
    fun intToBool(@IntRange(0,1) value: Int) = value>0

    @TypeConverter
    fun fromAttentionDetailList(value: List<AttentionDetail>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<AttentionDetail>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toAttentionDetailList(value: String): List<AttentionDetail>? {
        val gson = Gson()
        val type = object : TypeToken<List<AttentionDetail>>() {}.type
        return gson.fromJson(value, type)
    }
}