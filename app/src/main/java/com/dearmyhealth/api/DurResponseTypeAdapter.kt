package com.dearmyhealth.api

import com.google.gson.*
import java.lang.reflect.Type

class DurResponseTypeAdapter<T> : JsonDeserializer<DurResponse<T>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DurResponse<T> {
        val jsonObject = json?.asJsonObject
        val header = context?.deserialize<Header>(jsonObject?.get("header"), Header::class.java)
        val body = context?.deserialize<Body<T>>(jsonObject?.get("body"), typeOfT)
        return DurResponse(header!!, body!!)
    }
}