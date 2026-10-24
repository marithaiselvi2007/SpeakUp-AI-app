package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechState {
    IDLE,
    LISTENING,
    PROCESSING,
    PAUSED,
    ERROR
}

class SpeechRecognizerHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onFinalResult: ((String) -> Unit)? = null
    var onPartialResult: ((String) -> Unit)? = null
    var onErrorCallback: ((Int, String) -> Unit)? = null

    private fun ensureRecognizer(): Boolean {
        if (speechRecognizer != null) return true
        return try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _errorMessage.value = "Speech recognition is not available on this device"
                return false
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _speechState.value = SpeechState.LISTENING
                        _errorMessage.value = null
                    }

                    override fun onBeginningOfSpeech() {
                        _speechState.value = SpeechState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _speechState.value = SpeechState.PROCESSING
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Speech recognition error ($error)"
                        }
                        Log.w("SpeechHelper", "Recognition error: $message ($error)")
                        _speechState.value = SpeechState.ERROR
                        _errorMessage.value = message
                        onErrorCallback?.invoke(error, message)
                    }

                    override fun onResults(results: Bundle?) {
                        _speechState.value = SpeechState.IDLE
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val bestMatch = matches?.firstOrNull() ?: ""
                        if (bestMatch.isNotBlank()) {
                            _spokenText.value = bestMatch
                            onFinalResult?.invoke(bestMatch)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { partial ->
                            if (partial.isNotBlank()) {
                                _spokenText.value = partial
                                onPartialResult?.invoke(partial)
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            true
        } catch (e: Throwable) {
            Log.w("SpeechHelper", "SpeechRecognizer creation failed: ${e.message}")
            _errorMessage.value = "Speech recognition service unavailable"
            false
        }
    }

    fun startListening() {
        try {
            if (!ensureRecognizer()) {
                return
            }
            _spokenText.value = ""
            _errorMessage.value = null

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
            _speechState.value = SpeechState.LISTENING
        } catch (e: Throwable) {
            Log.e("SpeechHelper", "Failed to start listening", e)
            _speechState.value = SpeechState.ERROR
            _errorMessage.value = e.localizedMessage ?: "Failed to start microphone"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _speechState.value = SpeechState.PROCESSING
        } catch (e: Throwable) {
            Log.e("SpeechHelper", "Failed to stop listening", e)
            _speechState.value = SpeechState.IDLE
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _speechState.value = SpeechState.IDLE
            _rmsDb.value = 0f
        } catch (e: Throwable) {
            Log.e("SpeechHelper", "Failed to cancel listening", e)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Throwable) {
            Log.e("SpeechHelper", "Failed to destroy recognizer", e)
        }
    }
}
