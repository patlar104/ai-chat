package com.example.aicompanion.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aicompanion.core.data.database.dao.AliasDao
import com.example.aicompanion.core.data.database.dao.AuditLogDao
import com.example.aicompanion.core.data.database.dao.MessageDao
import com.example.aicompanion.core.data.database.dao.ReminderDao
import com.example.aicompanion.core.data.database.entity.AliasEntity
import com.example.aicompanion.core.data.database.entity.AuditLogEntity
import com.example.aicompanion.core.data.database.entity.MessageEntity
import com.example.aicompanion.core.data.database.entity.ReminderEntity

@Database(
    entities = [MessageEntity::class, AuditLogEntity::class, AliasEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun aliasDao(): AliasDao
    abstract fun reminderDao(): ReminderDao
}
