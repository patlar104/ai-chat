package com.ariaai.companion.core.domain.logging

/**
 * Injectable logging contract for all modules.
 * Implementations are bound via Hilt in :app (TimberLogger for production).
 * Tests can inject a no-op or capturing logger without Timber.
 */
interface Logger {
    /** Log a debug message. Stripped in release builds by the TimberLogger implementation. */
    fun d(tag: String, message: String)

    /** Log an error message, optionally with a throwable. */
    fun e(tag: String, message: String, throwable: Throwable? = null)

    /** Log a warning message. */
    fun w(tag: String, message: String)

    /** Log an info message. */
    fun i(tag: String, message: String)
}
