package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.lifecycle.Observer
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentStepBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class WeightFragment : VitalChartFragment<WeightRecord>(VitalType.WEIGHT) {
    private val TAG = javaClass.simpleName
    override lateinit var binding: FragmentStepBinding

    val observer = Observer<List<WeightRecord>> { value ->
        setChartData(value)
        analyzeData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터 추가 버튼
        binding.stepAddDataLL.setOnClickListener {
            startActivity(Intent(requireContext(), WeightAddActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()

        viewModel.weightRecords.observe(viewLifecycleOwner, observer)
    }

    override fun onPause() {
        super.onPause()
        viewModel.weightRecords.removeObserver(observer)
    }


    /********  fetch Data  **********/

    override fun fetchData(){
        if (currentRange == 0)
            fetchDataForDay(viewModel.startOfRange)
        else
            fetchDataInPeriod(period)
    }
    override fun fetchDataForDay(date: OffsetDateTime) {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.readWeightRecords(startOfDay, endOfDay)
            }
        }
    }
    override fun fetchDataInPeriod(period: PERIOD) {
        val startOfRange = viewModel.startOfRange.toInstant()
        val endOfRange = viewModel.endOfRange.toInstant()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.readWeightRecords(startOfRange, endOfRange)
            }
        }
    }


    override fun configureAxis() {
        super.configureAxis()
        binding.stepBarChart.axisLeft.axisMaximum = 80f
    }

    override fun setChartData(dataList: List<WeightRecord>) {

        fun generateGroups(): Map<OffsetDateTime, List<WeightRecord>> {
            val sortedList = dataList.sortedBy { record -> record.time }

            if(period == PERIOD.WEEK)
                return dataList.groupBy {
                    OffsetDateTime.ofInstant(
                        it.time, ZoneId.systemDefault()
                    ).truncatedTo(ChronoUnit.DAYS) }

            else
                return viewModel.startOfRange.run {
                    val map = mutableMapOf<OffsetDateTime, MutableList<WeightRecord>>()
                    var cur = this
                    var next = this.plus(1, period.subunit)
                    for (el in sortedList) {
                        if(el.time.toEpochMilli() < this.toInstant().toEpochMilli())
                            continue

                        while(el.time.toEpochMilli() >= next.toInstant().toEpochMilli()) {
                            if(next.isAfter(viewModel.endOfRange)) break
                            cur = next
                            next = next.plus(1, period.subunit)
                        }
                        if( el.time.toEpochMilli() >= cur.toInstant().toEpochMilli() &&
                            el.time.toEpochMilli() < next.toInstant().toEpochMilli() ){
                            if(!map.contains(cur))
                                map[cur] = mutableListOf()
                            val recordList = map[cur]
                            recordList!!.add(el)
                            map[cur] = recordList
                        }
                        else {
                            if(next.isAfter(viewModel.endOfRange)) break
                            cur = next
                            next = next.plus(1, period.subunit)
                        }
                    }
                    Log.d(TAG, map.toString())
                    map
                }
        }

        val groups = generateGroups().mapValues { entry ->
            val divider = if(entry.value.isNotEmpty()) entry.value.size else 1
            entry.value.fold(0f) { acc, record ->
                (acc + (record.weight.inKilograms.toFloat()))
            }.div(divider)
        }
        var maxValue = 80f
        val entries: MutableList<Entry> = mutableListOf()
        for (g in groups) {
            entries.add(Entry(TimeUnit.SECONDS.toHours(g.key.toEpochSecond()).toFloat(), g.value))
            if(maxValue < g.value) maxValue = g.value
        }
        val lineDataSet = LineDataSet(entries,"체중")
        lineDataSet.apply {
            color = resources.getColor(R.color.primary, null)
            circleRadius = 5f
            lineWidth = 3f
            setCircleColor(resources.getColor(R.color.purple_700, null))
            circleHoleColor = resources.getColor(R.color.purple_700, null)
            setDrawHighlightIndicators(false)
            setDrawValues(true) // 숫자표시
            valueTextColor = resources.getColor(R.color.black, null)
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }

        val combinedData = CombinedData()
        combinedData.setData(LineData(lineDataSet))

        configureAxis()

        val barChart = binding.stepBarChart
        barChart.apply {
            data = combinedData
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            axisLeft.axisMaximum = maxValue * 1.1f
            description.isEnabled = false
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }
    }

    @SuppressLint("SetTextI18n")
    fun analyzeData() {
        val summaryText = binding.metricChartDataTV
        val summaryValueText = binding.chartRepresentedDataTV
        summaryText.text = "평균"
        var result: Mass? = null
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                result = viewModel.healthConnectManager.computeWeightAverage(
                    viewModel.startOfRange.toInstant(),
                    viewModel.endOfRange.toInstant()
                )
            }
        }
        if(result != null) {
            summaryValueText.text = "${result!!.inKilograms} kg"
        }
        else
            summaryValueText.text = "0 kg"
    }
}
