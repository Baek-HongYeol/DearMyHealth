package com.dearmyhealth.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.dearmyhealth.R
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class BpmFragment : Fragment() {

    private lateinit var graphView: GraphView
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var minBpmTextView: TextView
    private lateinit var avgBpmTextView: TextView
    private lateinit var maxBpmTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previousDayButton = view.findViewById<Button>(R.id.previousDayButton)
        val nextDayButton = view.findViewById<Button>(R.id.nextDayButton)
        val currentDateTextView = view.findViewById<TextView>(R.id.currentDateTextView)
        minBpmTextView = view.findViewById(R.id.minBpmTextView)
        avgBpmTextView = view.findViewById(R.id.avgBpmTextView)
        maxBpmTextView = view.findViewById(R.id.maxBpmTextView)
        // HealthConnectClient 초기화
        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

        // 오늘 날짜
        var currentDate = Instant.now()

        // 버튼 클릭 시 전날의 심박수 데이터 가져오기
        previousDayButton.setOnClickListener {
            // 전날의 날짜로 갱신
            currentDate = currentDate.minus(1, ChronoUnit.DAYS)
            fetchDataForDay(currentDate)
            // 다음 날짜 버튼 활성화
            nextDayButton.isEnabled = true
        }

        // 버튼 클릭 시 다음날의 심박수 데이터 가져오기
        nextDayButton.setOnClickListener {
            // 다음날의 날짜로 갱신
            currentDate = currentDate.plus(1, ChronoUnit.DAYS)
            fetchDataForDay(currentDate)
            // 오늘이 아닌 경우만 다음 날짜 버튼 비활성화
            nextDayButton.isEnabled = !currentDate.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))
        }

        fun updateDateText(date: Instant) {
            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            currentDateTextView.text = dateFormat.format(Date.from(date))
        }

        // 초기 데이터 가져오기
        fetchDataForDay(currentDate)
        updateDateText(currentDate)
    }

    private suspend fun readHeartRateRecordsForDay(date: Instant): List<HeartRateRecord> {
        // 날짜의 시작 시간과 종료 시간을 계산합니다.
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)

        // 시작 시간부터 종료 시간까지의 데이터를 가져옵니다.
        return withContext(Dispatchers.IO) {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
            val response = healthConnectClient.readRecords(request)
            response.records
        }
    }

    private fun fetchDataForDay(date: Instant) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // 선택한 날짜의 심박수 데이터를 가져오는 비동기 작업
                val heartRateData = readHeartRateRecordsForDay(date)
                drawHeartRateGraph(heartRateData)
                displayHeartRateStats(heartRateData)
            } catch (e: Exception) {
                // 예외 처리
                // 데이터를 가져오는 동안 오류가 발생한 경우 처리할 내용을 여기에 추가하세요.
            }
        }
    }

    // displayHeartRateData 및 displayHeartRateStats 함수는 필요에 따라 구현하세요.
    private fun drawHeartRateGraph(heartRateData: List<HeartRateRecord>) {
        // 그래프를 초기화합니다.
        graphView.removeAllSeries()

        // 그래프 데이터를 저장할 리스트를 생성합니다.
        val series = LineGraphSeries<DataPoint>()

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

        // Y축의 눈금 간격을 설정합니다.
        graphView.gridLabelRenderer.numVerticalLabels = 7 // Y축에 표시할 눈금 수를 설정합니다.

        // 심박수 데이터를 그래프에 추가합니다.
        for (record in heartRateData) {
            for (sample in record.samples) {
                val timestamp = sample.time.toEpochMilli().toDouble() // 샘플의 시간을 가져옵니다.
                val heartRate = sample.beatsPerMinute.toDouble() // 샘플의 심박수를 가져옵니다.
                val dataPoint = DataPoint(timestamp, heartRate)
                series.appendData(dataPoint, true, heartRateData.size) // 그래프 데이터를 추가합니다.
            }
        }

        // X축과 Y축의 레이블을 추가합니다.
        graphView.gridLabelRenderer.horizontalAxisTitle = "시간"
        graphView.gridLabelRenderer.verticalAxisTitle = "심박수"

        // 그래프에 시리즈를 추가합니다.
        graphView.addSeries(series)
    }

    private fun displayHeartRateStats(heartRateData: List<HeartRateRecord>) {
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