package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentStepBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

open class VitalChartFragment<T>(val dataType: VitalType) : Fragment() {
    private val TAG = javaClass.simpleName
    protected open lateinit var binding: FragmentStepBinding
    protected val viewModel: VitalViewModel by viewModels({requireParentFragment()})


    var currentRange = 0
    protected val period
        get() = PERIOD.values()[currentRange]

    protected var today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStepBinding.inflate(inflater)

        currentRange = 0
        viewModel.currentRange = period
        viewModel.startOfRange = today

        setRangeText()

        // 차트 기간 선택
        binding.stepRangeDay.setOnClickListener {
            changeBackground(binding.chartPeriodLL, currentRange, 0)
            currentRange = 0
            viewModel.currentRange = period
            setRangeText()
            fetchData()
        }
        binding.stepRangeWeek.setOnClickListener {
            changeBackground(binding.chartPeriodLL, currentRange, 1)
            currentRange = 1
            viewModel.currentRange = period
            setRangeText()
            fetchData()
        }
        binding.stepRangeMonth.setOnClickListener {
            changeBackground(binding.chartPeriodLL, currentRange, 2)
            currentRange = 2
            viewModel.currentRange = period
            setRangeText()
            fetchData()
        }
        binding.stepRangeYear.setOnClickListener {
            changeBackground(binding.chartPeriodLL, currentRange, 3)
            currentRange = 3
            viewModel.currentRange = period
            setRangeText()
            fetchData()
        }

        binding.nextDayButton.setOnClickListener {
            // 유효성 확인
            if (viewModel.startOfRange.truncatedTo(ChronoUnit.DAYS)
                .plus(period.unitValue, period.unit)
                .isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))
                )
            {
                it.isEnabled = false
                return@setOnClickListener
            }
            // 날짜 변경
            viewModel.startOfRange = viewModel.startOfRange.plus(period.unitValue, period.unit)

            // 미래 날짜 제한
            it.isEnabled = !viewModel.startOfRange.truncatedTo(ChronoUnit.DAYS)
                .plus(period.unitValue, period.unit)
                .isAfter(
                    OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
                )
            setRangeText()
            fetchData()
        }
        binding.prevDayButton.setOnClickListener {
            viewModel.startOfRange = viewModel.startOfRange.minus(period.unitValue, period.unit)
            binding.nextDayButton.isEnabled = true
            setRangeText()
            fetchData()
        }
        configureAxis()

        return binding.root
    }


    /******** layout *********/
    fun changeBackground(parent: ViewGroup, oldIdx:Int, newIdx:Int) {
        parent.getChildAt(oldIdx).backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(), R.color.secondary
            ))
        parent.getChildAt(newIdx).backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(), R.color.white
            ))
    }

    /********  fetch Data  **********/
    open fun fetchData(){
        if (currentRange == 0)
            fetchDataForDay(viewModel.startOfRange)
        else
            fetchDataInPeriod(period)
    }
    open fun fetchDataForDay(date: OffsetDateTime = OffsetDateTime.now()) {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Duration.ofHours(1))
            }
        }
    }
    open fun fetchDataInPeriod(period: PERIOD) {
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
    @SuppressLint("SetTextI18n")
    fun setRangeText() {
        val target: OffsetDateTime = viewModel.endOfRange
        val startOfRange = viewModel.startOfRange
        val dtf = when (currentRange) {
            0 -> DateTimeFormatter.ofPattern("yyyy년 M월 dd일", Locale.getDefault())
            1 -> DateTimeFormatter.ofPattern("dd일", Locale.getDefault())
            2 -> DateTimeFormatter.ofPattern("M월 dd일", Locale.getDefault())
            else -> DateTimeFormatter.ofPattern("yyyy년 MM월", Locale.getDefault())
        }


        binding.currentDateTextView.text =
            if(currentRange!=0) "${startOfRange.format(dtf)} ~ ${target.format(dtf)}"
            else dtf.format(target)
    }

    open fun configureAxis() {
        val xAxis = binding.stepBarChart.xAxis
        val startMillis = viewModel.startOfRange.toInstant().toEpochMilli()
        val endMillis = viewModel.endOfRange.toInstant().toEpochMilli()
        when(currentRange) {
            0 -> {
                // 2시간 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(12, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            1 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(7, false) // X축에 표시할 눈금 수를 설정합니다.
            }
            2 -> {
                // 1일 단위로 눈금을 추가합니다.
                xAxis.setLabelCount(7, false) // X축에 표시할 눈금 수를 설정합니다.
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
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = XAxisCustomFormatter(currentRange)
            textColor = resources.getColor(R.color.black, null)
            textSize = 10f
            labelRotationAngle = 0f
            axisMinimum = TimeUnit.MILLISECONDS.toHours(startMillis).toFloat()
            axisMaximum = TimeUnit.MILLISECONDS.toHours(endMillis).toFloat()
            granularity = period.subunit.duration.toHours().toFloat()
            isGranularityEnabled = true

        }
        binding.stepBarChart.axisRight.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }
        binding.stepBarChart.axisLeft.apply {
            setDrawLabels(true)
            setDrawGridLines(true)
            setDrawAxisLine(true)
            axisMinimum = 0f
            axisMaximum = 100f
        }

    }

    /** 데이터 가공 및 차트 데이터 추가 **/
    open fun setChartData(dataList: List<T>) {
        throw NotImplementedError()
    }

    class XAxisCustomFormatter(private val range: Int) : IndexAxisValueFormatter() {
        override fun getFormattedValue(hours: Float): String {
            val datetime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(TimeUnit.HOURS.toSeconds(hours.toLong())),
                ZoneId.systemDefault()
            )
            val DAY_FORMATTER = DateTimeFormatter.ofPattern("HH", Locale.getDefault())
            val WEEK_FORMATTER = DateTimeFormatter.ofPattern("d", Locale.getDefault())
            val MONTH_FORMATTER = DateTimeFormatter.ofPattern("d", Locale.getDefault())
            val YEAR_FORMATTER = DateTimeFormatter.ofPattern("M", Locale.getDefault())
            val dtf = when(range) {
                0 -> DAY_FORMATTER
                1 -> WEEK_FORMATTER
                2 -> MONTH_FORMATTER
                else -> YEAR_FORMATTER
            }
            return dtf.format(datetime)
        }
    }
}