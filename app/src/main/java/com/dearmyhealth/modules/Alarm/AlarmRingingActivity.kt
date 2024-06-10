package com.dearmyhealth.modules.Alarm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dearmyhealth.databinding.ActivityAlarmRingingBinding
import java.time.OffsetDateTime

class AlarmRingingActivity: AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityAlarmRingingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dosageName = intent.getStringExtra("dosageName")
        val requestId = intent.getIntExtra("requestId", 0)
        binding.alarmTimeTV.text = OffsetDateTime.now().run { String.format("%02d:%02d",this.hour, this.minute) }

        binding.dosageTitleTV.text = dosageName
    }

}