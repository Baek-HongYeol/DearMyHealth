package com.dearmyhealth.modules.exercise.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentDietStatsBinding
import com.dearmyhealth.modules.Diet.model.DietStats
import com.dearmyhealth.modules.exercise.model.ExerciseSessionData
import com.dearmyhealth.modules.exercise.viewmodel.ExerciseSessionViewModel
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


class ExerciseStatsFragment : Fragment() {
    private val TAG = javaClass.simpleName

    private lateinit var binding : FragmentDietStatsBinding

    private val viewModel: ExerciseSessionViewModel by viewModels({requireParentFragment()})
    private var currentPosition = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDietStatsBinding.inflate(layoutInflater)

        viewModel.currentPosition = 0

        observeViewModel()

        setRangeText()
        fetchData()
        configureAxis()

        return binding.root
    }


    private fun observeViewModel() {
        viewModel.ranges.observe(viewLifecycleOwner) { ranges -> listPeriodLayout(ranges) }

        viewModel.currentPeriodText.observe(viewLifecycleOwner) { str ->
            binding.chartPeriodTV.text = str
        }

        viewModel.sessionDataList.observe(viewLifecycleOwner) { dataList ->
            Log.d(TAG, "data size: ${dataList.size}")
            setChartData(dataList)
        }
    }

    /******** layout *********/
    private fun changeBackground(parent: ViewGroup, oldIdx:Int, newIdx:Int) {
        parent.getChildAt(oldIdx)?.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(), R.color.secondary
            ))
        parent.getChildAt(newIdx)?.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(), R.color.white
            ))
    }

    @SuppressLint("SetTextI18n")
    private fun setRangeText() {
        val startOfRange = viewModel.startOfRange
        val target = viewModel.endOfRange
        val dtf = when (currentPosition) {
            0 -> DateTimeFormatter.ofPattern("dd일", Locale.getDefault())
            1 -> DateTimeFormatter.ofPattern("M월 dd일", Locale.getDefault())
            2 -> DateTimeFormatter.ofPattern("M월 dd일", Locale.getDefault())
            else -> DateTimeFormatter.ofPattern("yyyy년 MM월", Locale.getDefault())
        }


        binding.chartPeriodTV.text ="${startOfRange.format(dtf)} ~ ${target.format(dtf)}"
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
                changeBackground(binding.chartPeriodLL, currentPosition, index)
                currentPosition = index
                viewModel.currentPosition = index
                setRangeText()
                fetchData()
            }
            parent.addView(view)
        }
        val ranges = binding.chartPeriodLL.children.toList()
        ranges.getOrNull(viewModel.currentPosition)?.run {
            background =
                ContextCompat.getDrawable(context, R.drawable.shape_radius_square_stroke_r5dp)
        }
        currentPosition = 0
        viewModel.currentPosition = 0
        setRangeText()
        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.readExerciseSessions()
        }
    }

    private fun configureAxis() {
        val xAxis = binding.todayDietLineChart.xAxis
        val startMillis = viewModel.startOfRange.toInstant().toEpochMilli()
        val endMillis = viewModel.endOfRange.toInstant().toEpochMilli()
        val range = viewModel.ranges.value!![currentPosition]
        when(currentPosition) {
            0 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(7, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            1 -> {
                // 1주 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(4, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            2 -> {
                // 1주 단위로 눈금을 추가합니다.
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

    private fun setChartData(dataList: List<ExerciseSessionData>) {

        fun generateGroups(dataList: List<ExerciseSessionData>): Map<OffsetDateTime, List<ExerciseSessionData>> {
            val range = viewModel.ranges.value!![currentPosition]
            val sortedList = dataList.sortedBy { session -> session.startTime }

            if(range<=DietStats.PERIOD.MONTH)
                return dataList.groupBy {
                    OffsetDateTime.ofInstant(it.startTime, ZoneId.systemDefault())
                        .truncatedTo(ChronoUnit.DAYS) }

            else
                return viewModel.startOfRange.run {
                val map = LinkedHashMap<OffsetDateTime, MutableList<ExerciseSessionData>>()
                var cur = this
                var next = this.plus(1, range.unit)
                for (el in sortedList) {
                    if((el.startTime?.toEpochMilli() ?: 0) < this.toInstant().toEpochMilli())
                        continue

                    while((el.startTime?.toEpochMilli() ?: 0) >= next.toInstant().toEpochMilli()) {
                        if(next.isAfter(viewModel.endOfRange)) break
                        cur = next
                        next = next.plus(1, range.unit)
                    }
                    if((el.startTime?.toEpochMilli() ?: 0) >= cur.toInstant().toEpochMilli() &&
                        (el.startTime?.toEpochMilli() ?: 0) < next.toInstant().toEpochMilli()
                    ){
                        if(!map.contains(cur))
                            map[cur] = mutableListOf()
                        val _dataList = map[cur]
                        _dataList!!.add(el)
                        map[cur] = _dataList
                    }
                    else {
                        if(next.isAfter(viewModel.endOfRange)) break
                        cur = next
                        next = next.plus(1, range.unit)
                    }
                }
                Log.d(TAG, map.toString())
                map
            }
        }

        val groups = generateGroups(dataList).mapValues { entry ->
            entry.value.fold(0f) { acc, exSession -> (acc + (exSession.totalEnergyBurned?.inKilocalories?.toFloat()?:0f)) }
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
        val ThreeMONTH_FORMATTER = DateTimeFormatter.ofPattern("M/d ", Locale.getDefault())
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