package com.ariaai.companion.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ariaai.companion.core.data.database.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_log ORDER BY timestampMs DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insert(entity: AuditLogEntity): Long

    @Query("DELETE FROM audit_log")
    suspend fun deleteAll()
}
