package com.mahmoud.azkarapp

import androidx.compose.runtime.*
import platform.AVFoundation.*
import platform.darwin.*

actual class TextToSpeechManager {
    private val synthesizer = AVSpeechSynthesizer()
    private var onCompleteCallback: (() -> Unit)? = null

    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            onCompleteCallback?.invoke()
        }
    }

    init {
        synthesizer.delegate = delegate
    }
    
    actual fun speak(text: String, onComplete: () -> Unit) {
        stop()
        onCompleteCallback = onComplete
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("ar-SA")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * 0.5f
        synthesizer.speakUtterance(utterance)
    }

    actual fun speakWithMishary(id: Int, text: String, onComplete: () -> Unit) {
        speak(text, onComplete)
    }
    
    actual fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
    
    actual fun isSpeaking(): Boolean = synthesizer.isSpeaking()
}

@Composable
actual fun rememberTextToSpeechManager(): TextToSpeechManager {
    return remember { TextToSpeechManager() }
}
