package com.ariaai.companion.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val triggerTimeMs: Long,
    @ColumnInfo(index = true) val status: String,  // "PENDING" | "FIRED" | "CANCELLED"
    val createdAtMs: Long,
)
