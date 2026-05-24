package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"

    fun scheduleAlarmsForToday(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now()
        val times = PrayerTimesRepository.getTimesForDate(today.monthValue, today.dayOfMonth) ?: return

        val prayerNames = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")

        for (i in times.indices) {
            val timeParts = times[i].split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: continue
                val min = timeParts[1].toIntOrNull() ?: continue

                val alarmTime = LocalDateTime.of(today.year, today.month, today.dayOfMonth, hour, min)
                val alarmMillis = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (alarmMillis > System.currentTimeMillis()) {
                    val intent = Intent(context, AdhanReceiver::class.java).apply {
                        putExtra(EXTRA_PRAYER_NAME, prayerNames.getOrNull(i) ?: "Prayer")
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        i,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmMillis,
                            pendingIntent
                        )
                    } catch (e: SecurityException) {
                        // Handle missing exact alarm permission on Android 14+
                    }
                }
            }
        }
    }
}
