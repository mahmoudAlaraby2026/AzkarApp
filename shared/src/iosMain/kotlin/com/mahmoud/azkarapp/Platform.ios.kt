@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package com.mahmoud.azkarapp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIDevice
import platform.UserNotifications.*
import platform.posix.exit
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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

    override fun getCachedAudioFile(fileName: String): String? {
        return try {
            val fileManager = platform.Foundation.NSFileManager.defaultManager
            val cacheDirs = fileManager.URLsForDirectory(
                directory = platform.Foundation.NSCachesDirectory,
                inDomains = platform.Foundation.NSUserDomainMask
            )
            val cacheUrl = cacheDirs.firstOrNull() as? platform.Foundation.NSURL ?: return null
            val cachePath = cacheUrl.path ?: return null
            val filePath = "$cachePath/audio_cache/$fileName"
            if (fileManager.fileExistsAtPath(filePath)) filePath else null
        } catch (_: Exception) {
            null
        }
    }

    override fun saveAudioToCache(fileName: String, bytes: ByteArray): String? {
        if (bytes.isEmpty()) return null
        return try {
            val fileManager = platform.Foundation.NSFileManager.defaultManager
            val cacheDirs = fileManager.URLsForDirectory(
                directory = platform.Foundation.NSCachesDirectory,
                inDomains = platform.Foundation.NSUserDomainMask
            )
            val cacheUrl = cacheDirs.firstOrNull() as? platform.Foundation.NSURL ?: return null
            val cachePath = cacheUrl.path ?: return null
            val filePath = "$cachePath/audio_cache/$fileName"
            
            val fileUrl = platform.Foundation.NSURL.fileURLWithPath(filePath)
            val parentUrl = fileUrl.URLByDeletingLastPathComponent
            if (parentUrl != null) {
                fileManager.createDirectoryAtURL(
                    url = parentUrl,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
            
            if (!fileManager.fileExistsAtPath(filePath)) {
                val nsData = bytes.usePinned { pinned ->
                    NSData.create(
                        bytes = pinned.addressOf(0),
                        length = bytes.size.toULong()
                    )
                }
                nsData.writeToFile(filePath, atomically = true)
            }
            filePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual fun getPlatform(): Platform = IOSPlatform()