package com.ariaai.companion.logging

import com.ariaai.companion.core.domain.logging.Logger
import timber.log.Timber
import javax.inject.Inject

/**
 * Timber-backed Logger implementation.
 * Timber.DebugTree is planted in AICompanionApp.onCreate() for debug builds only.
 * In release builds, Timber has no tree planted so debug logs are no-ops.
 * Injected via Hilt — bound in AppModule.
 */
class TimberLogger @Inject constructor() : Logger {

    override fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
    }

    override fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }

    override fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }
}
