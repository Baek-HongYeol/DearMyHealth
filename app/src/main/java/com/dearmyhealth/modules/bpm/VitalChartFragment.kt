package com.dearmyhealth.modules.bpm

import androidx.fragment.app.Fragment

abstract class VitalChartFragment<T>(val dataType: VitalType) : Fragment() {
    internal abstract fun initializeGraph()

    internal abstract fun drawGraph(data: List<T>)
}