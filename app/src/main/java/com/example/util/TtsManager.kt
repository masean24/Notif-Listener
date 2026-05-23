package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error creating TTS instance", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TtsManager", "Indonesian locale is missing or unsupported. Falling back to system default.")
                tts?.setLanguage(Locale.getDefault())
            }
            isInitialized = true
        } else {
            Log.e("TtsManager", "TTS initialization failed.")
        }
    }

    fun speak(text: String, repeatCount: Int = 1, volume: Float = 1.0f) {
        if (!isInitialized || tts == null) {
            Log.w("TtsManager", "TTS speaker is not initialized yet.")
            return
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        
        // Setup Audio Focus parameters
        val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
        } else {
            null
        }

        val focusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager?.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }

        if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            val params = android.os.Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            }

            val count = repeatCount.coerceIn(1, 3)
            for (i in 0 until count) {
                val queueMode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(text, queueMode, params, "qris_tts_${System.currentTimeMillis()}_$i")
                if (i < count - 1) {
                    // Let's add brief pause between repetitions
                    tts?.playSilentUtterance(600, TextToSpeech.QUEUE_ADD, "qris_tts_pause_$i")
                }
            }
        } else {
            Log.w("TtsManager", "Failed to acquire audio focus for speaking.")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS instance", e)
        } finally {
            tts = null
            isInitialized = false
        }
    }
}
