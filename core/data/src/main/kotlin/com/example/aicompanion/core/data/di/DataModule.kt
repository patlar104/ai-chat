package com.example.aicompanion.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.aicompanion.core.data.database.AppDatabase
import com.example.aicompanion.core.data.database.dao.AliasDao
import com.example.aicompanion.core.data.database.dao.AuditLogDao
import com.example.aicompanion.core.data.database.dao.MessageDao
import com.example.aicompanion.core.data.database.dao.ReminderDao
import com.example.aicompanion.core.data.repository.AliasRepositoryImpl
import com.example.aicompanion.core.data.repository.AuditLogRepositoryImpl
import com.example.aicompanion.core.data.repository.MessageRepositoryImpl
import com.example.aicompanion.core.data.repository.ReminderRepositoryImpl
import com.example.aicompanion.core.data.repository.SettingsRepositoryImpl
import com.example.aicompanion.core.domain.repository.AliasRepository
import com.example.aicompanion.core.domain.repository.AuditLogRepository
import com.example.aicompanion.core.domain.repository.MessageRepository
import com.example.aicompanion.core.domain.repository.ReminderRepository
import com.example.aicompanion.core.domain.repository.SettingsRepository
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
    }
}
