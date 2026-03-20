package com.ariaai.companion.core.domain.repository

import com.ariaai.companion.core.domain.model.Alias
import kotlinx.coroutines.flow.Flow

interface AliasRepository {
    fun observeAll(): Flow<List<Alias>>
    suspend fun findByAlias(alias: String): Alias?
    suspend fun insert(alias: Alias): Long
    suspend fun update(alias: Alias)
    suspend fun delete(id: Long)
}
