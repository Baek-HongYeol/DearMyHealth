package com.dearmyhealth.modules.bpm

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build.VERSION_CODES.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*
import com.dearmyhealth.R
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.units.Temperature
import java.time.ZonedDateTime


class FragmentDeptAddData : DialogFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var selectedDate: Calendar
    private lateinit var selectedTime: Calendar
    private lateinit var healthConnectClient: HealthConnectClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dept_add_data, container, false)

        val cancelButton = view.findViewById<Button>(R.id.cancelbutton)
        cancelButton.setOnClickListener {
            dismiss()
        }

        val selectDateButton = view.findViewById<Button>(R.id.datebutton)
        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        val selectTimeButton = view.findViewById<Button>(R.id.timebutton)
        selectTimeButton.setOnClickListener {
            showTimePickerDialog()
        }

        val addDataButton = view.findViewById<Button>(R.id.addbutton)
        addDataButton.setOnClickListener {
            if (::selectedDate.isInitialized && ::selectedTime.isInitialized) {
                // 온도 입력값 가져오기
                val temperatureEditText = view.findViewById<EditText>(R.id.temperatureEditText)
                val temperature = temperatureEditText.text.toString()

                // Health Connect에 데이터 추가
                addToHealthConnect(selectedDate, selectedTime, temperature)

                // 추가 후 팝업 닫기
                dismiss()
            } else {
                // 선택된 날짜 또는 시간이 없는 경우 사용자에게 메시지 표시
                Toast.makeText(requireContext(), "날짜와 시간을 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), this, hour, minute, false).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate = Calendar.getInstance().apply {
            set(year, month, dayOfMonth)
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        selectedTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
    }

    private fun addToHealthConnect(selectedDate: Calendar, selectedTime: Calendar, temperature: String) {
        // HealthConnectClient를 초기화합니다.
        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

        // 선택된 시간을 Instant 객체로 변환합니다.
        val instant = selectedDate.toInstant().plus(selectedTime.toInstant().epochSecond, ChronoUnit.SECONDS)
        val metadata = Metadata(
            id = "unique_id",
            dataOrigin = DataOrigin("dearmyhealth"),
            lastModifiedTime = Instant.now(),
            clientRecordId = null,
            clientRecordVersion = 1,
            device = null
        )
        // BodyTemperatureRecord를 생성합니다.
        val record = BodyTemperatureRecord(
            time = instant, // 시간 인스턴스
            zoneOffset = ZonedDateTime.now().withNano(0).offset, // ZoneOffset이 사용할 수 없으면 null 전달
            temperature = Temperature.celsius(temperature.toDouble()), // 체온 인스턴스
            measurementLocation = 8, // 체온 측정 위치(귀)
            metadata = metadata
        )

        // 코루틴 내에서 insertRecords 함수를 호출합니다.
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Health Connect API에 BodyTemperatureRecord를 추가합니다.
                val result = withContext(Dispatchers.IO) {
                    healthConnectClient.insertRecords(listOf(record))
                }
                // 성공적으로 추가된 경우 사용자에게 메시지를 표시합니다.
                Toast.makeText(requireContext(), "데이터가 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } catch (exception: Exception) {
                // 추가 중에 오류가 발생한 경우 오류 메시지를 표시합니다.
                Toast.makeText(requireContext(), "데이터 추가 중 오류가 발생했습니다: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}