@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package com.mahmoud.azkarapp

import android.os.Build
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val versionCode: Int
        get() = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            0
        }

    override fun showTestNotification() {
        val channelId = "salla_periodic_channel_v6"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "تنبيه الصلاة على النبي", 
                NotificationManager.IMPORTANCE_HIGH
            )
            
            try {
                val resId = context.resources.getIdentifier("salla", "raw", context.packageName)
                if (resId != 0) {
                    val soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + resId)
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    channel.setSound(soundUri, audioAttributes)
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("اذكر الله")
            .setContentText("صلّ على سيدنا محمد")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))

        try {
            val resId = context.resources.getIdentifier("salla", "raw", context.packageName)
            if (resId != 0) {
                val soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + resId)
                builder.setSound(soundUri)
            }
        } catch (e: Exception) {}

        notificationManager.notify(1, builder.build())
    }

    override fun exitApp() {
        (context as? android.app.Activity)?.finishAffinity()
    }

    override fun playClickSound() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD, 1.0f)
    }

    override fun playAlertSound() {
        try {
            val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val r = android.media.RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun vibrate(duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    override fun getCachedAudioFile(fileName: String): String? {
        return try {
            val cacheDir = java.io.File(context.cacheDir, "audio_cache")
            val file = java.io.File(cacheDir, fileName)
            if (file.exists() && file.length() > 0) {
                file.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun saveAudioToCache(fileName: String, bytes: ByteArray): String? {
        return try {
            val cacheDir = java.io.File(context.cacheDir, "audio_cache")
            val file = java.io.File(cacheDir, fileName)
            file.parentFile?.mkdirs()
            if (!file.exists() || file.length() != bytes.size.toLong()) {
                file.writeBytes(bytes)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private var platformInstance: Platform? = null

fun initPlatform(context: Context) {
    platformInstance = AndroidPlatform(context)
    
    // Cancel the old inexact WorkManager jobs
    androidx.work.WorkManager.getInstance(context).cancelUniqueWork("AzkarPeriodicWork")
    androidx.work.WorkManager.getInstance(context).cancelUniqueWork("SallaPeriodicWork")
    
    // Schedule exact 15-minute alarms
    AzkarReceiver.scheduleExact(context)
    SallaReceiver.scheduleExact(context)
}

actual fun getPlatform(): Platform = platformInstance ?: throw IllegalStateException("Platform not initialized")
