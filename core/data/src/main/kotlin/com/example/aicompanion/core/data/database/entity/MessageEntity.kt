package com.example.aicompanion.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,               // "USER" | "ASSISTANT"
    val content: String,
    val sourceType: String,         // "DETERMINISTIC" | "UNKNOWN"
    val timestampMs: Long,
    @ColumnInfo(index = true) val sessionId: String,
)
