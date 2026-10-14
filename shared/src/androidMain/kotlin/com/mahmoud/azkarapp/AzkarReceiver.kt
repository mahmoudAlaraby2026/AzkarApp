package com.mahmoud.azkarapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AzkarReceiver : BroadcastReceiver() {

    private val azkarList = listOf(
        "سبحان الله وبحمده، سبحان الله العظيم",
        "لا إله إلا الله وحده لا شريك له",
        "أستغفر الله وأتوب إليه",
        "الحمد لله حمداً كثيراً طيباً مباركاً فيه",
        "لا حول ولا قوة إلا بالله",
        "سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر",
        "اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور",
        "رضيت بالله رباً وبالإسلام ديناً وبمحمد صلى الله عليه وسلم نبياً",
        "حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleExact(context, force = true)
            return
        }

        showNotification(context)
        // Schedule the next one exactly in 15 minutes
        scheduleExact(context, force = true)
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val randomZekr = azkarList.random()
        
        val channelId = "azkar_periodic_channel_v6"
        val channelName = "أذكار تنبيهية"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            
            notificationManager.createNotificationChannel(channel)
        }

        val appIcon = context.applicationInfo.icon
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            launchIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(appIcon)
            .setContentTitle("اذكر الله")
            .setContentText(randomZekr)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val prefs = context.getSharedPreferences("azkar_prefs", Context.MODE_PRIVATE)
        val lastId = prefs.getInt("azkar_notif_id", 3000)
        val nextId = if (lastId == 3000) 3001 else 3000
        prefs.edit().putInt("azkar_notif_id", nextId).apply()

        notificationManager.cancel(lastId)
        notificationManager.notify(nextId, notificationBuilder.build())
    }

    companion object {
        fun scheduleExact(context: Context, force: Boolean = false) {
            val prefs = context.getSharedPreferences("azkar_prefs", Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val nextAlarm = prefs.getLong("azkar_next_alarm", 0)
            val interval = 15 * 60 * 1000L
            
            // If not forced and the alarm is already scheduled for the future, don't overwrite it
            if (!force && nextAlarm > now && (nextAlarm - now) <= interval) {
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AzkarReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                1002, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            val triggerTime = now + interval
            prefs.edit().putLong("azkar_next_alarm", triggerTime).apply()
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        // Fallback if permission not granted
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
