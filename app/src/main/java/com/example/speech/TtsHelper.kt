package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    @Volatile
    private var isInitialized = false
    @Volatile
    private var isInitializing = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private fun ensureInitialized(onReady: (() -> Unit)? = null) {
        if (isInitialized) {
            onReady?.invoke()
            return
        }
        if (isInitializing) return
        isInitializing = true

        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                isInitializing = false
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        val result = tts?.setLanguage(Locale.US)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.w("TtsHelper", "US English not supported, falling back to default locale")
                            tts?.setLanguage(Locale.getDefault())
                        }
                        tts?.setSpeechRate(0.95f)
                        tts?.setPitch(1.0f)
                        isInitialized = true

                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                _isSpeaking.value = false
                            }
                        })
                        onReady?.invoke()
                    } else {
                        Log.w("TtsHelper", "TTS status returned non-success: $status")
                    }
                } catch (e: Throwable) {
                    Log.w("TtsHelper", "TTS listener setup error: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            isInitializing = false
            Log.w("TtsHelper", "TextToSpeech instance creation failed: ${e.message}")
        }
    }

    fun speak(text: String) {
        if (!_isTtsEnabled.value) return
        if (!isInitialized) {
            ensureInitialized {
                speakInternal(text)
            }
            return
        }
        speakInternal(text)
    }

    private fun speakInternal(text: String) {
        try {
            stop()
            val cleanText = text.replace(Regex("[*#_`\\[\\]]"), "").trim()
            if (cleanText.isNotBlank()) {
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "SpeakUp_Utterance_${System.currentTimeMillis()}")
            }
        } catch (e: Throwable) {
            Log.w("TtsHelper", "Error speaking text: ${e.message}")
        }
    }

    fun toggleTts() {
        val newState = !_isTtsEnabled.value
        _isTtsEnabled.value = newState
        if (!newState) {
            stop()
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        if (!enabled) {
            stop()
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Throwable) {
            Log.w("TtsHelper", "Error stopping TTS: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            isInitializing = false
        } catch (e: Throwable) {
            Log.w("TtsHelper", "Error shutting down TTS: ${e.message}")
        }
    }
}
