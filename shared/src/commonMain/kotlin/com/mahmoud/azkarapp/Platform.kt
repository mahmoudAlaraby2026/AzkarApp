package com.mahmoud.azkarapp

interface Platform {
    val name: String
    val versionCode: Int
    fun showTestNotification()
    fun exitApp()
    fun playClickSound()
    fun playAlertSound()
    fun vibrate(duration: Long)
}

expect fun getPlatform(): Platform