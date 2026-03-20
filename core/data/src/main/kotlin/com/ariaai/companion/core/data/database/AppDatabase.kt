package com.ariaai.companion.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ariaai.companion.core.data.database.dao.AliasDao
import com.ariaai.companion.core.data.database.dao.AuditLogDao
import com.ariaai.companion.core.data.database.dao.MemoryDao
import com.ariaai.companion.core.data.database.dao.MessageDao
import com.ariaai.companion.core.data.database.dao.ReminderDao
import com.ariaai.companion.core.data.database.entity.AliasEntity
import com.ariaai.companion.core.data.database.entity.AuditLogEntity
import com.ariaai.companion.core.data.database.entity.MessageEntity
import com.ariaai.companion.core.data.database.entity.MemoryEntity
import com.ariaai.companion.core.data.database.entity.ReminderEntity

@Database(
    entities = [MessageEntity::class, AuditLogEntity::class, AliasEntity::class, ReminderEntity::class, MemoryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun aliasDao(): AliasDao
    abstract fun reminderDao(): ReminderDao
    abstract fun memoryDao(): MemoryDao
}
