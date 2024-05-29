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
import com.dearmyhealth.data.db.AppDatabase
import com.dearmyhealth.modules.Alarm.AlarmRepository
import com.dearmyhealth.modules.Alarm.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit


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

        // 다음 알람 설정
        setAlarm(context, intent)
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

    fun setAlarm(context: Context, intent: Intent) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val requestId = intent.extras!!.getInt("requestId")
        CoroutineScope(Dispatchers.IO).launch {
            val alarmRepository = AlarmRepository.getInstance(context)
            val alarm = alarmRepository.findByRequestId(requestId) ?: return@launch
            val dosageAlarmDao = AppDatabase.getDatabase(context).dosageAlarmDao()
            val dosageAlarm = dosageAlarmDao.findByRequestCode(requestId) ?: return@launch

            // 다음 알람 시간 구하기
            var datetime = OffsetDateTime.now()
            val minutes = TimeUnit.HOURS.toMinutes(datetime.hour.toLong()).toInt() + datetime.minute

            var nextAlarmMinutes = minutes

            for (time in dosageAlarm.dosageTime) {
                if (time > minutes) {
                    nextAlarmMinutes = time
                    break
                }
            }
            if (nextAlarmMinutes == minutes) {
                nextAlarmMinutes = dosageAlarm.dosageTime[0]
                datetime = datetime.plusDays(1)
            }
            val nextDatetime = datetime
                .withHour(nextAlarmMinutes/60)
                .withMinute(nextAlarmMinutes%60)
                .toInstant().toEpochMilli()

            // DB에 다음 알람 시간 업데이트
            alarm.time = nextDatetime
            alarmRepository.updateAlarm(alarm)
            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra("requestId", requestId)
                intent.putExtra("dosageName", alarm.description)
                PendingIntent.getBroadcast(context, requestId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager!!.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                nextDatetime,
                alarmIntent
            )
            val date_text = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDatetime)
            withContext(Dispatchers.Main){
                Toast.makeText(context.applicationContext,"다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // on device boot complete, reset the alarm
            val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            CoroutineScope(Dispatchers.IO).launch {
                val alarmRepository = AlarmRepository.getInstance(context)
                val dosageAlarmDao = AppDatabase.getDatabase(context).dosageAlarmDao()
                val alarms = alarmRepository.getEnabledAlarms()
                for (alarm in alarms) {
                    val dosageAlarm = dosageAlarmDao.findByRequestCode(alarm.requestCode) ?: return@launch

                    // 다음 알람 시간 구하기
                    var datetime = OffsetDateTime.now()
                    var nextDatetime = alarm.time

                    if (datetime.isAfter(
                            OffsetDateTime.ofInstant(Instant.ofEpochSecond(alarm.time), ZoneId.systemDefault())
                    )) {
                        val minutes =
                            TimeUnit.HOURS.toMinutes(datetime.hour.toLong()).toInt() + datetime.minute

                        var nextAlarmMinutes = minutes

                        for (time in dosageAlarm.dosageTime) {
                            if (time > minutes) {
                                nextAlarmMinutes = time
                                break
                            }
                        }
                        if (nextAlarmMinutes == minutes) {
                            nextAlarmMinutes = dosageAlarm.dosageTime[0]
                            datetime = datetime.plusDays(1)
                        }
                        nextDatetime = datetime
                            .withHour(nextAlarmMinutes / 60)
                            .withMinute(nextAlarmMinutes % 60)
                            .toInstant().toEpochMilli()

                        // DB에 다음 알람 시간 업데이트
                        CoroutineScope(Dispatchers.IO).launch {
                            alarm.time = nextDatetime
                            alarmRepository.updateAlarm(alarm)
                        }
                    }
                    val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                        intent.putExtra("requestId", alarm.requestCode)
                        intent.putExtra("dosageName", alarm.description)
                        PendingIntent.getBroadcast(context, alarm.requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
                    }
                    manager!!.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        nextDatetime,
                        alarmIntent
                    )

                    val currentDateTime = nextDatetime
                    val date_text = SimpleDateFormat(
                        "yyyy년 MM월 dd일 EE요일 a hh시 mm분 ",
                        Locale.getDefault()
                        ).format(currentDateTime)
                    withContext(Dispatchers.Main){
                        Toast.makeText(
                            context.applicationContext,
                            "[재부팅후] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}