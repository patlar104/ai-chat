package com.ariaai.companion.core.audio.model

sealed interface SpeechResult {
    data class Success(val text: String) : SpeechResult
    data class Error(val code: Int, val message: String) : SpeechResult
}
