# CLAUDE.md — Aria AI Companion: Project Context

## Project Overview

**Aria** (package: `com.ariaai.companion`) is a personal, voice-first Android application
built with Kotlin and Jetpack Compose. Three primary roles:

1. **Voice-first AI Companion:** Expressive AI avatar with realistic voice conversation.
2. **Home Control Command Center:** Integrates with Home Assistant to control smart-home devices and routines.
3. **Task & Memory Operator:** Briefings, reminders, and automations with persistent user memory.

Designed for **privacy and offline reliability** via hybrid AI routing:
**Deterministic Parser → Local AI → Cloud AI**. Core features (home control, reminders, local TTS) must work without a network.

---

## Module Structure (15 modules)

```
:app                    # Entry point, Hilt setup, global navigation, TimberLogger
:core:ui                # Shared Compose components, theme, design tokens
:core:domain            # Pure Kotlin models, repository interfaces, Logger interface
:core:data              # Room DB, DataStore, repository implementations
:core:network           # Retrofit, OkHttp, interceptors (HA auth, privacy, base URL)
:core:audio             # STT, TTS, Media3 playback
:core:ai                # AiRouter, CommandParser, cloud/local AI service wrappers
:core:automation        # WorkManager workers, AlarmManager, ReminderScheduler
:feature:voice          # Voice UI, VoiceViewModel, mic button, listening/speaking states
:feature:chat           # Chat timeline UI, ChatViewModel
:feature:avatar         # 2D avatar states (idle/listening/thinking/speaking/error)
:feature:memory         # Memory review and deletion UI
:feature:homecontrol    # HA device/routine control UI
:feature:tasks          # Reminder/task management UI
:feature:settings       # HA URL/token config, privacy toggle, AI key entry
```

---

## Architecture Rules (Enforced at Build Time)

- **Feature isolation:** Feature modules MUST NOT depend on other feature modules.
  Violation throws `GradleException` at Gradle configuration time (root `build.gradle.kts`).
- **Shared logic:** All shared business logic, models, and interfaces live in `:core:domain`.
- **No UI logic:** Business logic is strictly prohibited in Composables; use ViewModels.
- **Clean networking:** No network calls from UI modules; all calls go through typed repository contracts in `:core:domain`.
- **Logger:** `:core:domain` defines a `Logger` interface with zero Android imports. `TimberLogger` lives in `:app` only — Timber dependency declared nowhere else.
- **Annotation processing:** KSP only. No kapt references anywhere.
- **Hilt modules:** Use abstract class (not object) for modules that use `@Binds`.

---

## Settled Decisions (Do Not Re-Litigate)

These are resolved decisions from the project decision log. Do not propose changing them:

- `@Volatile` fields on interceptors (`HaAuthInterceptor`, `BaseUrlInterceptor`, `PrivacyInterceptor`, `CloudAiService`) — updated from DataStore Flow without blocking OkHttp threads. Mirrors the same pattern across all four.
- `PrivacyInterceptor` is the **first** application interceptor — blocks cloud AI requests before HA auth or base URL interceptors run.
- `AiRouterImpl` checks `privacyInterceptor.privacyModeEnabled` before any cloud call. `apiKey==null` returns `Unknown` (silent); apiKey set + cloud null returns a degraded message.
- `ParsedIntent.CloudResponse` is a sealed subtype — cloud AI returns free-text, not a typed `QueryType`.
- `TURN_OFF` matched before `TURN_ON` in `CommandParser` — prevents greedy trailing-on false positives.
- `VoiceViewModel` uses `StateFlow` for state + `Channel(BUFFERED)` for one-shot UI effects.
- `ReminderScheduler` uses `Class.forName` to reference `ReminderReceiver` — avoids `:core:automation` depending on `:app`.
- `SpeechRecognizer` must be created on the main thread — enforce this from all voice work.
- `foregroundServiceType="microphone"` is declared in the manifest.
- `AppModule` uses abstract class (not object) — required for `@Binds` in Hilt.
- All domain models are pure Kotlin — zero `android.*` imports for testability.
- Repository interfaces use `Flow` for reactive streams and `suspend` for one-shot operations.
- `HomeAssistantRepository` returns `kotlin.Result<Unit>` — no checked exceptions.
- Dispatcher qualifiers: `@MainDispatcher` and `@IoDispatcher` via Hilt `SingletonComponent`.
- `AppNavHost` navController is a required param — `MainActivity` owns the shared `NavController`.
- `AiRouter.resolveIntent` is a `suspend fun` — required for async cloud calls.
- `CloudAiService` uses `@Volatile var apiKey` — mirrors `HaAuthInterceptor.token` pattern; keeps `:core:network` free of `:core:data` dependency.
- Brightness converted from % (0–100) to HA int (0–255) at parse time, not at HA call time.
- BootReceiver uses `@AndroidEntryPoint + goAsync()` — standard Hilt+coroutine pattern for BroadcastReceiver async work.
- HA Access Token is masked using `PasswordVisualTransformation` with show/hide toggle.
- `ktlint` is **blocking** in CI — formatting violations fail the pipeline immediately.
- `detekt` is **warning-only** in Phases 1–5; tightened to blocking in Phase 6.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.20 |
| JVM target | 17 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| DI | Hilt 2.59.2 + KSP |
| Persistence | Room 2.8.4, DataStore 1.2.1, Android Keystore |
| Networking | Retrofit 3.0.0, OkHttp 5.0.0 |
| Background | WorkManager 2.11.1, AlarmManager |
| AI/ML | ML Kit GenAI (local, Phase 3+), Cloud AI via Retrofit |
| Audio | Android Speech Services (STT), Android TTS + Media3 |
| Animation | Lottie 6.7.1 (Phase 5) |
| Serialization | kotlinx-serialization 1.10.0 |

**Testing:** JUnit 4, MockK 1.14.9, Turbine 1.2.1, Robolectric 4.14, Compose BOM 2026.03.00

---

## Build Configuration

- `applicationId`: `com.ariaai.companion`
- `minSdk`: 26 (Android 8.0)
- `compileSdk` / `targetSdk`: 36
- AGP: 9.1.0
- `rootProject.name`: `AICompanion` (in `settings.gradle.kts`)

---

## WSL2 Environment Paths

```
JAVA_HOME=/home/patri/jdk/jdk-21.0.10+7
ANDROID_HOME=/home/patri/Android/Sdk
```

These are set in `~/.bashrc`. `gradle.properties` also pins `org.gradle.java.home`. Do NOT use `ANDROID_SDK_ROOT` — `ANDROID_HOME` is the correct variable.

---

## Common Build Commands

```bash
./gradlew build                    # Full build (all modules)
./gradlew :app:installDebug        # Build + install debug APK (requires connected device/emulator)
./gradlew test                     # Unit tests (all modules)
./gradlew connectedAndroidTest     # Instrumented tests (requires device)
./gradlew detekt                   # Static analysis
./gradlew ktlintCheck              # Lint check (BLOCKING — formatting violations fail CI)
./gradlew ktlintFormat             # Auto-format Kotlin code
./gradlew :app:assembleRelease     # Release APK (requires keystore.properties)
adb devices                        # List connected devices
adb logcat -s Aria                 # Filter logcat by app tag
```

---

## Phase Status

| Phase | Name | Status |
|---|---|---|
| 01 | Foundation & Scaffold | COMPLETE |
| 02 | Voice + Home Control MVP | COMPLETE |
| 03 | AI Routing + Privacy | COMPLETE (2026-03-19) |
| 04 | Background Ops + Memory | NOT STARTED |
| 05 | Avatar | NOT STARTED |
| 06 | Hardening & Release | NOT STARTED |

**Next up:** Phase 04 — WorkManager briefings/automations, persistent user memory with review/delete controls.

State file: `.planning/STATE.md`
Roadmap: `.planning/ROADMAP.md`

---

## GSD Workflow (Get Shit Done Automation)

This project uses the GSD automation framework in `.claude/`:

- **Resume:** Run `/gsd:resume-work` or `/gsd:next` to pick up from last state.
- **Progress:** `/gsd:progress` shows current phase/plan position.
- **State:** `.planning/STATE.md` tracks current position, decisions, blockers.
- **Config:** `.planning/config.json` — mode is `yolo` (auto-advance, no manual confirmation between plans).
- **Planning:** `.planning/phases/` — per-phase directories with PLAN.md files.

---

## Code Conventions

- **Logging:** Always use `Timber.d/i/w/e()` — never `Log.*` directly.
- **Coroutines:** `@IoDispatcher` for I/O, `@MainDispatcher` for UI — both provided via Hilt qualifiers defined in `:core:domain`.
- **Flows:** `StateFlow` for UI state, `Channel(BUFFERED)` for one-shot effects.
- **Results:** Repository one-shot ops return `kotlin.Result<T>` — no checked exceptions.
- **No empty catch blocks** — always at minimum log with Timber.
- **Domain purity:** `:core:domain` must have zero `android.*` imports. Pure Kotlin only.
- **Feature modules:** Only depend on `:core:domain` and `:core:ui` — no cross-feature imports.
