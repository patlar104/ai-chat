package com.ariaai.companion.core.data.di

import android.content.Context
import androidx.room.Room
import com.ariaai.companion.core.data.database.AppDatabase
import com.ariaai.companion.core.data.database.dao.AliasDao
import com.ariaai.companion.core.data.database.dao.AuditLogDao
import com.ariaai.companion.core.data.database.dao.MemoryDao
import com.ariaai.companion.core.data.database.dao.MessageDao
import com.ariaai.companion.core.data.database.dao.ReminderDao
import com.ariaai.companion.core.data.repository.AliasRepositoryImpl
import com.ariaai.companion.core.data.repository.AuditLogRepositoryImpl
import com.ariaai.companion.core.data.repository.MemoryRepositoryImpl
import com.ariaai.companion.core.data.repository.MessageRepositoryImpl
import com.ariaai.companion.core.data.repository.ReminderRepositoryImpl
import com.ariaai.companion.core.data.repository.SettingsRepositoryImpl
import com.ariaai.companion.core.domain.repository.AliasRepository
import com.ariaai.companion.core.domain.repository.AuditLogRepository
import com.ariaai.companion.core.domain.repository.MemoryRepository
import com.ariaai.companion.core.domain.repository.MessageRepository
import com.ariaai.companion.core.domain.repository.ReminderRepository
import com.ariaai.companion.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(impl: AuditLogRepositoryImpl): AuditLogRepository

    @Binds
    @Singleton
    abstract fun bindAliasRepository(impl: AliasRepositoryImpl): AliasRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "ai_companion_db")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

        @Provides
        fun provideAuditLogDao(db: AppDatabase): AuditLogDao = db.auditLogDao()

        @Provides
        fun provideAliasDao(db: AppDatabase): AliasDao = db.aliasDao()

        @Provides
        fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

        @Provides
        fun provideMemoryDao(db: AppDatabase): MemoryDao = db.memoryDao()
    }
}
