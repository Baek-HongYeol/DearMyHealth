package com.dearmyhealth.modules.bpm

import androidx.health.connect.client.records.BodyTemperatureRecord

class TempFragment : VitalChartFragment<BodyTemperatureRecord>(VitalType.TEMPERATURE) {
    override fun initializeGraph() {
        TODO("Not yet implemented")
    }

    override fun drawGraph(data: List<BodyTemperatureRecord>) {
        TODO("Not yet implemented")
    }
}
