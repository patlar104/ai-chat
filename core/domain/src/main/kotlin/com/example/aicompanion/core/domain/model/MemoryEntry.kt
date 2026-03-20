package com.example.aicompanion.core.domain.model

data class MemoryEntry(
    val id: Long = 0,
    val title: String,
    val detail: String,
    val createdAtMs: Long,
)
