package com.example.aicompanion.core.network.ha

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HaStateResponse(
    @SerialName("entity_id") val entityId: String = "",
    val state: String = "",
)
