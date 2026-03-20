# Keep source file names and line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ── Gson / SerializedName ─────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Hilt ──────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── Google AI Client (Gemini SDK) ─────────────────────────────────────────────
-keep class com.google.ai.client.** { *; }
-keep class com.google.generativeai.** { *; }
-dontwarn com.google.ai.client.**
-dontwarn com.google.generativeai.**

# ── Protobuf (used by Gemini SDK internally) ──────────────────────────────────
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ── Android Keystore / Crypto ─────────────────────────────────────────────────
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# ── App models (domain entities shared across modules) ────────────────────────
-keep class com.ariaai.companion.core.domain.model.** { *; }
-keep class com.ariaai.companion.core.data.database.entity.** { *; }

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-dontwarn androidx.work.**

# ── Timber ────────────────────────────────────────────────────────────────────
-dontwarn timber.log.**

# ── BuildConfig ───────────────────────────────────────────────────────────────
-keep class com.ariaai.companion.BuildConfig { *; }
