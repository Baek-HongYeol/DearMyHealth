package com.dearmyhealth.modules.bpm

import androidx.fragment.app.Fragment
import androidx.health.connect.client.records.WeightRecord

class WeightFragment : VitalChartFragment<WeightRecord>(VitalType.WEIGHT) {

    override fun initializeGraph() {
        TODO("Not yet implemented")
    }

    override fun drawGraph(data: List<WeightRecord>) {
        TODO("Not yet implemented")
    }
}
