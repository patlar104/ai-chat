package com.example.aicompanion.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val HA_SERVER_URL = stringPreferencesKey("ha_server_url")
        val HA_TOKEN_CIPHERTEXT = stringPreferencesKey("ha_token_ciphertext")
        val HA_TOKEN_IV = stringPreferencesKey("ha_token_iv")
        val TTS_VOICE_NAME = stringPreferencesKey("tts_voice_name")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode_enabled")
        val BACKGROUND_AUTOMATION = booleanPreferencesKey("background_automation_enabled")
    }

    val haServerUrl: Flow<String> = context.dataStore.data.map { it[Keys.HA_SERVER_URL] ?: "" }
    val haTokenCiphertext: Flow<String?> = context.dataStore.data.map { it[Keys.HA_TOKEN_CIPHERTEXT] }
    val haTokenIv: Flow<String?> = context.dataStore.data.map { it[Keys.HA_TOKEN_IV] }
    val ttsVoiceName: Flow<String?> = context.dataStore.data.map { it[Keys.TTS_VOICE_NAME] }
    val privacyModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.PRIVACY_MODE] ?: false }
    val backgroundAutomationEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BACKGROUND_AUTOMATION] ?: true }

    suspend fun setHaServerUrl(url: String) { context.dataStore.edit { it[Keys.HA_SERVER_URL] = url } }
    suspend fun setHaTokenEncrypted(ciphertext: String, iv: String) {
        context.dataStore.edit {
            it[Keys.HA_TOKEN_CIPHERTEXT] = ciphertext
            it[Keys.HA_TOKEN_IV] = iv
        }
    }
    suspend fun clearHaToken() {
        context.dataStore.edit {
            it.remove(Keys.HA_TOKEN_CIPHERTEXT)
            it.remove(Keys.HA_TOKEN_IV)
        }
    }
    suspend fun setTtsVoiceName(name: String) { context.dataStore.edit { it[Keys.TTS_VOICE_NAME] = name } }
    suspend fun setPrivacyModeEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.PRIVACY_MODE] = enabled } }
    suspend fun setBackgroundAutomationEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.BACKGROUND_AUTOMATION] = enabled } }
}
