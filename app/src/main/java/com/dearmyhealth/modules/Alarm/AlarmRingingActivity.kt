package com.dearmyhealth.modules.Alarm

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.dearmyhealth.databinding.ActivityAlarmRingingBinding

class AlarmRingingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityAlarmRingingBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

}