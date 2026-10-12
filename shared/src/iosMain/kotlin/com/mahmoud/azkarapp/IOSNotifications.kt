package com.mahmoud.azkarapp

import platform.UserNotifications.*

object IOSNotifications {

    fun setupAndScheduleNotifications() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // Request permissions
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            if (granted) {
                // Clear existing notifications to reset timers
                center.removeAllPendingNotificationRequests()
                
                scheduleSallaNotification(center)
                // Optionally schedule another one for normal Azkar (e.g. offset by a few minutes so they don't fire exactly together)
                scheduleAzkarNotification(center)
            }
        }
    }

    private fun scheduleSallaNotification(center: UNUserNotificationCenter) {
        val content = UNMutableNotificationContent()
        content.setTitle("اذكر الله")
        content.setBody("صلّ على سيدنا محمد ﷺ")
        // NOTE: The salla.mp3 file MUST be added to your Xcode project bundle (drag and drop it into Xcode).
        // Otherwise, the default iOS sound will play.
        content.setSound(UNNotificationSound.soundNamed("salla.mp3")) 
        
        // 15 minutes = 900 seconds
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(900.0, repeats = true)
        
        val request = UNNotificationRequest.requestWithIdentifier("salla_periodic_ios", content, trigger)
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling Salla notification: $error")
            }
        }
    }

    private fun scheduleAzkarNotification(center: UNUserNotificationCenter) {
        val content = UNMutableNotificationContent()
        content.setTitle("اذكر الله")
        content.setBody("سبحان الله وبحمده، سبحان الله العظيم") // Fixed text due to iOS repeating trigger limits
        content.setSound(UNNotificationSound.defaultSound())
        
        // Schedule every 15 minutes + 1 minute offset (960 seconds) so it doesn't overlap the Salla notification
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(960.0, repeats = true)
        
        val request = UNNotificationRequest.requestWithIdentifier("azkar_periodic_ios", content, trigger)
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling Azkar notification: $error")
            }
        }
    }
}
