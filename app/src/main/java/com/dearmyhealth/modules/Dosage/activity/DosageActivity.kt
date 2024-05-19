package com.dearmyhealth.modules.Dosage.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.R
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.databinding.ActivityDosageBinding
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class OPERATION {
    CREATE,
    EDIT
}

class DosageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDosageBinding
    private lateinit var dosageViewModel: DosageViewModel
    var medicationName = ""
    var medicationItemSeq = ""
    var startdate = OffsetDateTime.now()
    var endDate = OffsetDateTime.now().plusHours(24)
    lateinit var operation :OPERATION
    var targetDosage: Dosage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosageBinding.inflate(layoutInflater)
        dosageViewModel = ViewModelProvider(this, DosageViewModelFactory(RepositoryProvider.dosageRepository))[DosageViewModel::class.java]

        // 수정 혹은 생성 기능 구분
        try{
            operation = OPERATION.valueOf(intent.getStringExtra("operation")?:"")
        }catch(e: IllegalArgumentException){
            Log.e("DosageActivity","Dosage data operation not defined for Activity Intent.")
            finish()
        }

        getArguments(operation)

        // 날짜 선택 UI 초기화
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        binding.dosageStartDate.text = dtf.format(startdate)
        binding.dosageEndDate.text = dtf.format(endDate)

        binding.dosageStartDateLL.setOnClickListener {
            binding.dosageAddStartDatepicker.let {
                if(it.visibility == View.VISIBLE)
                    it.visibility =  View.GONE
                else it.visibility = View.VISIBLE
                binding.dosageAddEndDatepicker.visibility = View.GONE
            }
        }
        binding.dosageEndDateLL.setOnClickListener {
            binding.dosageAddEndDatepicker.let {
                if(it.visibility == View.VISIBLE)
                    it.visibility =  View.GONE
                else it.visibility = View.VISIBLE
                binding.dosageAddStartDatepicker.visibility = View.GONE
            }
        }
        binding.dosageAddStartDatepicker.init(
            startdate.year, startdate.monthValue-1, startdate.dayOfMonth
        )// onDateChangedListener
        { _, year, month, day  ->
            startdate = OffsetDateTime.now()
                .withYear(year)
                .withMonth(month+1)
                .withDayOfMonth(day)
                .truncatedTo(ChronoUnit.DAYS)
            binding.dosageStartDate.text = dtf.format(startdate)
        }

        binding.dosageAddEndDatepicker.init(
            endDate.year, endDate.monthValue-1, endDate.dayOfMonth
        )// onDateChangedListener
        { _, year, month, day  ->
            endDate = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).minusNanos(1)
                .withYear(year)
                .withMonth(month+1)
                .withDayOfMonth(day)
            binding.dosageEndDate.text = dtf.format(endDate)
        }

        binding.dosageTimeAddButton.setOnClickListener {
            showTimePickerDialog()
        }

        initSaveButton()

        dosageViewModel.dosageTimeList.observe(this) { times ->
            generateTimeChips(times)
        }
        // 생성 결과 확인
        dosageViewModel.addResult.observe(this) { result ->
            if(result == null)
                return@observe
            if(result is Result.Success){
                Toast.makeText(this, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            else {
                Toast.makeText(this, "복약 정보를 추가하지 못했습니다.", Toast.LENGTH_LONG).show()
                Log.e("DosageActivity", "dosage insertion failed: ${(result as Result.Error).exception}")
            }
        }

        setContentView(binding.root)
    }

    /** 생성 혹은 수정 작업에 따른 정보 가져오기 **/
    private fun getArguments(operation: OPERATION) {
        // 수정 시 dosage ID를 통해 기존 dosage load.
        if (operation == OPERATION.EDIT) {
            val dosageId = intent.getIntExtra("dosageId", 0)
            if (dosageId==0){
                Log.e("DosageActivity", "Dosage edit data is not transferred from Intent.")
                Toast.makeText(this, "복약 정보가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            dosageViewModel.getResult.observe(this) { dosage ->
                if(dosage==null){
                    Log.e("DosageActivity", "non-existing id is passed.")
                    Toast.makeText(this, "non-existing id is passed.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Log.d("DosageActivity", "dosage load completed!")
                targetDosage = dosage
                medicationItemSeq = dosage.medicationId
                medicationName = dosage.name
                startdate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(dosage.startTime), ZoneId.systemDefault())
                endDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(dosage.endTime), ZoneId.systemDefault())
                dosageViewModel.dosageTimeList.value = dosage.dosageTime.map { minutes ->
                    OffsetTime.of(minutes.toInt()/60, minutes.toInt()%60,
                        0,0, OffsetTime.now().offset)
                }.toMutableList()

                binding.nameInput.apply {
                    setText(medicationName)
                    isEnabled = false
                }
                binding.medIdInput.apply {
                    setText(medicationItemSeq)
                    isEnabled = false
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                dosageViewModel.getDosage(dosageId)
            }
        }
        // 생성 시 intent를 통해 약물 이름과 번호 획득
        else {
            medicationName = intent.getStringExtra("medName") ?: ""
            medicationItemSeq = intent.getStringExtra("medItemSeq") ?: ""
            if(medicationItemSeq != "") {
                binding.nameInput.apply {
                    setText(medicationName)
                    isEnabled = false
                }
                binding.medIdInput.apply {
                    setText(medicationItemSeq)
                    isEnabled = false
                }
            }
            else {
                binding.medIdInput.setText("직접 추가한 약물은 주의사항 열람을 확인할 수 없습니다.")
                binding.medIdInput.isEnabled = false
            }
        }
    }

    private fun initSaveButton() {
        // 저장 버튼 초기화
        binding.saveButton.setOnClickListener {
            val dosageTime = dosageViewModel.dosageTimeList.value!!.map { time -> TimeUnit.HOURS.toMinutes(time.hour.toLong())+time.minute }

            if(operation == OPERATION.CREATE) {
                CoroutineScope(Dispatchers.IO).launch {
                    if((intent.getStringExtra("medItemSeq") ?: "") == "")
                        medicationItemSeq = "custom"
                    else
                        medicationItemSeq = binding.medIdInput.text.toString().trim()
                    dosageViewModel.insertDosage(
                        medicationItemSeq, binding.nameInput.text.toString().trim(),
                        startdate.toInstant().toEpochMilli(), endDate.toInstant().toEpochMilli(),
                        dosageTime, binding.dosageInput.text.toString().toDouble()
                    )
                }
            }
            else if(operation == OPERATION.EDIT) {
                targetDosage?.let {
                    val updated = Dosage(
                        it.dosageId,
                        it.insertedTime,
                        binding.medIdInput.text.toString().trim(),
                        it.user,
                        binding.nameInput.text.toString().trim(),
                        startdate.toInstant().toEpochMilli(),
                        endDate.toInstant().toEpochMilli(),
                        dosageTime,
                        binding.dosageInput.text.toString().toDouble(),
                        ""
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            dosageViewModel.updateDosage(updated)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@DosageActivity, "수정이 완료되었습니다.",Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        catch(e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@DosageActivity, "저장하지 못했습니다.",Toast.LENGTH_LONG).show()
                                Log.e("DosageActivity", "dosage edition failed: ${e.localizedMessage}")
                            }
                        }
                    }
                }
            }
        }
    }

    fun showTimePickerDialog() {
        val now = OffsetTime.now()
        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(now.hour)
            .setMinute(now.minute)
            .setPositiveButtonText(R.string.save)
            .setNegativeButtonText(R.string.cancel)
            .build()
        timePickerDialog.addOnPositiveButtonClickListener {
            val hour = timePickerDialog.hour
            val minute = timePickerDialog.minute
            val offsetTime = OffsetTime.of(hour, minute, 0, 0, OffsetTime.now().offset)
            val list = dosageViewModel.dosageTimeList.value!!
            list.add(offsetTime)
            list.sort()
            dosageViewModel.dosageTimeList.value = list
        }
        timePickerDialog.show(supportFragmentManager, "tag")
    }

    private fun generateTimeChips(times: List<OffsetTime>?) {
        // 시간을 각각 하나의 chip으로 표현
        val chips = binding.chipGroup
        chips.removeAllViews()
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        if (times == null) return
        for (time in times) {
            val chip = makeChip(chips, time)
            chip.text = timeFormatter.format(time)
            chips.addView(chip)
        }
    }

    private fun makeChip(parent:ChipGroup, offsetTime: OffsetTime): Chip {
        val backgroundColor = ColorStateList.valueOf(resources.getColor(R.color.secondary, theme))

        val chip = Chip(this).apply{
            isCheckable = false
            isCloseIconVisible = true
            chipBackgroundColor = backgroundColor
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
            setOnCloseIconClickListener { v ->
                dosageViewModel.dosageTimeList.value!!.remove(offsetTime)
                parent.removeView(v);
            }
        }

        return chip
    }
}