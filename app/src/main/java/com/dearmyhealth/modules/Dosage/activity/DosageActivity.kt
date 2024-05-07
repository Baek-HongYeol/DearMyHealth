package com.dearmyhealth.modules.Dosage.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.databinding.ActivityDosageBinding
import com.dearmyhealth.modules.Dosage.viewmodel.DosageViewModel
import java.text.SimpleDateFormat
import java.util.*

class DosageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDosageBinding
    private val dosageViewModel: DosageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*binding.saveButton.setOnClickListener {
            try {
                val medicationId = binding.medIdInput.text.toString().toInt()
                val userId = binding.userInput.text.toString().toInt()
                val name = binding.nameInput.text.toString()
                val startTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(binding.startDateInput.text.toString())?.time ?: error("Invalid start date")
                val endTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(binding.endDateInput.text.toString())?.time ?: error("Invalid end date")
                val dosage = binding.dosageInput.text.toString().toDoubleOrNull()
                val memo = binding.memoInput.text.toString()
                val insertedTime = System.currentTimeMillis()
                val dosageTime = startTime

                val dosageEntity = Dosage(
                    dosageId = 0,
                    insertedTime = insertedTime,
                    medicationId = medicationId,
                    user = userId,
                    name = name,
                    startTime = startTime,
                    endTime = endTime,
                    dosageTime = dosageTime,
                    dosage = dosage,
                    memo = memo
                )
                dosageViewModel.insertDosage(dosageEntity)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }*/
    }
}