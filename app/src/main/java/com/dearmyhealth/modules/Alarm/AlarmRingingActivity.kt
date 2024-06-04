package com.dearmyhealth.modules.Alarm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dearmyhealth.databinding.ActivityAlarmRingingBinding

class AlarmRingingActivity: AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAlarmRingingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dosageName = intent.getStringExtra("dosageName")
        val requestId = intent.getIntExtra("requestId", 0)

        binding.dosageTitleTV.text = dosageName
    }

}