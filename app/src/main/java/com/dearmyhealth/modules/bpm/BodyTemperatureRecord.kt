package com.dearmyhealth.modules.bpm

import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Temperature
import java.time.Instant
import java.time.ZoneOffset

data class BodyTemperatureRecord(
    val time: Instant,
    val zoneOffset: ZoneOffset?,
    val temperature: Temperature,
    val measurementLocation: Int,
    override val metadata: Metadata
) : Record