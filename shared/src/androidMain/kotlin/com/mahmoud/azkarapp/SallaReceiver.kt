package com.mahmoud.azkarapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class SallaReceiver : BroadcastReceiver() {

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
        
        val channelId = "salla_periodic_channel_v6"
        val channelName = "تنبيه الصلاة على النبي"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            
            try {
                val resId = context.resources.getIdentifier("salla", "raw", context.packageName)
                if (resId != 0) {
                    val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + resId)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    channel.setSound(soundUri, audioAttributes)
                    // Enable vibration to ensure it gets attention
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
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
            .setContentText("صلّ على سيدنا محمد ﷺ")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)

        try {
            val resId = context.resources.getIdentifier("salla", "raw", context.packageName)
            if (resId != 0) {
                val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + resId)
                notificationBuilder.setSound(soundUri)
            }
        } catch (e: Exception) {}

        val prefs = context.getSharedPreferences("azkar_prefs", Context.MODE_PRIVATE)
        val lastId = prefs.getInt("salla_notif_id", 2000)
        val nextId = if (lastId == 2000) 2001 else 2000
        prefs.edit().putInt("salla_notif_id", nextId).apply()

        notificationManager.cancel(lastId)
        notificationManager.notify(nextId, notificationBuilder.build())
    }

    companion object {
        fun scheduleExact(context: Context, force: Boolean = false) {
            val prefs = context.getSharedPreferences("azkar_prefs", Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val nextAlarm = prefs.getLong("salla_next_alarm", 0)
            val interval = 15 * 60 * 1000L
            
            // If not forced and the alarm is already scheduled for the future, don't overwrite it
            if (!force && nextAlarm > now && (nextAlarm - now) <= interval) {
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SallaReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                1001, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            val triggerTime = now + interval
            prefs.edit().putLong("salla_next_alarm", triggerTime).apply()
            
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
