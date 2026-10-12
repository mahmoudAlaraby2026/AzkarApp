package com.mahmoud.azkarapp

import androidx.compose.runtime.Composable

expect class TextToSpeechManager {
    fun speak(text: String, onComplete: () -> Unit = {})
    fun speakWithMishary(id: Int, text: String, onComplete: () -> Unit = {})
    fun stop()
    fun isSpeaking(): Boolean
}

@Composable
expect fun rememberTextToSpeechManager(): TextToSpeechManager
