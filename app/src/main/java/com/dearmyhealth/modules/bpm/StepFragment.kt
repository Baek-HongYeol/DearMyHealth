package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.Observer
import com.dearmyhealth.R
import com.dearmyhealth.modules.healthconnect.GroupedAggregationResult
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class StepFragment : VitalChartFragment<GroupedAggregationResult>(VitalType.STEPS) {
    private val TAG = javaClass.simpleName

    var metric: AggregateMetric<Number> = StepsRecord.COUNT_TOTAL

    // Steps Livedata Observer 정의
    val observer = Observer<List<GroupedAggregationResult>> { value ->
        Log.d(TAG, "livedata change observed")
        Log.d(TAG, "size: ${value.size}")

        setChartData(value)
        analyzeData(
            value,
            period.subunit.between(viewModel.startOfRange, viewModel.endOfRange).toInt()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터 추가 버튼
        binding.stepAddDataLL.setOnClickListener {
            startActivity(Intent(requireContext(), StepAddActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()

        viewModel.stepsIntoSlices.observe(viewLifecycleOwner, observer)
    }
    override fun onPause() {
        super.onPause()
        viewModel.stepsIntoSlices.removeObserver(observer)
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
                viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Duration.ofHours(1))
            }
        }
    }
    override fun fetchDataInPeriod(period: PERIOD) {
        val startOfRange = viewModel.startOfRange.toLocalDateTime()
        val endOfRange = viewModel.endOfRange.toLocalDateTime()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfRange, endOfRange, Period.of(0,
                    if(period==PERIOD.YEAR) 1 else 0,
                    if(period==PERIOD.YEAR) 0 else 1
                ))
            }
        }
    }

    /********  setting Chart  **********/

    override fun setChartData(dataList: List<GroupedAggregationResult>) {

        fun generateGroups(): Map<OffsetDateTime, List<GroupedAggregationResult>> {
            val sortedList = dataList.sortedBy { record -> record.startTime }

            if(period == PERIOD.WEEK)
                return dataList.groupBy {
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(it.startTime.toEpochMilli()), ZoneId.systemDefault()
                    ).truncatedTo(ChronoUnit.DAYS).plusHours(12) }

            else
                return viewModel.startOfRange.run {
                    val map = mutableMapOf<OffsetDateTime, MutableList<GroupedAggregationResult>>()
                    var cur = this
                    var next = this.plus(1, period.subunit)
                    for (el in sortedList) {
                        if(el.startTime.toEpochMilli() < this.toInstant().toEpochMilli())
                            continue

                        while(el.startTime.toEpochMilli() >= next.toInstant().toEpochMilli()) {
                            if(next.isAfter(viewModel.endOfRange)) break
                            cur = next
                            next = next.plus(1, period.subunit)
                        }
                        if( el.startTime.toEpochMilli() >= cur.toInstant().toEpochMilli() &&
                            el.startTime.toEpochMilli() < next.toInstant().toEpochMilli() ){
                            val middle = OffsetDateTime.ofInstant(
                                el.startTime.plusSeconds((el.endTime.epochSecond - el.startTime.epochSecond)/2),
                                ZoneId.systemDefault()
                            )
                            if(!map.contains(middle))
                                map[middle] = mutableListOf()
                            val recordList = map[middle]
                            recordList!!.add(el)
                            map[middle] = recordList
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
            entry.value.fold(0f) { acc, record -> (acc + (record.result[metric]?.toFloat()?:0f)) }
        }
        var maxValue = 100f
        val entries: MutableList<BarEntry> = mutableListOf()

        for (g in groups) {
            entries.add(BarEntry(TimeUnit.SECONDS.toHours(g.key.toEpochSecond()).toFloat(), g.value))
            if(maxValue < g.value) maxValue = g.value
        }

        val barDataSet = BarDataSet(entries,"걸음수")
        barDataSet.apply {
            color = resources.getColor(R.color.primary, null)
            setDrawValues(true) // 숫자표시
            valueTextColor = resources.getColor(R.color.black, null)
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }
        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        combinedData.barData.barWidth = when(currentRange) {
            0 -> 0.6f
            1 -> 8f
            2 -> 15f
            3 -> 400f
            else -> 2f
        }

        configureAxis()

        val barChart = binding.stepBarChart
        barChart.apply {
            data = combinedData
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            axisLeft.axisMaximum = maxValue * 1.05f
            description.isEnabled = false
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }
    }


    @SuppressLint("SetTextI18n")
    fun analyzeData(data: List<GroupedAggregationResult>, totalUnit: Int) {
        val summaryText = binding.metricChartDataTV
        val summaryValueText = binding.chartRepresentedDataTV
        Log.d(TAG, "totalUnits: $totalUnit")
        if(currentRange == 0) {
            summaryText.text = "합계"
            var sum = 0
            for (record in data) {
                val metricValue = record.result[metric]?.toInt()?:continue
                sum += metricValue
            }
            summaryValueText.text = "$sum 걸음"
        }
        else {
            summaryText.text = if(currentRange==3)"월평균" else "일평균"
            var sum = 0
            for (record in data) {
                val metricValue = record.result[metric]?.toInt()?:continue
                sum += metricValue
            }
            if(totalUnit == 0)
                summaryValueText.text = "$sum 걸음"
            else
                summaryValueText.text = "${sum/totalUnit} 걸음"
        }

    }



}