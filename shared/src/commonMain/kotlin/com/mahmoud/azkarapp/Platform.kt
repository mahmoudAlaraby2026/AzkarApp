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
}

expect fun getPlatform(): Platform