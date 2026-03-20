package com.ariaai.companion.core.domain.model

data class Reminder(
    val id: Long = 0,
    val description: String,
    val triggerTimeMs: Long,
    val status: ReminderStatus,
    val createdAtMs: Long,
)

enum class ReminderStatus { PENDING, FIRED, CANCELLED }
