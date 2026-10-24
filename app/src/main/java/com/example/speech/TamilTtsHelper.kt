package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Dedicated Text-to-Speech manager for reading Tamil meaning aloud
 * using Tamil language / voice (ta-IN or ta).
 */
class TamilTtsHelper private constructor(private val context: Context) {

    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isInitializing = false

    private val _currentlySpeakingText = MutableStateFlow<String?>(null)
    val currentlySpeakingText: StateFlow<String?> = _currentlySpeakingText.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        initTts()
    }

    private fun initTts(onReady: (() -> Unit)? = null) {
        if (isInitialized) {
            onReady?.invoke()
            return
        }
        if (isInitializing) return
        isInitializing = true

        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                isInitializing = false
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val tamilLocale = Locale("ta", "IN")
                        val langResult = tts?.setLanguage(tamilLocale)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            val genericTamil = Locale("ta")
                            val genericResult = tts?.setLanguage(genericTamil)
                            if (genericResult == TextToSpeech.LANG_MISSING_DATA || genericResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.w("TamilTtsHelper", "Tamil language not directly supported on device, using default")
                            }
                        }

                        tts?.setSpeechRate(0.9f) // Slightly slower for clear Tamil pronunciation
                        tts?.setPitch(1.0f)

                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                                _currentlySpeakingText.value = null
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                                _currentlySpeakingText.value = null
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                _isSpeaking.value = false
                                _currentlySpeakingText.value = null
                            }
                        })

                        isInitialized = true
                        onReady?.invoke()
                    } catch (e: Throwable) {
                        Log.w("TamilTtsHelper", "Error configuring Tamil TTS: ${e.message}")
                    }
                } else {
                    Log.w("TamilTtsHelper", "Tamil TTS initialization failed with status: $status")
                }
            }
        } catch (e: Throwable) {
            isInitializing = false
            Log.w("TamilTtsHelper", "Failed to create TextToSpeech instance: ${e.message}")
        }
    }

    /**
     * Reads the exact Tamil text aloud. If the same text is already being spoken, stops playback.
     */
    fun speakTamil(tamilText: String) {
        val cleanText = tamilText.replace(Regex("[*#_`\\[\\]]"), "").trim()
        if (cleanText.isBlank()) return

        if (_currentlySpeakingText.value == cleanText && _isSpeaking.value) {
            stop()
            return
        }

        if (!isInitialized) {
            initTts {
                speakInternal(cleanText)
            }
            return
        }

        speakInternal(cleanText)
    }

    private fun speakInternal(text: String) {
        try {
            stop()
            _currentlySpeakingText.value = text
            _isSpeaking.value = true

            val tamilLocale = Locale("ta", "IN")
            val langResult = tts?.setLanguage(tamilLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("ta"))
            }

            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "Tamil_Utterance_${System.currentTimeMillis()}"
            )
        } catch (e: Throwable) {
            Log.w("TamilTtsHelper", "Error speaking Tamil: ${e.message}")
            _isSpeaking.value = false
            _currentlySpeakingText.value = null
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Throwable) {
            Log.w("TamilTtsHelper", "Error stopping TTS: ${e.message}")
        } finally {
            _isSpeaking.value = false
            _currentlySpeakingText.value = null
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
            Log.w("TamilTtsHelper", "Error shutting down Tamil TTS: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: TamilTtsHelper? = null

        fun getInstance(context: Context): TamilTtsHelper {
            return instance ?: synchronized(this) {
                instance ?: TamilTtsHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
