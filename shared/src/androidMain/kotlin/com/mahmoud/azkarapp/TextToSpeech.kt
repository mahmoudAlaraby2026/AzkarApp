package com.mahmoud.azkarapp

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import android.media.MediaPlayer
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import azkarapp.shared.generated.resources.Res
import java.io.File
import java.io.FileOutputStream

actual class TextToSpeechManager(private val context: Context, private val tts: TextToSpeech) {
    private var mediaPlayer: MediaPlayer? = null
    var isReady = false
    private var isStopped = false
    private var fetchJob: Job? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })
        // Cache files are now preserved to avoid redownloading/extracting every time
    }

    actual fun speakWithMishary(id: Int, text: String, onComplete: () -> Unit) {
        stop()
        isStopped = false

        // Check if this is the "صل على محمد" dhikr, and try to play offline resource first
        if (id == 20 || id == 49) {
            try {
                val resId = context.resources.getIdentifier("salla", "raw", context.packageName)
                if (resId != 0) {
                    val player = MediaPlayer.create(context, resId)
                    if (player != null) {
                        mediaPlayer = player
                        player.setOnCompletionListener {
                            try { it.release() } catch (e: Exception) {}
                            mediaPlayer = null
                            if (!isStopped) onComplete()
                        }
                        player.setOnErrorListener { _, _, _ ->
                            try { player.release() } catch (e: Exception) {}
                            mediaPlayer = null
                            // If local fails, try URL
                            playWithLocalCache(id, text, onComplete)
                            true
                        }
                        player.start()
                        return
                    }
                }
            } catch (e: Exception) {
                // Ignore and fallback to URL
            }
        }

        playWithLocalCache(id, text, onComplete)
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

    private fun playWithLocalCache(id: Int, text: String, onComplete: () -> Unit) {
        val mappedId = getLocalFileId(id)
        fetchJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val tempFile = File(context.cacheDir, "mishary_v2_$mappedId.mp3")
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    val bytes = withContext(Dispatchers.IO) {
                        Res.readBytes("files/audio/mishary/$mappedId.mp3")
                    }
                    if (isStopped) return@launch
                    withContext(Dispatchers.IO) {
                        FileOutputStream(tempFile).use { it.write(bytes) }
                    }
                }
                
                if (isStopped) return@launch
                
                playAudioLocalOrUrl(tempFile.absolutePath, id, text, onComplete)
            } catch (e: Exception) {
                if (!isStopped) {
                    speakTtsInternal(text, onComplete)
                }
            }
        }
    }

    private fun playAudioLocalOrUrl(path: String, id: Int, text: String, onComplete: () -> Unit) {
        try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setDataSource(path)
            player.setOnPreparedListener { mp ->
                if (isStopped) {
                    try { mp.release() } catch (e: Exception) {}
                    mediaPlayer = null
                    return@setOnPreparedListener
                }
                try {
                    val params = mp.playbackParams
                    params.speed = 0.90f
                    mp.playbackParams = params
                } catch (e: Exception) {}
                mp.start()
            }
            player.setOnCompletionListener {
                try { it.release() } catch (e: Exception) {}
                mediaPlayer = null
                if (!isStopped) onComplete()
            }
            player.setOnErrorListener { _, _, _ ->
                try { player.release() } catch (e: Exception) {}
                mediaPlayer = null
                if (!isStopped) speakTtsInternal(text, onComplete)
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            mediaPlayer = null
            if (!isStopped) speakTtsInternal(text, onComplete)
        }
    }

    actual fun speak(text: String, onComplete: () -> Unit) {
        stop()
        isStopped = false
        speakTtsInternal(text, onComplete)
    }

    private fun speakTtsInternal(text: String, onComplete: () -> Unit) {
        // Setup language and parameters for maximum clarity for illiterate users
        tts.language = Locale("ar")
        tts.setSpeechRate(0.40f) // Extremely slow and deliberate for easy listening
        tts.setPitch(0.95f) // Natural calm pitch

        // Strip verse numbers like (1), (2), (3) and format punctuation
        val cleanedText = text
            .replace(Regex("\\(\\d+\\)"), "")
            .replace(":", ": ")
            .replace(".", ". ")

        val rawChunks = cleanedText.split(Regex("(?<=[.:!?،;\n])|(?=\\n)"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val chunks = mutableListOf<String>()
        for (chunk in rawChunks) {
            if (chunk.length > 50) {
                val words = chunk.split(" ")
                var current = StringBuilder()
                for (word in words) {
                    if (current.length + word.length + 1 > 40) {
                        chunks.add(current.toString())
                        current = StringBuilder(word)
                    } else {
                        if (current.isNotEmpty()) current.append(" ")
                        current.append(word)
                    }
                }
                if (current.isNotEmpty()) chunks.add(current.toString())
            } else {
                chunks.add(chunk)
            }
        }

        if (chunks.isEmpty()) {
            onComplete()
            return
        }

        var currentChunkIndex = 0

        fun playNextChunk() {
            if (isStopped) return

            if (currentChunkIndex < chunks.size) {
                val utteranceId = "ID_${System.currentTimeMillis()}_$currentChunkIndex"
                val params = android.os.Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId && !isStopped) {
                            currentChunkIndex++
                            handler.postDelayed({
                                playNextChunk()
                            }, 1200)
                        }
                    }
                    override fun onError(id: String?) {
                        if (!isStopped) {
                            handler.post { onComplete() }
                        }
                    }
                })
                
                tts.language = Locale("ar")
                tts.setSpeechRate(0.40f) 
                tts.setPitch(0.95f)
                
                tts.speak(chunks[currentChunkIndex], TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            } else {
                if (!isStopped) {
                    handler.post { onComplete() }
                }
            }
        }

        playNextChunk()
    }

    actual fun stop() {
        isStopped = true
        fetchJob?.cancel()
        handler.removeCallbacksAndMessages(null)
        try {
            tts.stop()
        } catch (e: Exception) {}
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaPlayer = null
    }

    actual fun isSpeaking(): Boolean = tts.isSpeaking || (mediaPlayer?.isPlaying == true)
}

@Composable
actual fun rememberTextToSpeechManager(): TextToSpeechManager {
    val context = LocalContext.current
    val manager = remember {
        var ttsInstance: TextToSpeech? = null
        val m = TextToSpeechManager(context, TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("ar")
            }
        }.also { ttsInstance = it })
        m
    }
    
    LaunchedEffect(manager) {
        manager.isReady = true 
    }
    
    DisposableEffect(Unit) {
        onDispose {
            manager.stop()
        }
    }
    
    return manager
}