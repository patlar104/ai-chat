package com.ariaai.companion.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "aliases", indices = [Index(value = ["alias"], unique = true)])
data class AliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val entityId: String,
    val domain: String,
)
