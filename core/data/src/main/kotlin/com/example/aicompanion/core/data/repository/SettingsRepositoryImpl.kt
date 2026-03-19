package com.example.aicompanion.core.data.repository

import com.example.aicompanion.core.data.crypto.HaTokenCrypto
import com.example.aicompanion.core.data.datastore.AppPreferences
import com.example.aicompanion.core.domain.di.IoDispatcher
import com.example.aicompanion.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val haTokenCrypto: HaTokenCrypto,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override val haServerUrl: Flow<String> = appPreferences.haServerUrl

    override val haAccessToken: Flow<String?> = combine(
        appPreferences.haTokenCiphertext,
        appPreferences.haTokenIv,
    ) { ciphertext, iv ->
        if (ciphertext != null && iv != null) {
            withContext(ioDispatcher) {
                haTokenCrypto.decrypt(ciphertext, iv)
            }
        } else {
            null
        }
    }

    override val ttsVoiceName: Flow<String?> = appPreferences.ttsVoiceName
    override val privacyModeEnabled: Flow<Boolean> = appPreferences.privacyModeEnabled
    override val backgroundAutomationEnabled: Flow<Boolean> = appPreferences.backgroundAutomationEnabled

    override suspend fun setHaServerUrl(url: String) = appPreferences.setHaServerUrl(url)

    override suspend fun setHaAccessToken(token: String) {
        val (ciphertext, iv) = withContext(ioDispatcher) { haTokenCrypto.encrypt(token) }
        appPreferences.setHaTokenEncrypted(ciphertext, iv)
    }

    override suspend fun clearHaAccessToken() = appPreferences.clearHaToken()
    override suspend fun setTtsVoiceName(name: String) = appPreferences.setTtsVoiceName(name)
    override suspend fun setPrivacyModeEnabled(enabled: Boolean) = appPreferences.setPrivacyModeEnabled(enabled)
    override suspend fun setBackgroundAutomationEnabled(enabled: Boolean) = appPreferences.setBackgroundAutomationEnabled(enabled)
}
