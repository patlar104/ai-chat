package com.ariaai.companion.core.data.repository

import com.ariaai.companion.core.data.crypto.HaTokenCrypto
import com.ariaai.companion.core.data.database.dao.AuditLogDao
import com.ariaai.companion.core.data.database.dao.MemoryDao
import com.ariaai.companion.core.data.database.dao.MessageDao
import com.ariaai.companion.core.data.database.dao.ReminderDao
import com.ariaai.companion.core.data.datastore.AppPreferences
import com.ariaai.companion.core.domain.di.IoDispatcher
import com.ariaai.companion.core.domain.repository.SettingsRepository
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
    private val messageDao: MessageDao,
    private val auditLogDao: AuditLogDao,
    private val reminderDao: ReminderDao,
    private val memoryDao: MemoryDao,
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
    override val googleAiApiKey: Flow<String?> = appPreferences.googleAiApiKey

    override suspend fun setHaServerUrl(url: String) = appPreferences.setHaServerUrl(url)

    override suspend fun setHaAccessToken(token: String) {
        val (ciphertext, iv) = withContext(ioDispatcher) { haTokenCrypto.encrypt(token) }
        appPreferences.setHaTokenEncrypted(ciphertext, iv)
    }

    override suspend fun clearHaAccessToken() = appPreferences.clearHaToken()
    override suspend fun setTtsVoiceName(name: String) = appPreferences.setTtsVoiceName(name)
    override suspend fun setPrivacyModeEnabled(enabled: Boolean) = appPreferences.setPrivacyModeEnabled(enabled)
    override suspend fun setBackgroundAutomationEnabled(enabled: Boolean) = appPreferences.setBackgroundAutomationEnabled(enabled)
    override suspend fun setGoogleAiApiKey(key: String) = appPreferences.setGoogleAiApiKey(key)
    override suspend fun clearGoogleAiApiKey() = appPreferences.clearGoogleAiApiKey()

    override suspend fun clearAllUserData() = withContext(ioDispatcher) {
        messageDao.deleteAll()
        auditLogDao.deleteAll()
        reminderDao.deleteAll()
        memoryDao.deleteAll()
        appPreferences.clearHaToken()
        appPreferences.clearGoogleAiApiKey()
    }
}
