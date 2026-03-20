package com.example.aicompanion.core.audio.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var initialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var onDoneCallback: (() -> Unit)? = null

    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            initialized = (status == TextToSpeech.SUCCESS)
            if (initialized) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onDoneCallback?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        onDoneCallback?.invoke()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        onDoneCallback?.invoke()
                    }
                })
                onReady()
            }
        }
    }

    fun speak(
        text: String,
        utteranceId: String = java.util.UUID.randomUUID().toString(),
        onDone: (() -> Unit)? = null,
    ) {
        if (!initialized) return
        onDoneCallback = onDone
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
        _isSpeaking.value = false
    }

    fun getAvailableVoices(): List<Voice> = tts?.voices?.toList() ?: emptyList()

    fun setVoice(voiceName: String) {
        tts?.voices?.firstOrNull { it.name == voiceName }?.let { tts?.voice = it }
    }

    fun isInitialized(): Boolean = initialized
}
