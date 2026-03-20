package com.ariaai.companion.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val haServerUrl: Flow<String>
    val haAccessToken: Flow<String?>
    val ttsVoiceName: Flow<String?>
    val privacyModeEnabled: Flow<Boolean>
    val backgroundAutomationEnabled: Flow<Boolean>
    val googleAiApiKey: Flow<String?>
    suspend fun setHaServerUrl(url: String)
    suspend fun setHaAccessToken(token: String)
    suspend fun clearHaAccessToken()
    suspend fun setTtsVoiceName(name: String)
    suspend fun setPrivacyModeEnabled(enabled: Boolean)
    suspend fun setBackgroundAutomationEnabled(enabled: Boolean)
    suspend fun setGoogleAiApiKey(key: String)
    suspend fun clearGoogleAiApiKey()
    suspend fun clearAllUserData()
}
