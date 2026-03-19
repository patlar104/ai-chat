package com.example.aicompanion.core.domain.model

data class Alias(
    val id: Long = 0,
    val alias: String,
    val entityId: String,
    val domain: String,
)
