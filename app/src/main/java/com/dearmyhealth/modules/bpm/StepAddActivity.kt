package com.dearmyhealth.modules.bpm

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.dearmyhealth.R
import com.dearmyhealth.databinding.ActivityStepAddDataBinding
import com.dearmyhealth.modules.healthconnect.HealthConnectAvailability
import com.dearmyhealth.modules.healthconnect.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class StepAddActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private lateinit var binding : ActivityStepAddDataBinding

    private lateinit var healthConnectManager: HealthConnectManager
    private val permissions = setOf(HealthPermission.getWritePermission(StepsRecord::class))
    private val requestPermissionsLauncher by lazy {
        registerForActivityResult(
            healthConnectManager.requestPermissionsActivityContract()
        ) { granted ->
            if (granted.containsAll(permissions)) {
                Log.d(TAG, "1 ALL PERMISSIONS GRANTED")
                // Permissions successfully granted
                permitted = true
            } else {
                // Lack of required permissions
                Log.e(TAG, "1 LACKING PERMISSIONS")
                Toast.makeText(this, "Not enough permission", Toast.LENGTH_SHORT).show()
                permitted = false
            }
        }
    }
    private var permitted = false

    // stepRecord
    private var startDateTime = Calendar.getInstance()
    private var durationTypes = listOf("시간", "분")
    private var durationIdx = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStepAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthConnectManager = HealthConnectManager(applicationContext)
        healthConnectManager.checkAvailability()

        checkAvailability()

        CoroutineScope(Dispatchers.Default).launch {
            permitted = healthConnectManager.hasAllPermissions(permissions)
        }

        binding.stepAddDateCL.setOnClickListener {
            binding.stepAddTimepicker.visibility = View.GONE
            binding.stepAddDatepicker.let {
                it.visibility = if (it.visibility==View.GONE) View.VISIBLE else View.GONE
            }
        }

        binding.stepAddTimeCL.setOnClickListener {
            binding.stepAddDatepicker.visibility = View.GONE
            binding.stepAddTimepicker.let {
                it.visibility = if (it.visibility==View.GONE) View.VISIBLE else View.GONE
            }
        }

        val datepickerHeaderID = binding.stepAddDatepicker.getChildAt(0)
            .resources.getIdentifier("date_picker_header", "id", "android")
        binding.stepAddDatepicker.findViewById<View>(datepickerHeaderID).visibility = View.GONE
        binding.stepAddDatepicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
            startDateTime.apply {
                this.set(year, month, dayOfMonth)
                binding.stepDateTV.text = "$year.${month+1}.$dayOfMonth"
            }
        }

        binding.stepAddTimepicker.setOnTimeChangedListener { _, hour, minute ->
            startDateTime.apply {
                this.set(Calendar.HOUR_OF_DAY, hour)
                this.set(Calendar.MINUTE, minute)
                val ampm = if (this.get(Calendar.AM_PM)==Calendar.AM) "오전" else "오후"
                val minuteStr = if(minute<10) "0$minute" else "$minute"
                binding.stepTimeTV.text = "$ampm $hour:$minuteStr"
            }
        }
        startDateTime.apply {
            val year = this.get(Calendar.YEAR)
            val month = this.get(Calendar.MONTH)
            val dayOfMonth = this.get(Calendar.DAY_OF_MONTH)
            binding.stepDateTV.text = "$year.${month+1}.$dayOfMonth"
            val hour = this.get(Calendar.HOUR)
            val minute = this.get(Calendar.MINUTE)
            val ampm = if (this.get(Calendar.AM_PM)==Calendar.AM) "오전" else "오후"
            val minuteStr = if(minute<10) "0$minute" else "$minute"
            binding.stepTimeTV.text = "$ampm $hour:$minuteStr"
        }

        binding.timebutton2.setOnClickListener {
            durationIdx = (durationIdx+1)%2
            binding.timebutton2.text = durationTypes[durationIdx]
        }

        binding.addbutton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val startTime = startDateTime.toInstant()
                val durationMinute = if(durationIdx==0) 60 else 1
                val endTime = startDateTime.toInstant().plusSeconds(60*durationMinute*(binding.durationEditText.text.toString().toLongOrNull()?:0 ))
                val value = binding.stepsEditText.text.toString().toLong()
                try{
                    healthConnectManager.writeStepsRecord(value, startTime, endTime)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@StepAddActivity, "Successfully insert records", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
                catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@StepAddActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.cancelbutton.setOnClickListener {
            finish()
        }
    }

    private fun checkAvailability() {
        Log.d(TAG, "availability: ${healthConnectManager.availability.value}")
        when (healthConnectManager.availability.value) {
            HealthConnectAvailability.NOT_INSTALLED ->
                AlertDialog.Builder(this)
                    .setMessage("헬스 커넥트가 설치되어 있지 않습니다.\n설치하기 위해 스토어로 가시겠습니까?")
                    .setNegativeButton(R.string.cancel) { _, _, -> finish() }
                    .setPositiveButton("이동") { _, _ ->
                        // Optionally redirect to package installer to find a provider, for example:
                        startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setPackage("com.android.vending")
                                data = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter(
                                        "id",
                                        getString(R.string.health_connect_package)
                                    )
                                    .appendQueryParameter("url", "healthconnect://onboarding")
                                    .build()
                                putExtra("overlay", true)
                                putExtra("callerId", packageName)
                            }
                        )
                        return@setPositiveButton
                    }.show()

            HealthConnectAvailability.NOT_SUPPORTED -> {
                AlertDialog.Builder(this)
                    .setMessage("헬스 커넥트가 지원되지 않는 기기입니다.")
                    .show()
                finish()
            }

            else -> {
                CoroutineScope(Dispatchers.Default).launch {
                    permitted = healthConnectManager.hasAllPermissions(permissions)
                }

                if (!(permitted)) {
                    Log.d(TAG, "requestPermissionLauncher")
                    requestPermissionsLauncher.launch(permissions)
                }
            }
        }
    }
}