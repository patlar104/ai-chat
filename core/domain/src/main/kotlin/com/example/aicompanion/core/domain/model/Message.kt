package com.example.aicompanion.core.domain.model

data class Message(
    val id: Long = 0,
    val role: MessageRole,
    val content: String,
    val sourceType: SourceType,
    val timestampMs: Long,
    val sessionId: String,
)

enum class MessageRole { USER, ASSISTANT }
enum class SourceType { DETERMINISTIC, CLOUD, UNKNOWN }
