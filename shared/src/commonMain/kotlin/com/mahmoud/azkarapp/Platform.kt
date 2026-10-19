@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package com.mahmoud.azkarapp

interface Platform {
    val name: String
    val versionCode: Int
    val isAndroid: Boolean get() = name.startsWith("Android")
    val isIOS: Boolean get() = !isAndroid
    fun showTestNotification()
    fun exitApp()
    fun playClickSound()
    fun playAlertSound()
    fun vibrate(duration: Long)
    fun getCachedAudioFile(fileName: String): String?
    fun saveAudioToCache(fileName: String, bytes: ByteArray): String?
}

expect fun getPlatform(): Platform