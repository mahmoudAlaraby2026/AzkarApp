package com.mahmoud.azkarapp

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val versionCode: Int = 1
    override fun showTestNotification() {}
    override fun exitApp() {}
    override fun playClickSound() {}
    override fun playAlertSound() {}
    override fun vibrate(duration: Long) {}
}

actual fun getPlatform(): Platform = IOSPlatform()