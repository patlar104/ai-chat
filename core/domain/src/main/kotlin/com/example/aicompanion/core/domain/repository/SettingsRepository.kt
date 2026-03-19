package com.example.aicompanion.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val haServerUrl: Flow<String>
    val haAccessToken: Flow<String?>
    val ttsVoiceName: Flow<String?>
    val privacyModeEnabled: Flow<Boolean>
    val backgroundAutomationEnabled: Flow<Boolean>
    suspend fun setHaServerUrl(url: String)
    suspend fun setHaAccessToken(token: String)
    suspend fun clearHaAccessToken()
    suspend fun setTtsVoiceName(name: String)
    suspend fun setPrivacyModeEnabled(enabled: Boolean)
    suspend fun setBackgroundAutomationEnabled(enabled: Boolean)
}
