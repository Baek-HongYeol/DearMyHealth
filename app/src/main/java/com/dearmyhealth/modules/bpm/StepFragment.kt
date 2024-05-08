package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.Observer
import com.dearmyhealth.databinding.FragmentStepBinding
import com.dearmyhealth.modules.healthconnect.GroupedAggregationResult
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
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

class StepFragment : VitalChartFragment<GroupedAggregationResult>(VitalType.STEPS) {
    private val TAG = javaClass.simpleName
    private lateinit var binding: FragmentStepBinding
    private val viewModel: VitalViewModel by viewModels({requireParentFragment()})

    var metric: AggregateMetric<Number> = StepsRecord.COUNT_TOTAL

    private var currentRange = 0
    private val period
        get() = PERIOD.values()[currentRange]

    private var today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)

    // Steps Livedata Observer 정의
    val observer = Observer<List<GroupedAggregationResult>> { value ->
        Log.d(TAG, "livedata change observed")
        Log.d(TAG, "size: ${value.size}")
        val list = mutableListOf<GroupedAggregationResult>()
        for (record in value) {
            list.add(GroupedAggregationResult(
                record.result,
                record.startTime,
                record.endTime
            ))
        }

        drawGraph(list)
        analyzeData(list, period.subunit.between(viewModel.startOfRange, viewModel.endOfDay).toInt())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStepBinding.inflate(inflater)
        viewModel.endDateOfRange = today

        setRangeText()
        initializeGraph()

        // 차트 기간 선택
        binding.stepRangeDay.setOnClickListener {
            currentRange = 0
            viewModel.currentRange = period
            setRangeText()
            fetchDataForDay()
        }
        binding.stepRangeWeek.setOnClickListener {
            currentRange = 1
            viewModel.currentRange = period
            setRangeText()
            fetchDataInPeriod(PERIOD.WEEK)
        }
        binding.stepRangeMonth.setOnClickListener {
            currentRange = 2
            viewModel.currentRange = period
            setRangeText()
            fetchDataInPeriod(PERIOD.MONTH)
        }
        binding.stepRangeYear.setOnClickListener {
            currentRange = 3
            viewModel.currentRange = period
            setRangeText()
            fetchDataInPeriod(PERIOD.YEAR)
        }

        // 데이터 추가 버튼
        binding.stepAddDataLL.setOnClickListener {
            startActivity(Intent(requireContext(), StepAddActivity::class.java))
        }

        binding.nextDayButton.setOnClickListener {
            if (viewModel.endDateOfRange.truncatedTo(ChronoUnit.DAYS)
                .plus(period.unitValue, period.unit)
                .isAfter(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))
                )
            {
                it.isEnabled = false
                return@setOnClickListener
            }
            viewModel.endDateOfRange = viewModel.endDateOfRange.plus(period.unitValue, period.unit)
            // 미래 날짜 제한
            it.isEnabled = !viewModel.endDateOfRange.truncatedTo(ChronoUnit.DAYS)
                .plus(period.unitValue, period.unit)
                .isAfter(
                    OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
                )
            setRangeText()
            fetchData()
        }
        binding.prevDayButton.setOnClickListener {
            viewModel.endDateOfRange = viewModel.endDateOfRange.minus(period.unitValue, period.unit)
            binding.nextDayButton.isEnabled = true
            setRangeText()
            fetchData()
        }

        return binding.root
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
    fun fetchData(){
        if (currentRange == 0)
            fetchDataForDay(viewModel.endDateOfRange)
        else
            fetchDataInPeriod(period)
    }
    fun fetchDataForDay(date: OffsetDateTime = OffsetDateTime.now()) {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1)
        setupGraph()
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Duration.ofHours(1))
            }
        }
    }
    fun fetchDataInPeriod(period: PERIOD) {
        val endDateOfRange = viewModel.endDateOfRange
        val startOfRange = viewModel.startOfRange.toLocalDateTime()
        val endOfDay = endDateOfRange
            .withHour(23)
            .withMinute(59)
            .withSecond(59).toLocalDateTime()

        setupGraph()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfRange, endOfDay, Period.of(0,
                    if(period==PERIOD.YEAR) 1 else 0,
                    if(period==PERIOD.YEAR) 0 else 1
                ))
            }
        }
    }

    /********  setting Chart  **********/
    @SuppressLint("SetTextI18n")
    fun setRangeText() {
        val target: OffsetDateTime = viewModel.endDateOfRange
        val startOfRange = when(currentRange) {
            0 -> target.minusDays(1)
            1 -> target.minusDays(6)
            2 -> target.minusMonths(1)
            3 -> target.minusYears(1).plusMonths(1)
            else -> target.minusDays(1)
        }
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

    override fun initializeGraph() {
        val graphView = binding.graphView2

        val endOfDay = viewModel.endOfDay
        val startOfRange = viewModel.startOfRange

        // X축과 Y축의 레이블을 추가합니다.
        graphView.gridLabelRenderer.horizontalAxisTitle = "날짜"
        graphView.gridLabelRenderer.verticalAxisTitle = "걸음수"

        // Y축의 눈금 간격을 설정합니다.
        graphView.gridLabelRenderer.numVerticalLabels = 6 // Y축에 표시할 눈금 수를 설정합니다.

        // Y축의 걸음수 범위를 설정합니다.
        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(100.0) // 최대 걸음수를 500으로 설정합니다.

        // X축의 시간 범위를 설정합니다.
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(startOfRange.toInstant().toEpochMilli().toDouble())
        graphView.viewport.setMaxX(endOfDay.toInstant().toEpochMilli().toDouble()) // 내일을 Max로 설정합니다. (range의 양끝 데이터 막대가 full width를 같는 버그 존재)
        Log.d(TAG, "min: ${startOfRange.toInstant().toEpochMilli()}")
        Log.d(TAG, "max: ${endOfDay.toInstant().toEpochMilli()}")
        graphView.viewport.isScrollable = true
    }

    fun setupGraph() {
        initializeGraph()

        val graphView = binding.graphView2
        graphView.gridLabelRenderer.labelFormatter = CustomLabelFormatter.getFormatter(period)

        when(period) {
            PERIOD.DAY -> {
                graphView.gridLabelRenderer.horizontalAxisTitle = "시간"
                // 3시간 단위로 눈금을 추가합니다.
                graphView.gridLabelRenderer.numHorizontalLabels = 4 // X축에 표시할 눈금 수를 설정합니다.
            }
            PERIOD.WEEK -> {
                // 1일 단위로 눈금을 추가합니다.
                graphView.gridLabelRenderer.numHorizontalLabels = 7 // X축에 표시할 눈금 수를 설정합니다.
            }
            PERIOD.MONTH -> {
                // 1일 단위로 눈금을 추가합니다.
                graphView.gridLabelRenderer.numHorizontalLabels = 7 // X축에 표시할 눈금 수를 설정합니다.
            }
            PERIOD.YEAR -> {
                // 1개월 단위로 눈금을 추가합니다.
                graphView.gridLabelRenderer.numHorizontalLabels = 12 // X축에 표시할 눈금 수를 설정합니다.
            }
        }
    }


    override fun drawGraph(data: List<GroupedAggregationResult>) {
        val graphView = binding.graphView2

        // 그래프를 초기화합니다.
        graphView.removeAllSeries()

        // 그래프 데이터를 저장할 리스트를 생성합니다.
        val series = BarGraphSeries<DataPoint>()

        var maxValue = 0.0
        // 데이터를 그래프에 추가합니다.
        for (record in data) {
            val timestamp = record.endTime.toEpochMilli().toDouble() // 샘플의 시간을 가져옵니다.
            val metricValue = record.result[metric]?.toDouble() ?: continue // 샘플의 값을 가져옵니다.
            val dataPoint = DataPoint(timestamp, metricValue)
            series.appendData(dataPoint, true, data.size+1) // 그래프 데이터를 추가합니다.
            maxValue = if( maxValue < metricValue ) metricValue else maxValue
        }
        // 데이터 개수가 1개일 때 해당 데이터의 bar width가 full size인 버그 해결 (0인 데이터 삽입)
        if(data.size < 2 ) {
            val dataPoint =DataPoint(
                viewModel.endOfDay.toInstant().toEpochMilli().toDouble(),
                0f.toDouble()
            )
            series.appendData(dataPoint, true, data.size + 1)
            Log.d(TAG, "dataPoint: $dataPoint")
        }

        // bar의 너비를 설정합니다.
        series.spacing = 100 / period.subunit.between(viewModel.startOfRange, viewModel.endOfDay).toInt()
        if(currentRange == 1) series.spacing /= 2


        // Y축 높이를 max 값으로 맞춥니다.
        if(maxValue > 100)
            graphView.viewport.setMaxY(maxValue+100.0)

        // 그래프에 시리즈를 추가합니다.
        graphView.addSeries(series)
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
            summaryValueText.text = "${sum/totalUnit} 걸음"
        }

    }

    class CustomLabelFormatter {

        companion object {
            val DAY_FORMATTER = createFormatter(DateTimeFormatter.ofPattern("HH"))
            val WEEK_FORMATTER = createFormatter(DateTimeFormatter.ofPattern("d"))
            val MONTH_FORMATTER = createFormatter(DateTimeFormatter.ofPattern("dd"))
            val YEAR_FORMATTER = createFormatter(DateTimeFormatter.ofPattern("M"))
            fun getFormatter(period: PERIOD) : DefaultLabelFormatter {
                return when(period) {
                    PERIOD.DAY -> DAY_FORMATTER
                    PERIOD.WEEK -> WEEK_FORMATTER
                    PERIOD.MONTH -> MONTH_FORMATTER
                    PERIOD.YEAR -> YEAR_FORMATTER
                }
            }
            private fun createFormatter(dft: DateTimeFormatter): DefaultLabelFormatter {
                return object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String {
                        if (isValueX) {
                            // X축의 눈금을 시간으로 변환하여 표시합니다.
                            val day = OffsetDateTime.ofInstant(
                                Instant.ofEpochMilli(value.toLong()),
                                ZoneId.systemDefault()
                            )
                            return dft.format(day) // 시간을 전달받은 DateTimeFormatter 형식의 문자열로 변환합니다.
                        } else {
                            return super.formatLabel(value, isValueX)
                        }
                    }
                }
            }
        }
    }

}
