package com.dearmyhealth.modules.Dosage.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dearmyhealth.data.Result
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.databinding.ActivityDosageBinding
import com.dearmyhealth.modules.Dosage.repository.RepositoryProvider
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

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

        binding.dosageAddStartDatepicker.init(
            startdate.year, startdate.monthValue, startdate.dayOfMonth
        )// onDateChangedListener
        { _, year, month, day  ->
            startdate = OffsetDateTime.now()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
                .truncatedTo(ChronoUnit.DAYS)
            binding.dosageStartDate.text = dtf.format(startdate)
        }

        binding.dosageAddEndDatepicker.init(
            endDate.year, endDate.monthValue, endDate.dayOfMonth
        )// onDateChangedListener
        { _, year, month, day  ->
            endDate = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).minusNanos(1)
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(day)
            binding.dosageEndDate.text = dtf.format(endDate)
        }

        // 저장 버튼 초기화
        val dosageTime = 0L
        if(operation == OPERATION.CREATE) {
            binding.saveButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    medicationItemSeq = binding.medIdInput.text.toString().trim()
                    dosageViewModel.insertDosage(
                        medicationItemSeq, binding.nameInput.text.toString().trim(),
                        startdate.toInstant().toEpochMilli(), endDate.toInstant().toEpochMilli(),
                        dosageTime, binding.dosageInput.text.toString().toDouble()
                    )
                }
            }
        }
        else if(operation == OPERATION.EDIT) {
            binding.saveButton.setOnClickListener {
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
                Log.d("DosageActivity", "dosage load completed!")
                targetDosage = dosage
                medicationItemSeq = dosage.medicationId
                medicationName = dosage.name
                startdate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(dosage.startTime), ZoneId.systemDefault())
                endDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(dosage.endTime), ZoneId.systemDefault())
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
        }
    }
}