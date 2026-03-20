package com.ariaai.companion.core.data.repository

import com.ariaai.companion.core.data.database.dao.AliasDao
import com.ariaai.companion.core.data.database.entity.AliasEntity
import com.ariaai.companion.core.domain.model.Alias
import com.ariaai.companion.core.domain.repository.AliasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AliasRepositoryImpl @Inject constructor(
    private val aliasDao: AliasDao,
) : AliasRepository {

    override fun observeAll(): Flow<List<Alias>> =
        aliasDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findByAlias(alias: String): Alias? =
        aliasDao.findByAlias(alias)?.toDomain()

    override suspend fun insert(alias: Alias): Long =
        aliasDao.insert(alias.toEntity())

    override suspend fun update(alias: Alias) =
        aliasDao.update(alias.toEntity())

    override suspend fun delete(id: Long) =
        aliasDao.deleteById(id)

    private fun AliasEntity.toDomain() = Alias(
        id = id,
        alias = alias,
        entityId = entityId,
        domain = domain,
    )

    private fun Alias.toEntity() = AliasEntity(
        id = id,
        alias = alias,
        entityId = entityId,
        domain = domain,
    )
}
