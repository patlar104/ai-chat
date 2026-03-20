package com.ariaai.companion.core.network.ha

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HaServiceRequest(
    @SerialName("entity_id") val entityId: String,
    val brightness: Int? = null,
    val temperature: Float? = null,
)
