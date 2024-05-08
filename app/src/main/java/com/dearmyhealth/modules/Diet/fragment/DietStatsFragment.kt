package com.dearmyhealth.modules.Diet.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.R
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.databinding.FragmentDietStatsBinding
import com.dearmyhealth.modules.Diet.viewmodel.DietStatsViewModel
import com.dearmyhealth.modules.Diet.model.DietStats
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.collections.LinkedHashMap


class DietStatsFragment : Fragment() {
    private val TAG = "DietStatsFragment"

    private lateinit var binding : FragmentDietStatsBinding

    private lateinit var viewModel: DietStatsViewModel
    private var currentRange = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDietStatsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this,
            DietStatsViewModel.Factory(requireContext())
        )[DietStatsViewModel::class.java]

        observeViewModel()
        viewModel.selectRange(0)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.setRangedData(viewModel.startOfRange, viewModel.endOfDay)
        }
        configureAxis()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.ranges.observe(viewLifecycleOwner) { ranges -> listPeriodLayout(ranges) }

        viewModel.currentPeriodText.observe(viewLifecycleOwner) { str ->
            binding.chartPeriodTV.text = str
        }

        viewModel.data.observe(viewLifecycleOwner) { dataList ->
            Log.d(TAG, "data size: ${dataList.size}")
            setChartData(dataList)
        }
    }

    private fun listPeriodLayout(list: List<DietStats.PERIOD>) {
        val parent = binding.chartPeriodLL
        parent.removeAllViews()
        list.forEachIndexed { index, period ->
            val view = TextView(context)
            view.text = period.displayName
            view.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f)
            view.setOnClickListener {
                parent.children.toList().getOrNull(currentRange)?.run {
                    background = null
                }
                currentRange = index
                viewModel.selectRange(index)
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.setRangedData(viewModel.startOfRange, viewModel.endOfDay)
                }
                parent.children.toList().getOrNull(viewModel.currentPosition)?.run {
                    background = ContextCompat.getDrawable(context, R.drawable.shape_radius_square_stroke_r5dp)
                }
            }
            parent.addView(view)
        }
        val ranges = binding.chartPeriodLL.children.toList()
        ranges.getOrNull(viewModel.currentPosition)?.run {
            background =
                ContextCompat.getDrawable(context, R.drawable.shape_radius_square_stroke_r5dp)
        }
    }

    private fun configureAxis() {
        val xAxis = binding.todayDietLineChart.xAxis
        val startMillis = viewModel.startOfRange.toInstant().toEpochMilli()
        val endMillis = viewModel.endOfDay.toInstant().toEpochMilli()
        val range = viewModel.ranges.value!![currentRange]
        when(currentRange) {
            0 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(7, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            1 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(4, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            2 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(12, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            else -> {
                // 1개월 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(12, false) // X축에 표시할 눈금 수를 설정합니다.
            }
        }
        xAxis.apply {
            setDrawGridLines(true)
            setDrawAxisLine(true)
            setDrawLabels(true)
            setCenterAxisLabels(false)
            position = XAxis.XAxisPosition.TOP
            valueFormatter = XAxisCustomFormatter(viewModel.currentPosition)
            textColor = resources.getColor(R.color.black, null)
            textSize = 10f
            labelRotationAngle = 0f
            axisMinimum = TimeUnit.MILLISECONDS.toMinutes(startMillis).toFloat()
            axisMaximum = TimeUnit.MILLISECONDS.toMinutes(endMillis).toFloat()
            granularity = range.unit.duration.toMinutes().toFloat()
            isGranularityEnabled = true

        }
        binding.todayDietLineChart.axisRight.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }
        binding.todayDietLineChart.axisLeft.apply {
            setDrawLabels(true)
            setDrawGridLines(true)
            setDrawAxisLine(true)
        }

    }

    private fun setChartData(dataList: List<Diet>) {

        fun generateGroups(dataList: List<Diet>): Map<OffsetDateTime, List<Diet>> {
            val range = viewModel.ranges.value!![currentRange]
            val sortedList = dataList.sortedBy { diet -> diet.time }

            if(range<=DietStats.PERIOD.MONTH)
                return dataList.groupBy {
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(it.time), ZoneId.systemDefault()
                    ).truncatedTo(ChronoUnit.DAYS) }

            else
                return viewModel.startOfRange.run {
                val map = LinkedHashMap<OffsetDateTime, MutableList<Diet>>()
                var cur = this
                var next = this.plus(1, range.unit)
                for (el in sortedList) {
                    if(el.time < this.toInstant().toEpochMilli())
                        continue

                    while(el.time >= next.toInstant().toEpochMilli()) {
                        if(next.isAfter(viewModel.endOfDay)) break
                        cur = next
                        next = next.plus(1, range.unit)
                    }
                    if( el.time >= cur.toInstant().toEpochMilli() &&
                        el.time < next.toInstant().toEpochMilli() ){
                        if(!map.contains(cur))
                            map[cur] = mutableListOf()
                        val dietlist = map[cur]
                        dietlist!!.add(el)
                        map[cur] = dietlist
                    }
                    else {
                        if(next.isAfter(viewModel.endOfDay)) break
                        cur = next
                        next = next.plus(1, range.unit)
                    }
                }
                Log.d(TAG, map.toString())
                map
            }
        }

        val groups = generateGroups(dataList).mapValues { entry ->
            entry.value.fold(0f) { acc, diet -> (acc + (diet.calories?.toFloat()?:0f)) }
        }
        val entries: MutableList<Entry> = mutableListOf()
        for (g in groups) {
            entries.add(Entry(TimeUnit.MILLISECONDS.toMinutes(g.key.toInstant().toEpochMilli()).toFloat(), g.value))
        }
        val lineDataSet = LineDataSet(entries,"calories")
        lineDataSet.apply {
            color = resources.getColor(R.color.black, null)
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
        configureAxis()

        val lineChart = binding.todayDietLineChart
        lineChart.apply {
            data = LineData(lineDataSet)
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }

    }
}

class XAxisCustomFormatter(private val range: Int) : IndexAxisValueFormatter() {
    override fun getFormattedValue(minutes: Float): String {
        val datetime = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(TimeUnit.MINUTES.toMillis(minutes.toLong())),
            ZoneId.systemDefault()
        )
        val WEEK_FORMATTER = DateTimeFormatter.ofPattern("d", Locale.getDefault())
        val ThreeMONTH_FORMATTER = DateTimeFormatter.ofPattern("M월-W주", Locale.getDefault())
        val YEAR_FORMATTER = DateTimeFormatter.ofPattern("M월", Locale.getDefault())
        val dtf = when(range) {
            0 -> WEEK_FORMATTER
            1 -> WEEK_FORMATTER
            2 -> ThreeMONTH_FORMATTER
            else -> YEAR_FORMATTER
        }
        return dtf.format(datetime)
    }
}