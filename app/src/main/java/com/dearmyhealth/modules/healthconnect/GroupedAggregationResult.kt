package com.dearmyhealth.modules.healthconnect

import androidx.health.connect.client.aggregate.AggregationResult
import java.time.Instant

data class GroupedAggregationResult(val result: AggregationResult,
                                    val startTime: Instant,
                                    val endTime: Instant)