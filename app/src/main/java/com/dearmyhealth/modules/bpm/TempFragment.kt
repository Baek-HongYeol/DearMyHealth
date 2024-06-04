package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.health.connect.client.records.BodyTemperatureRecord
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

class TempFragment : VitalChartFragment<BodyTemperatureRecord>(VitalType.TEMPERATURE) {
    private val TAG = javaClass.simpleName
    override lateinit var binding: FragmentStepBinding

    val observer = Observer<List<BodyTemperatureRecord>> { value ->
        setChartData(value)
        analyzeData(value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 데이터 추가 버튼
        binding.stepAddDataLL.setOnClickListener {
            startActivity(Intent(requireContext(), TempAddActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()

        viewModel.tempRecords.observe(viewLifecycleOwner, observer)
    }

    override fun onPause() {
        super.onPause()
        fetchData()

        viewModel.tempRecords.removeObserver(observer)
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
                viewModel.readTempRecords(startOfDay, endOfDay)
            }
        }
    }
    override fun fetchDataInPeriod(period: PERIOD) {
        val startOfRange = viewModel.startOfRange.toInstant()
        val endOfRange = viewModel.endOfRange.toInstant()


        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.readTempRecords(startOfRange, endOfRange)
            }
        }
    }

    override fun configureAxis() {
        super.configureAxis()
        binding.stepBarChart.axisLeft.axisMaximum = 40f
    }


    /** 데이터 가공 및 차트 데이터 추가 **/
    override fun setChartData(dataList: List<BodyTemperatureRecord>) {

        fun generateGroups(): Map<OffsetDateTime, List<BodyTemperatureRecord>> {
            val sortedList = dataList.sortedBy { record -> record.time }

            // 하루 범위에서는 등록한 레코드 전부 표시
            if(period == PERIOD.DAY) {
                return viewModel.startOfRange.run {
                    val map = mutableMapOf<OffsetDateTime, MutableList<BodyTemperatureRecord>>()
                    for(el in sortedList) {
                        map[
                            OffsetDateTime.ofInstant(el.time, ZoneId.systemDefault())
                        ] = mutableListOf(el)
                    }
                    map
                }
            }
            // 1주일, 1달 범위에서는 하루 평균을 표시
            if(period == PERIOD.WEEK || period == PERIOD.MONTH)
                return dataList.groupBy {
                    OffsetDateTime.ofInstant(
                        it.time, ZoneId.systemDefault()
                    ).truncatedTo(ChronoUnit.DAYS) }

            // 1년 범위에서는 1달 평균을 표시
            else
                return viewModel.startOfRange.run {
                    val map = mutableMapOf<OffsetDateTime, MutableList<BodyTemperatureRecord>>()
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
                (acc + (record.temperature.inCelsius.toFloat()))
            }.div(divider)
        }
        var maxValue = 40f
        val entries: MutableList<Entry> = mutableListOf()
        for (g in groups) {
            entries.add(Entry(TimeUnit.SECONDS.toHours(g.key.toEpochSecond()).toFloat(), g.value))
            if(maxValue < g.value) maxValue = g.value
        }
        val lineDataSet = LineDataSet(entries,"체온")
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
            axisLeft.axisMaximum = maxValue
            description.isEnabled = false
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }
    }

    @SuppressLint("SetTextI18n")
    fun analyzeData(dataList: List<BodyTemperatureRecord>) {
        val summaryText = binding.metricChartDataTV
        val summaryValueText = binding.chartRepresentedDataTV
        val celsiusUnit = ContextCompat.getString(requireContext(), R.string.unit_temperature)
        summaryText.text = "평균"

        var sum = 0.0
        for (record in dataList) {
            val metricValue = record.temperature.inCelsius
            sum += metricValue
        }
        if(dataList.isEmpty())
            summaryValueText.text = "0 $celsiusUnit"
        else
            summaryValueText.text = "${sum/dataList.size} $celsiusUnit"
    }

}
