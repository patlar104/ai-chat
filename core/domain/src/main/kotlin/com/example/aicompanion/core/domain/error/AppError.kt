package com.example.aicompanion.core.domain.error

/**
 * Project-wide sealed error hierarchy.
 * All modules catch and surface errors using these types.
 * Using when(error) in UI/presentation layers will be exhaustive checked by the compiler.
 */
sealed class AppError(override val message: String) : Exception(message) {

    /** A network I/O failure — HTTP errors, timeout, no connectivity. */
    data class Network(val cause: Throwable) :
        AppError("Network error: ${cause.message}")

    /** A local storage failure — Room query error, DataStore write failure. */
    data class Storage(val cause: Throwable) :
        AppError("Storage error: ${cause.message}")

    /** An audio pipeline failure — SpeechRecognizer error, TTS failure, audio focus lost. */
    data class AudioPipeline(val reason: String) :
        AppError("Audio error: $reason")

    /** A Home Assistant API failure — authentication, entity not found, WebSocket disconnected. */
    data class HomeAssistant(val reason: String) :
        AppError("Home Assistant error: $reason")

    /** An unclassified error. Use only when no specific subtype applies. */
    data object Unknown : AppError("Unknown error")
}
