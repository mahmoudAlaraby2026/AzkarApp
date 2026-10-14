package com.mahmoud.azkarapp

import platform.UIKit.UIDevice
import platform.UserNotifications.*
import platform.posix.exit

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val versionCode: Int = 1
    
    override fun showTestNotification() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent()
        content.setTitle("اذكر الله")
        content.setBody("صلّ على سيدنا محمد ﷺ")
        // Assumes salla.mp3 is added directly to the Xcode project bundle.
        content.setSound(UNNotificationSound.soundNamed("salla.mp3"))
        
        // Trigger in 1 second for testing
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier("test_salla_ios_${platform.Foundation.NSUUID().UUIDString}", content, trigger)
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error showing test notification: $error")
            }
        }
    }
    
    override fun exitApp() {
        exit(0)
    }
    
    override fun playClickSound() {}
    override fun playAlertSound() {}
    override fun vibrate(duration: Long) {}
}

actual fun getPlatform(): Platform = IOSPlatform()