---
plan: 02-05
phase: 02-voice-home-control-mvp
status: complete
completed: 2026-03-19
requirements:
  - HOME-01
  - HOME-02
  - HOME-05
  - HOME-07
  - CONV-04
---

# Plan 02-05: HA REST API + Command Parser — Complete

## What was built

**Task 1 — HA REST API Network Layer** (committed in parallel with 02-03)
- `core/network/build.gradle.kts` — Retrofit, OkHttp, kotlinx-serialization-json, Hilt deps
- `HomeAssistantService.kt` — Retrofit interface: `@POST("api/services/{domain}/{service}")`
- `HaServiceRequest.kt` — `@Serializable` data class with entity_id, brightness, temperature
- `HaStateResponse.kt` — `@Serializable` response model
- `HaAuthInterceptor.kt` — `@Volatile var token: String?` updated from DataStore; no `runBlocking`
- `BaseUrlInterceptor.kt` — `@Volatile var baseUrl: HttpUrl?` enables dynamic user-configurable HA URL
- `HomeAssistantRepositoryImpl.kt` — Maps HTTP 401/404 to `AppError.HomeAssistant`, `IOException` to `AppError.Network`
- `NetworkModule.kt` — `abstract class` with `@Binds` + `companion object` for `@Provides`; OkHttpClient with both interceptors; placeholder baseUrl `http://localhost/` overridden at runtime
- `gradle/libs.versions.toml` — added `retrofit-kotlinx-serialization` entry
- Deleted `CoreNetwork.kt` placeholder

**Task 2 — Deterministic CommandParser** (committed 60dfdd5)
- `core/ai/build.gradle.kts` — added `:core:domain` dependency
- `CommandParser.kt` — `@Singleton @Inject constructor()`, 8 regex patterns:
  - TURN_ON, TURN_OFF, BRIGHTNESS, TEMPERATURE (home control)
  - ROUTINE (scene/automation triggers)
  - REMINDER (with `parseTime` for relative + absolute time)
  - TIME_QUERY, REMINDERS_QUERY (local queries)
  - Priority order: queries → reminders → brightness → temp → turn off → turn on → routine → Unknown
  - Brightness converts % (0-100) to HA value (0-255)
  - Time parser: relative ("in 30 minutes") + absolute ("8 PM", "8:30 AM"), schedules next-day if past
- `AiModule.kt` — comment updated; CommandParser auto-provided via `@Inject constructor`
- Deleted `CoreAi.kt` placeholder

## Key decisions

- `@Volatile` fields on interceptors — updated asynchronously from DataStore emissions (in VoiceViewModel, Wave 3) without blocking the OkHttp thread
- Placeholder `baseUrl("http://localhost/")` in Retrofit — BaseUrlInterceptor overrides scheme/host/port at call time; avoids Retrofit's URL validation requiring a real host
- TURN_OFF checked before TURN_ON — prevents "turn off the lights" from matching the TURN_ON pattern's trailing ` on` match
- `NetworkModule` changed from `object` to `abstract class` — required for Hilt `@Binds` support

## Self-Check: PASSED

key-files verified on disk:
- core/network/src/main/kotlin/.../ha/HomeAssistantService.kt ✓
- core/ai/src/main/kotlin/.../parser/CommandParser.kt ✓
- core/network/src/main/kotlin/.../ha/BaseUrlInterceptor.kt ✓
- core/network/src/main/kotlin/.../repository/HomeAssistantRepositoryImpl.kt ✓
