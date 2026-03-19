package com.example.aicompanion.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val domain: String,
    val service: String,
    val entityId: String,
    val status: String,             // "SUCCESS" | "FAILURE"
    val errorReason: String?,
    @ColumnInfo(index = true) val timestampMs: Long,
)
