package com.ariaai.companion.core.domain.model

data class AuditLogEntry(
    val id: Long = 0,
    val command: String,
    val domain: String,
    val service: String,
    val entityId: String,
    val status: AuditStatus,
    val errorReason: String?,
    val timestampMs: Long,
)

enum class AuditStatus { SUCCESS, FAILURE }
