package com.dearmyhealth.modules.Diet.ui

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
import com.dearmyhealth.modules.Diet.DietStatsViewModel
import com.dearmyhealth.modules.Diet.model.DietStats
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DietStatsFragment : Fragment() {
    private val TAG = "DietStatsFragment"

    private lateinit var binding : FragmentDietStatsBinding

    private lateinit var viewModel: DietStatsViewModel
    private var enabledRangePosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDietStatsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this,
            DietStatsViewModel.Factory(requireContext())
        )[DietStatsViewModel::class.java]

        observeViewModel()
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.selectRange(0)
        }
        configureAxis()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.ranges.observe(viewLifecycleOwner) { ranges -> listPeriodLayout(ranges) }

        viewModel.currentPeriod.observe(viewLifecycleOwner) { str ->
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
                parent.children.toList().getOrNull(enabledRangePosition)?.run {
                    background = null
                }
                enabledRangePosition = index
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.selectRange(index)
                    withContext(Dispatchers.Main) {
                        parent.children.toList().getOrNull(viewModel.currentPosition)?.run {
                            background = ContextCompat.getDrawable(context, R.drawable.shape_radius_square_stroke_r5dp)
                        }
                    }
                }
                // TODO 로딩 창 만들기
            }
            parent.addView(view)
        }
        val ranges = binding.chartPeriodLL.children.toList()
        ranges.getOrNull(enabledRangePosition)?.run {
            background = null
        }
        ranges.getOrNull(viewModel.currentPosition)?.run {
            background = ContextCompat.getDrawable(context, R.drawable.shape_radius_square_stroke_r5dp)
        }
        Log.d(TAG, "2's background: ${ranges.get(2).background}")
    }

    private fun configureAxis() {
        val xAxis = binding.todayDietLineChart.xAxis
        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            position = XAxis.XAxisPosition.TOP
            valueFormatter = XAxisCustomFormatter(viewModel.changeDateText())
            textColor = resources.getColor(R.color.black, null)
            textSize = 10f
            labelRotationAngle = 0f
            setLabelCount(5, false)
        }
        binding.todayDietLineChart.axisRight.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }
        binding.todayDietLineChart.axisLeft.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

    }

    private fun setChartData(dataList: List<Diet>) {
        val entries: MutableList<Entry> = mutableListOf()
        for (i in dataList.indices){
            dataList[i].calories.run {
                entries.add(Entry(i.toFloat(), this?.toFloat()?:0f))
            }
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
            legend.orientation = Legend.LegendOrientation.VERTICAL
            notifyDataSetChanged() //데이터 갱신
            invalidate() // view갱신
        }

    }
}

class XAxisCustomFormatter(val xAxisData: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return xAxisData[(value).toInt()]
    }
}