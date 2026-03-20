package com.ariaai.companion.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ariaai.companion.core.data.database.entity.AliasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasDao {
    @Query("SELECT * FROM aliases ORDER BY alias ASC")
    fun observeAll(): Flow<List<AliasEntity>>

    @Query("SELECT * FROM aliases WHERE LOWER(alias) = LOWER(:alias) LIMIT 1")
    suspend fun findByAlias(alias: String): AliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AliasEntity): Long

    @Update
    suspend fun update(entity: AliasEntity)

    @Query("DELETE FROM aliases WHERE id = :id")
    suspend fun deleteById(id: Long)
}
