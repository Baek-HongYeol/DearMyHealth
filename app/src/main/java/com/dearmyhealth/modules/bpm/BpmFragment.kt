package com.dearmyhealth.modules.bpm

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.HeartRateRecord
import androidx.lifecycle.Observer
import com.dearmyhealth.R
import com.dearmyhealth.databinding.FragmentBpmBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

class BpmFragment : VitalChartFragment<HeartRateRecord>(VitalType.BPM) {
    private val TAG = javaClass.simpleName

    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var minBpmTextView: TextView
    private lateinit var avgBpmTextView: TextView
    private lateinit var maxBpmTextView: TextView
    lateinit var _binding: FragmentBpmBinding


    var metric: AggregateMetric<Number> = HeartRateRecord.BPM_AVG

    // Steps Livedata Observer 정의
    val observer = Observer<List<HeartRateRecord>> { value ->
        Log.d(TAG, "livedata change observed")
        Log.d(TAG, "size: ${value.size}")

        setChartData(value)
        analyzeData(
            value,
            period.subunit.between(viewModel.startOfRange, viewModel.endOfRange).toInt()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBpmBinding.inflate(inflater)
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
        viewModel.currentDate = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        Log.d(TAG, "today: ${viewModel.currentDate}")
        currentRange = 0

        // 버튼 클릭 시 전날의 심박수 데이터 가져오기
        previousDayButton.setOnClickListener {
            viewModel.currentDate = viewModel.currentDate.minus(1, ChronoUnit.DAYS)
            Log.d(TAG, "currentDate: ${viewModel.currentDate}")
            // 다음 날짜 버튼 활성화
            nextDayButton.isEnabled = true
            updateDateText(viewModel.currentDate)
            fetchDataForDay(viewModel.currentDate)
        }

        // 버튼 클릭 시 다음날의 심박수 데이터 가져오기
        nextDayButton.setOnClickListener {
            viewModel.currentDate = viewModel.currentDate.plus(1, ChronoUnit.DAYS)

            // 오늘이 아닌 경우만 다음 날짜 버튼 비활성화
            nextDayButton.isEnabled = viewModel.currentDate.truncatedTo(ChronoUnit.DAYS)
                .isBefore(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))
            updateDateText(viewModel.currentDate)
            fetchDataForDay(viewModel.currentDate)
        }
        nextDayButton.isEnabled = false


        // 초기 데이터 가져오기
        updateDateText(viewModel.currentDate)

        fetchDataForDay(viewModel.currentDate)
        observeData()
    }

    fun observeData(){
        viewModel.bpmSeries.observe(viewLifecycleOwner) { value ->
            Log.d(TAG, "data.size: ${value.size}")
            setChartData(value)
            displayHeartRateStats(value)
        }
    }

    fun updateDateText(date: OffsetDateTime) {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.getDefault())
        _binding.currentDateTextView.text = dateFormat.format(date)
    }

    override fun fetchDataForDay(date: OffsetDateTime) {
        val start = date.truncatedTo(ChronoUnit.DAYS)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.tryWithPermissionsCheck {
                viewModel.readHeartRateRecordsForDay(start.toInstant())
            }
        }
    }

    override fun configureAxis() {
        val xAxis = _binding.barChart.xAxis
        val startMillis = viewModel.startOfRange.toInstant().toEpochMilli()
        val endMillis = viewModel.endOfRange.toInstant().toEpochMilli()

        // 2시간 단위로 눈금을 추가합니다.
        xAxis.setLabelCount(12, false) // X축에 표시할 눈금 수를 설정합니다.
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
        _binding.barChart.axisRight.apply {
            setDrawLabels(false)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }
        _binding.barChart.axisLeft.apply {
            setDrawLabels(true)
            setDrawGridLines(true)
            setDrawAxisLine(true)
            axisMinimum = 0f
            axisMaximum = 100f
        }

    }

    override fun setChartData(dataList: List<HeartRateRecord>) {
        for (record in dataList){
            val samples = record.samples
            Log.d(TAG, "startTime: ${record.startTime}")
            for(sample in samples) {
            //    Log.d(TAG, "sample: ${sample.beatsPerMinute} / time: ${sample.time.atOffset(OffsetDateTime.now().offset)}")
            }
        }

        fun generateGroups(): Map<OffsetDateTime, List<HeartRateRecord>> {
            val sortedList = dataList.sortedBy { record -> record.startTime }

            return viewModel.startOfRange.run {
                val map = mutableMapOf<OffsetDateTime, MutableList<HeartRateRecord>>()
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
                        Log.d(TAG, "start: ${el.startTime}")
                        Log.d(TAG, "middle: ${middle.toInstant()}")
                        Log.d(TAG, "end: ${el.endTime}")
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
            entry.value.fold(0f) { acc, record -> (acc + (record.samples.fold(0f){ _acc, sample -> _acc + sample.beatsPerMinute.toFloat()})) }
        }
        Log.d(TAG, "groups: ${generateGroups()}")
        var maxValue = 100f
        val entries: MutableList<BarEntry> = mutableListOf()

        for (g in groups) {
            entries.add(BarEntry(TimeUnit.SECONDS.toHours(g.key.toEpochSecond()).toFloat(), g.value))
            if(maxValue < g.value) maxValue = g.value
        }

        val barDataSet = BarDataSet(entries,"심박수")
        barDataSet.apply {
            color = resources.getColor(R.color.primary, null)
            setDrawValues(true) // 숫자표시
            valueTextColor = resources.getColor(R.color.black, null)
            valueFormatter = DefaultValueFormatter(0)  // 소숫점 자릿수 설정
            valueTextSize = 10f
        }
        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        combinedData.barData.barWidth = 0.6f

        configureAxis()

        val barChart = _binding.barChart
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
    fun analyzeData(data: List<HeartRateRecord>, totalUnit: Int) {
        val minTV = _binding.minBpmTextView
        val avgTV = _binding.minBpmTextView
        val maxTV = _binding.minBpmTextView



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