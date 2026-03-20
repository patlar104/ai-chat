package com.ariaai.companion.core.domain.model

import com.ariaai.companion.core.domain.error.AppError

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Transcribing : VoiceState
    data class Processing(val transcript: String) : VoiceState
    data class Speaking(val reply: String) : VoiceState
    data class Error(val error: AppError) : VoiceState
}

sealed interface VoiceEvent {
    data object MicTapped : VoiceEvent
    data object TtsDone : VoiceEvent
    data class TranscriptReceived(val text: String) : VoiceEvent
    data class RecognitionError(val code: Int) : VoiceEvent
    data class ReplyReady(val text: String) : VoiceEvent
    data class CommandError(val error: AppError) : VoiceEvent
}

sealed interface VoiceUiEffect {
    data class ShowToast(val message: String) : VoiceUiEffect
    data object Vibrate : VoiceUiEffect
}
