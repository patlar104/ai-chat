package com.ariaai.companion.core.audio.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.ariaai.companion.core.audio.model.SpeechResult
import com.ariaai.companion.core.domain.di.MainDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) {
    private var recognizer: SpeechRecognizer? = null
    private var resultCallback: ((SpeechResult) -> Unit)? = null

    suspend fun initialize() {
        withContext(mainDispatcher) {
            check(Looper.myLooper() == Looper.getMainLooper()) {
                "SpeechRecognizer MUST be created on the main thread"
            }
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    suspend fun startListening(onResult: (SpeechResult) -> Unit) {
        resultCallback = onResult
        withContext(mainDispatcher) {
            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NETWORK -> "No network available"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Could not understand speech"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        else -> "Recognition failed (code $error)"
                    }
                    resultCallback?.invoke(SpeechResult.Error(error, message))
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        resultCallback?.invoke(SpeechResult.Success(text))
                    } else {
                        resultCallback?.invoke(
                            SpeechResult.Error(SpeechRecognizer.ERROR_NO_MATCH, "No speech detected"),
                        )
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }

            recognizer?.setRecognitionListener(listener)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            recognizer?.startListening(intent)
        }
    }

    suspend fun stopListening() = withContext(mainDispatcher) {
        recognizer?.stopListening()
    }

    suspend fun destroy() = withContext(mainDispatcher) {
        recognizer?.destroy()
        recognizer = null
        resultCallback = null
    }
}
