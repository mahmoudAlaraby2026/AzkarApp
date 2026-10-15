package com.mahmoud.azkarapp

import androidx.compose.runtime.*
import platform.darwin.*
import platform.Foundation.*
import kotlinx.coroutines.*
import azkarapp.shared.generated.resources.Res
import kotlinx.cinterop.*
import platform.AVFAudio.*

@OptIn(ExperimentalForeignApi::class)
actual class TextToSpeechManager {
    private val synthesizer = AVSpeechSynthesizer()
    private var audioPlayer: AVAudioPlayer? = null
    private var onCompleteCallback: (() -> Unit)? = null
    private var isStopped = false
    private var fetchJob: Job? = null

    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            if (!isStopped) onCompleteCallback?.invoke()
        }
    }

    private val audioDelegate = object : NSObject(), AVAudioPlayerDelegateProtocol {
        override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
            audioPlayer = null
            if (!isStopped) onCompleteCallback?.invoke()
        }
    }

    init {
        synthesizer.delegate = delegate
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {}
    }
    
    actual fun speak(text: String, onComplete: () -> Unit) {
        stop()
        isStopped = false
        onCompleteCallback = onComplete
        
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("ar-SA")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * 0.5f
        synthesizer.speakUtterance(utterance)
    }

    actual fun speakWithMishary(id: Int, text: String, onComplete: () -> Unit) {
        stop()
        isStopped = false
        onCompleteCallback = onComplete
        
        val mappedId = getLocalFileId(id)
        
        fetchJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val bytes = if (id == 25 || id == 47) {
                    Res.readBytes("files/audio/salla.mp3")
                } else if (id == 5) {
                    Res.readBytes("files/audio/asbahna.mp3")
                } else if (id == 7 || id == 32) {
                    Res.readBytes("files/audio/sayyid.mp3")
                } else if (id == 11 || id == 36) {
                    Res.readBytes("files/audio/raditu.mp3")
                } else if (id == 18 || id == 43) {
                    Res.readBytes("files/audio/ashhaduka.mp3")
                } else if (id == 21) {
                    Res.readBytes("files/audio/min_nima.mp3")
                } else if (id == 13 || id == 38) {
                    Res.readBytes("files/audio/hasbi_allah.mp3")
                } else if (id == 10 || id == 35) {
                    Res.readBytes("files/audio/bismillah.mp3")
                } else {
                    Res.readBytes("files/audio/mishary/$mappedId.mp3")
                }
                
                if (isStopped) return@launch
                
                @OptIn(ExperimentalForeignApi::class)
                val data = bytes.usePinned { pinned ->
                    NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
                }
                
                val player = AVAudioPlayer(data = data, error = null)
                player.delegate = audioDelegate
                player.prepareToPlay()
                player.play()
                audioPlayer = player
            } catch (e: Exception) {
                if (!isStopped) speak(text, onComplete)
            }
        }
    }

    private fun getLocalFileId(id: Int): Int {
        return when (id) {
            26 -> 1; 27 -> 2; 28 -> 3; 29 -> 4; 30 -> 5
            31 -> 6; 32 -> 7; 33 -> 8; 34 -> 9; 35 -> 10
            36 -> 11; 37 -> 12; 38 -> 13; 39 -> 14; 48 -> 15
            41 -> 16; 42 -> 17; 43 -> 18; 40 -> 19; 49 -> 20
            else -> id
        }
    }
    
    actual fun stop() {
        isStopped = true
        fetchJob?.cancel()
        fetchJob = null
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        audioPlayer?.stop()
        audioPlayer = null
    }
    
    actual fun isSpeaking(): Boolean = synthesizer.isSpeaking() || (audioPlayer?.isPlaying() ?: false)
}

@Composable
actual fun rememberTextToSpeechManager(): TextToSpeechManager {
    val manager = remember { TextToSpeechManager() }
    DisposableEffect(Unit) {
        onDispose {
            manager.stop()
        }
    }
    return manager
}
