package com.dearmyhealth.modules.bpm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.StepsRecord
import com.dearmyhealth.databinding.FragmentStepBinding
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class StepFragment : VitalChartFragment<AggregationResultGroupedByDuration>(VitalType.STEPS) {
    private lateinit var binding: FragmentStepBinding
    private val viewModel: VitalViewModel by viewModels({requireParentFragment()})

    var metric: AggregateMetric<Number> = StepsRecord.COUNT_TOTAL

    private var currentRange = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStepBinding.inflate(inflater)
        initializeGraph()

        binding.stepRangeDay.setOnClickListener {
            setupDayGraph()
            currentRange = 0
            fetchDataForDay(Instant.now())
        }
        binding.stepRangeWeek.setOnClickListener {
            setupWeekGraph()
            currentRange = 1
            fetchDataInPeriod(PERIOD.WEEK)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchDataInPeriod(PERIOD.values()[currentRange])
    }

    fun fetchDataForDay(date: Instant) {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Duration.ofHours(1))
            }
        }
    }
    fun fetchDataInPeriod(period: PERIOD) {
        val date = Instant.now()
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS).minus(1, period.unit)
        val endOfDay = date
        val groupUnit = ChronoUnit.DAYS

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.aggregateStepsIntoSlices(startOfDay, endOfDay, Period.of(
                    if(period.unit==ChronoUnit.YEARS) 1 else 0,
                    if(period.unit==ChronoUnit.MONTHS) 1 else 0,
                    if(period.unit==ChronoUnit.WEEKS) 7 else 0
                ))
            }
        }
    }

//    fun observeData(){
//        viewModel.stepsIntoDurationSlices.observe(viewLifecycleOwner) { value ->
//            drawGraph(value)
//        }
//    }

    override fun initializeGraph() {
        val graphView = binding.graphView2


        // Y축의 눈금 간격을 설정합니다.
        graphView.gridLabelRenderer.numVerticalLabels = 7 // Y축에 표시할 눈금 수를 설정합니다.

        setupDayGraph()

        // X축과 Y축의 레이블을 추가합니다.
        graphView.gridLabelRenderer.horizontalAxisTitle = "시간"
        graphView.gridLabelRenderer.verticalAxisTitle = "걸음수"
    }

    fun setupDayGraph() {
        val graphView = binding.graphView2

        // X축의 시간 간격을 설정합니다.
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.0)
        graphView.viewport.setMaxX(24.0) // 24시간으로 설정합니다.

        // Y축의 심박수 범위를 설정합니다.
        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(500.0) // 최대 걸음수를 500으로 설정합니다.

        // 1시간 단위로 눈금을 추가합니다.
        graphView.gridLabelRenderer.numHorizontalLabels = 24 // X축에 표시할 눈금 수를 설정합니다.
        graphView.gridLabelRenderer.setHorizontalLabelsAngle(90) // 눈금 라벨을 90도 회전시킵니다.
        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if (isValueX) {
                    // X축의 눈금을 시간으로 변환하여 표시합니다.
                    val hour = value.toInt() % 24
                    return String.format("%02d:00", hour) // 시간을 00:00 형식으로 변환하여 반환합니다.
                } else {
                    return super.formatLabel(value, isValueX)
                }
            }
        }
    }

    fun setupWeekGraph() {
        val graphView = binding.graphView2

        // X축의 시간 간격을 설정합니다.
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(
            Instant.now()
                .truncatedTo(ChronoUnit.DAYS)
                .minus(7, ChronoUnit.DAYS)
                .toEpochMilli().toDouble())
        graphView.viewport.setMaxX(Instant.now().toEpochMilli().toDouble()) // 오늘을 Max로 설정합니다.

        // Y축의 심박수 범위를 설정합니다.
        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(500.0) // 최대 걸음수를 500으로 설정합니다.

        // 1시간 단위로 눈금을 추가합니다.
        graphView.gridLabelRenderer.numHorizontalLabels = 7 // X축에 표시할 눈금 수를 설정합니다.
        graphView.gridLabelRenderer.setHorizontalLabelsAngle(0) // 눈금 라벨을 정방향으로 회전시킵니다.
        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if (isValueX) {
                    // X축의 눈금을 시간으로 변환하여 표시합니다.
                    val day = Instant.ofEpochSecond(value.toLong())[ChronoField.DAY_OF_MONTH]
                    return String.format("%02d", day) // 시간을 00:00 형식으로 변환하여 반환합니다.
                } else {
                    return super.formatLabel(value, isValueX)
                }
            }
        }
    }

    override fun drawGraph(data: List<AggregationResultGroupedByDuration>) {
        //val roundedBarChart: RoundedBarChart = binding.roundedBarChart
        val graphView = binding.graphView2
        var maxValue = 0.0

        // 그래프를 초기화합니다.
        graphView.removeAllSeries()

        // 그래프 데이터를 저장할 리스트를 생성합니다.
        val series = BarGraphSeries<DataPoint>()

        // 데이터를 그래프에 추가합니다.
        for (record in data) {
            val timestamp = record.endTime.toEpochMilli().toDouble() // 샘플의 시간을 가져옵니다.
            val metricValue = record.result[metric]?.toDouble() ?: continue // 샘플의 값을 가져옵니다.
            val dataPoint = DataPoint(timestamp, metricValue)
            series.appendData(dataPoint, true, data.size) // 그래프 데이터를 추가합니다.
            maxValue = if( maxValue < metricValue ) metricValue else maxValue
        }

        // Y축 높이를 max 값으로 맞춥니다.
        if(maxValue > 500)
            graphView.viewport.setMaxY(maxValue+100.0)

        // 그래프에 시리즈를 추가합니다.
        graphView.addSeries(series)
    }


}
