package com.ariaai.companion.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ariaai.companion.core.data.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE status = 'PENDING' ORDER BY triggerTimeMs ASC")
    fun observePending(): Flow<List<ReminderEntity>>

    @Insert
    suspend fun insert(entity: ReminderEntity): Long

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT * FROM reminders WHERE status = 'PENDING'")
    suspend fun getAllPending(): List<ReminderEntity>

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
