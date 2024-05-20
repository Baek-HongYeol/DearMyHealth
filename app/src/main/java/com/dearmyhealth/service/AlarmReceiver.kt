package com.dearmyhealth.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.dearmyhealth.R
import com.dearmyhealth.modules.Alarm.AlarmRepository
import com.dearmyhealth.modules.Alarm.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        // TODO("AlarmReceiver.onReceive() is not implemented")

        Log.d("AlarmReceiver", "Alarm Receiver executed!")
        setNotification(context, intent)

        // setSoundOn(context, intent)
    }

    private fun setNotification(context: Context, intent: Intent) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, AlarmRingingActivity::class.java)

        notificationIntent.setFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        val pendingI = PendingIntent.getActivity(
            context, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, "default")


        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground) //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            val channelName ="알람"
            val description = "설정된 복용 시간에 알람합니다."
            var importance = NotificationManager.IMPORTANCE_HIGH //소리와 알림메시지를 같이 보여줌

            val channel = NotificationChannel("default", channelName, importance)
            channel.setDescription(description)

            notificationManager.createNotificationChannel(channel)
        }
        else builder.setSmallIcon(R.mipmap.ic_launcher) // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남


        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())

            .setTicker("약 먹을 시간이에요!")
            .setContentTitle("약 먹을 시간이에요!")
            .setContentText("")
            .setContentInfo("INFO")
            .setContentIntent(pendingI)

        // 노티피케이션 동작시킴
        notificationManager.notify(1234, builder.build())
        val nextNotifyTime = Calendar.getInstance()

        // 내일 같은 시간으로 알람시간 결정
        nextNotifyTime.add(Calendar.DATE, 1)

        // DB에 다음 알람 시간 업데이트
        val alarmRepository = AlarmRepository.getInstance(context)
        val requestId = intent.extras!!.getInt("requestId")

        CoroutineScope(Dispatchers.IO).launch {
            val alarm = alarmRepository.findByRequestId(requestId)!!
            alarm.time = nextNotifyTime.timeInMillis
            alarmRepository.updateAlarm(alarm)
        }

        val currentDateTime = nextNotifyTime.time
        val date_text = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
        Toast.makeText(context.applicationContext,"다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
    }

    fun setSoundOn(context: Context, intent: Intent) {
        // intent로부터 전달받은 알람 상태값 ALARMON
        val strAlarmState = intent.extras!!.getString("state")

//        // RingtonePlayingService 서비스 intent 생성
//        val service_intent = Intent(
//            context,
//            AlarmService::class.java
//        )
//
//        // RingtonePlayinService intent에 알람 상태값 저장
//        service_intent.putExtra("state", strAlarmState)
//
//        // 안드로이드 SDK 버전에 맞게 알람음 서비스 시작
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(service_intent)
//        } else {
//            context.startService(service_intent)
//        }
    }
}


class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            // on device boot complete, reset the alarm
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                alarmIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val current_calendar = Calendar.getInstance()

            // TODO db에서 읽어오기
//            val sharedPreferences = context.getSharedPreferences("daily alarm", MODE_PRIVATE)
//            val millis =
//                sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().timeInMillis)
//            val nextNotifyTime: Calendar = Calendar.getInstance()
//            nextNotifyTime.timeInMillis = sharedPreferences.getLong("nextNotifyTime", millis)

            val nextNotifyTime = Calendar.getInstance()
            nextNotifyTime.timeInMillis = 0 // TODO
            if (current_calendar.after(nextNotifyTime)) {
                nextNotifyTime.add(Calendar.DATE, 1)
            }
            val currentDateTime: Date = nextNotifyTime.time
            val date_text =
                SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(
                    currentDateTime
                )
            Toast.makeText(
                context.applicationContext,
                "[재부팅후] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!",
                Toast.LENGTH_SHORT
            ).show()
            manager.setRepeating(
                AlarmManager.RTC_WAKEUP, nextNotifyTime.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }
}