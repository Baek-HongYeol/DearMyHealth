package com.dearmyhealth.modules.bpm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentBpmBinding
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class BpmFragment : VitalChartFragment<HeartRateRecord>(VitalType.BPM) {

    private lateinit var graphView: GraphView
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var minBpmTextView: TextView
    private lateinit var avgBpmTextView: TextView
    private lateinit var maxBpmTextView: TextView
    lateinit var _binding: FragmentBpmBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBpmBinding.inflate(inflater)
        graphView = _binding.graphView
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previousDayButton = view.findViewById<Button>(R.id.previousDayButton)
        val nextDayButton = view.findViewById<Button>(R.id.nextDayButton)
        minBpmTextView = view.findViewById(R.id.minBpmTextView)
        avgBpmTextView = view.findViewById(R.id.avgBpmTextView)
        maxBpmTextView = view.findViewById(R.id.maxBpmTextView)
        // HealthConnectClient 초기화

        // 오늘 날짜
        viewModel.currentDate = OffsetDateTime.now()

        // 버튼 클릭 시 전날의 심박수 데이터 가져오기
        previousDayButton.setOnClickListener {
            viewModel.currentDate = viewModel.currentDate.minus(1, ChronoUnit.DAYS)

            // 다음 날짜 버튼 활성화
            nextDayButton.isEnabled = true
            updateDateText(viewModel.currentDate.toInstant())
        }

        // 버튼 클릭 시 다음날의 심박수 데이터 가져오기
        nextDayButton.setOnClickListener {
            viewModel.currentDate = viewModel.currentDate.plus(1, ChronoUnit.DAYS)

            // 오늘이 아닌 경우만 다음 날짜 버튼 비활성화
            nextDayButton.isEnabled = !viewModel.currentDate.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))
            updateDateText(viewModel.currentDate.toInstant())
        }
        nextDayButton.isEnabled = false


        initializeGraph()

        // 초기 데이터 가져오기
        updateDateText(viewModel.currentDate.toInstant())

        fetchDataForDay(viewModel.currentDate.toInstant())
        observeData()
    }

    fun observeData(){
        viewModel.bpmSeries.observe(viewLifecycleOwner) { value ->
            drawGraph(value)
            displayHeartRateStats(value)
        }
    }

    fun updateDateText(date: Instant) {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        _binding.currentDateTextView.text = dateFormat.format(Date.from(date))
    }

    private fun fetchDataForDay(date: Instant) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 선택한 날짜의 심박수 데이터를 가져오는 비동기 작업
                viewModel.tryWithPermissionsCheck { viewModel.readHeartRateRecordsForDay(date) }

            } catch (e: Exception) {
                // 예외 처리
                // 데이터를 가져오는 동안 오류가 발생한 경우 처리할 내용을 여기에 추가하세요.
            }
        }
    }

    fun initializeGraph() {

        // X축의 시간 간격을 설정합니다.
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.0)
        graphView.viewport.setMaxX(24.0) // 24시간으로 설정합니다.

        // Y축의 심박수 범위를 설정합니다.
        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(180.0) // 최대 심박수를 180으로 설정합니다.

        // Y축의 눈금 간격을 설정합니다.
        graphView.gridLabelRenderer.numVerticalLabels = 7 // Y축에 표시할 눈금 수를 설정합니다.

        // 1시간 단위로 눈금을 추가합니다.
        graphView.gridLabelRenderer.numHorizontalLabels = 12 // X축에 표시할 눈금 수를 설정합니다.
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

        // X축과 Y축의 레이블을 추가합니다.
        graphView.gridLabelRenderer.horizontalAxisTitle = "시간"
        graphView.gridLabelRenderer.verticalAxisTitle = "심박수"
    }


    // displayHeartRateData 및 displayHeartRateStats 함수는 필요에 따라 구현하세요.
    fun drawGraph(data: List<HeartRateRecord>){
        // 그래프를 초기화합니다.
        graphView.removeAllSeries()

        // 그래프 데이터를 저장할 리스트를 생성합니다.
        val series = LineGraphSeries<DataPoint>()

        // 심박수 데이터를 그래프에 추가합니다.
        for (record in data) {
            for (sample in record.samples) {
                val timestamp = sample.time.toEpochMilli().toDouble() // 샘플의 시간을 가져옵니다.
                val heartRate = sample.beatsPerMinute.toDouble() // 샘플의 심박수를 가져옵니다.
                val dataPoint = DataPoint(timestamp, heartRate)
                series.appendData(dataPoint, true, data.size) // 그래프 데이터를 추가합니다.
            }
        }

        // 그래프에 시리즈를 추가합니다.
        graphView.addSeries(series)
    }

    fun displayHeartRateStats(heartRateData: List<HeartRateRecord>) {
        // 최소, 평균, 최대 심박수를 계산하여 텍스트뷰에 표시합니다.
        val minBpm: Long = heartRateData
            .flatMap { it.samples.mapNotNull { sample -> sample.beatsPerMinute } } // 모든 샘플의 심박수를 추출하여 하나의 리스트로 만듭니다.
            .minOrNull() ?: 0L // 리스트에서 최소값을 찾습니다. 값이 없으면 기본값으로 0L을 반환합니다.
        val avgBpm = heartRateData.flatMap { it.samples.map { it.beatsPerMinute } }.average().toLong()
        val maxBpm: Long = heartRateData
            .flatMap { it.samples.mapNotNull { sample -> sample.beatsPerMinute } } // 모든 샘플의 심박수를 추출하여 하나의 리스트로 만듭니다.
            .maxOrNull() ?: 0L // 리스트에서 최대값을 찾습니다. 값이 없으면 기본값으로 0L을 반환합니다.

        minBpmTextView.text = minBpm.toString()
        avgBpmTextView.text = avgBpm.toString()
        maxBpmTextView.text = maxBpm.toString()
    }
}