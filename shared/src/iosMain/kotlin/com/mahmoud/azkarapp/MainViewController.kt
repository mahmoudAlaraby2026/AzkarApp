package com.mahmoud.azkarapp

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { 
    App() 
}.also {
    // Initialize notifications when the app starts
    IOSNotifications.setupAndScheduleNotifications()
}