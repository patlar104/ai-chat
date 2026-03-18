# Phase 1: Foundation & Scaffold - Research

**Researched:** 2026-03-18
**Domain:** Android multi-module Gradle scaffold — Version Catalogs, Hilt DI, Navigation Compose, ktlint/Detekt CI, module dependency enforcement
**Confidence:** HIGH (Gradle/AGP/Hilt/Navigation patterns via official docs and STACK.md prior research) / MEDIUM (Gradle configuration-based feature→feature ban enforcement pattern)

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Dependency Versioning Strategy**
- Use `libs.versions.toml` (Gradle version catalog) for ALL dependency versions — single source of truth
- Verified versions pre-filled at project init: Kotlin 2.3.20, Compose BOM 2026.03.00, Hilt 2.57.1, Room 2.8.4, DataStore 1.2.1, WorkManager 2.11.1, Navigation Compose 2.9.7, Lifecycle 2.10.0, Media3 ExoPlayer 1.9.2
- Versions marked `TO_BE_VERIFIED` (OkHttp, Retrofit, kotlinx-coroutines, kotlinx-serialization, KSP, ML Kit Prompt API, Lottie, Turbine, MockK, Robolectric) must be resolved as the FIRST task in Phase 1 by checking official GitHub release pages before any module code is written
- `security-crypto` / `EncryptedSharedPreferences` is fully deprecated — DO NOT add it. Use Android Keystore API directly + DataStore for encrypted storage.
- Google AI Edge SDK is deprecated — DO NOT add it. On-device AI path is ML Kit Prompt API (Gemini Nano via AICore).
- Compose Compiler is configured via the Compose Compiler Gradle plugin (NOT `kotlinCompilerExtensionVersion` — that is deprecated in Kotlin 2.x)

**Module Creation Scope**
- All 15 modules created in Phase 1 with placeholder implementations
- Module list: `:app`, `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:audio`, `:core:ai`, `:core:automation`, `:feature:voice`, `:feature:chat`, `:feature:avatar`, `:feature:memory`, `:feature:homecontrol`, `:feature:tasks`, `:feature:settings`
- Each module has a compilable `build.gradle.kts`, correct namespace, and at least one placeholder Kotlin file
- `settings.gradle.kts` includes all 15 modules from day 1

**Module Dependency Enforcement**
- Feature→feature imports are BANNED
- Enforcement via Gradle configuration: each feature module only declares allowed dependencies (`core.*` and `app` as permitted callers); any feature→feature dependency causes a Gradle configuration error at sync time
- This rule must be verified in CI: a test that confirms the dependency graph has no feature→feature edges
- Enforcement is established in Phase 1 and never relaxed

**Hilt Dependency Injection**
- Hilt 2.57.1 with KSP (NOT kapt)
- `@HiltAndroidApp` on `AICompanionApp` in `:app` module
- `@AndroidEntryPoint` wired on `MainActivity`
- Each core module exposes a Hilt module (`@Module @InstallIn`) so feature modules can inject without additional wiring
- DI graph covers at minimum: placeholder bindings for repository interfaces in `:core:domain`

**Navigation**
- Navigation Compose 2.9.7
- Single `AppNavHost` composable in `:app` module routes to all feature screens
- Each feature module exposes a navigation route constant (not a string literal inline)
- Phase 1 routes are all placeholder screens — each feature screen shows the feature name as a `Text` composable

**CI Baseline**
- ktlint for Kotlin formatting enforcement — fails CI on violations
- Detekt for static analysis — runs on every push, initially warning-only
- Gradle `build` task validates all 15 modules compile from clean checkout
- CI runs on every push

**Baseline Conventions**
- Package name: `com.example.aicompanion`
- `compileSdk = 35` (Android 15, current stable)
- `minSdk = 26` (Android 8.0 — covers 95%+ active devices, required for modern audio APIs)
- `targetSdk = 35` (matches compileSdk)

### Claude's Discretion
- Exact CI provider and workflow file structure (GitHub Actions YAML, Bitrise, etc.)
- Whether to include a `build-logic` convention plugin module or keep `build.gradle.kts` inline per module for Phase 1
- Test infrastructure setup (unit test dependencies can be added but no tests written in Phase 1)
- Exact Detekt configuration rules

### Deferred Ideas (OUT OF SCOPE)
- None — Phase 1 is purely infrastructure; discussion stayed within phase scope.
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| SCAF-01 | Repository compiles a minimal Android shell across all 15 modules | Standard Gradle multi-module setup with `settings.gradle.kts` including all 15 modules; each with valid `build.gradle.kts` and stub Kotlin file |
| SCAF-02 | Gradle module dependency rules prevent feature→feature imports (enforced by Lint or build tooling) | Gradle `configurations.configureEach` with `resolutionStrategy` or `ProjectDependencyRule`; CI DAG verification script |
| SCAF-03 | CI runs formatting checks and baseline build validation | GitHub Actions workflow with ktlint check + Detekt + `./gradlew build` |
| SCAF-04 | Dependency injection (Hilt) is wired at the app level and available in all modules | `@HiltAndroidApp` on `Application`, `@AndroidEntryPoint` on `MainActivity`, per-core-module `@Module @InstallIn` objects |
| SCAF-05 | Navigation host is established in the app module and routes to all feature screens | `NavHost` composable in `:app`, route constants in each feature module, placeholder `Text` screens |
| SCAF-06 | Baseline error model and logging conventions are in place | Sealed `AppError` hierarchy in `:core:domain`, logging wrapper (`Timber` or structured log) wired via Hilt in `:app` |
</phase_requirements>

---

## Summary

Phase 1 is a pure build-system and wiring phase — no user-facing behavior, no feature logic. The deliverable is a 15-module Gradle project that compiles clean from a fresh checkout with all architectural guardrails in place. The research domain is well-understood: standard Android multi-module Clean Architecture with Gradle Version Catalogs (libs.versions.toml), Hilt DI via KSP, Navigation Compose host, and a CI pipeline enforcing formatting and compilation.

The single highest-risk item in this phase is establishing the feature→feature import ban before any feature code exists. PITFALLS.md rates cyclic module dependency recovery cost as HIGH (1-3 days). The enforcement mechanism must be a Gradle build-time check — not documentation or convention — so violations are caught at sync time, not in code review. This check is the single most important architectural guardrail in the entire project.

The second highest-risk item is resolving all TO_BE_VERIFIED library versions before writing any module code. KSP version must match the Kotlin 2.3.20 toolchain exactly (KSP 2.3.6 is the current latest). OkHttp 5.0.0 is confirmed stable (released July 2025). Retrofit 3.0.0 is confirmed. kotlinx-coroutines 1.10.2, kotlinx-serialization 1.10.0 are confirmed. ML Kit Prompt API is `com.google.mlkit:genai-prompt:1.0.0-beta1` (Alpha/Beta status — note for future phases). These must be pinned in `libs.versions.toml` in Wave 0 before any module `build.gradle.kts` is written.

**Primary recommendation:** Scaffold all 15 modules with the minimum compilable stub pattern (one `build.gradle.kts` + one placeholder `.kt` file per module), install the Gradle feature→feature dependency ban in the same wave, then add Hilt wiring, Navigation host, and CI as subsequent discrete tasks.

---

## Standard Stack

### Core Build Infrastructure

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Android Gradle Plugin (AGP) | 9.1.0 | Build system | Current stable (March 2026); required for Kotlin 2.3.x |
| Gradle | 9.3.1 | Build runner | Required by AGP 9.1.0; must be this version or build fails |
| Kotlin | 2.3.20 | Language | Current stable tooling release (March 16, 2026) |
| KSP | 2.3.6 | Annotation processing | Latest stable; KSP1 does not support Kotlin 2.3.0+; KSP2 is default since early 2025 |
| JDK | 17 | Compilation | Required by AGP 9.1.0; JDK 11 will fail the build |
| Gradle Version Catalogs (`libs.versions.toml`) | (built into Gradle 7.4+) | Single-source version management | Standard for 15-module projects; eliminates version duplication |

### Dependency Injection

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Hilt | 2.57.1 | DI framework | Official stable; compile-time code gen via KSP; no runtime reflection in 15-module graph |
| hilt-navigation-compose | 1.3.0 | `hiltViewModel()` in Compose | Required companion for Hilt + Compose; decoupled from Navigation artifact since 1.3.0 |
| KSP processor (hilt-android-compiler) | 2.57.1 | Hilt KSP processor | Must use `ksp` directive, not `kapt` — kapt is deprecated for Kotlin 2.x |
| KSP processor (room-compiler) | 2.8.4 | Room KSP processor | Declared now in version catalog even if Room entities are in later phases |

### Navigation

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Navigation Compose | 2.9.7 | In-app navigation host | Latest stable (Jan 2026); type-safe routes via Kotlin Serialization; predictive back support |
| kotlinx-serialization-json | 1.10.0 | Required by type-safe Nav Compose routes | Kotlin-native JSON; also used for all data serialization in later phases |

### UI Framework (stubs only in Phase 1)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose BOM | 2026.03.00 | Pins all Compose deps | Single BOM version maps to Compose UI 1.10.5 + Material3 1.4.0 |
| Activity Compose | 1.13.0 | `ComponentActivity` + edge-to-edge | Entry point for Compose; provides `setContent {}` |
| Lifecycle ViewModel Compose | 2.10.0 | `collectAsStateWithLifecycle()` | Safe Flow collection in Compose respecting lifecycle |
| Lifecycle Runtime Compose | 2.10.0 | Lifecycle-aware state collection | Required companion to ViewModel KTX |

### Logging (SCAF-06)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Timber | 5.0.1 | Structured logging | De-facto standard for Android logging; zero-cost debug stripping in release; injectable via Hilt |

### CI / Code Quality

| Tool | Version | Purpose | Why Standard |
|------|---------|---------|--------------|
| ktlint (via Gradle plugin) | 12.1.2 | Kotlin formatting enforcement | Official Kotlin formatter; zero configuration required; fails CI on violations |
| Detekt | 1.23.8 | Static analysis | Kotlin-native; enforces architecture rules; warning-only initially |
| GitHub Actions | N/A | CI runner | Free for public repos; standard Android CI host; YAML workflow in `.github/workflows/` |

### Verified TO_BE_VERIFIED Versions (resolved by this research)

| Library | Verified Version | Source | Notes |
|---------|-----------------|--------|-------|
| KSP | 2.3.6 | github.com/google/ksp/releases | KSP2 decoupled from Kotlin version numbering; 2.3.6 is latest stable as of Feb 2026 |
| kotlinx-coroutines | 1.10.2 | github.com/Kotlin/kotlinx.coroutines/releases | Latest stable; confirmed Feb 2026 |
| kotlinx-serialization | 1.10.0 | github.com/Kotlin/kotlinx.serialization/releases | Latest stable |
| OkHttp | 5.0.0 | OkHttp CHANGELOG.md | First stable 5.x release (July 2025) |
| Retrofit | 3.0.0 | github.com/square/retrofit/releases | Latest stable |
| ML Kit Prompt API | 1.0.0-beta1 | developers.google.com/ml-kit/genai/prompt/android/get-started | Artifact: `com.google.mlkit:genai-prompt:1.0.0-beta1`; still Beta/Alpha — Phase 1 adds to version catalog only, not implemented until later phase |
| Lottie Compose | 6.7.1 | github.com/airbnb/lottie-android/releases | Latest stable; Phase 1 adds to catalog only |
| Turbine | 1.2.1 | github.com/cashapp/turbine/releases | Latest stable; Phase 1 adds to catalog only |
| MockK | 1.14.9 | github.com/mockk/mockk/releases | Latest stable |
| Robolectric | 4.14 | github.com/robolectric/robolectric/releases | Latest stable; API 35 support confirmed |

**Important KSP note:** KSP decoupled its version scheme from Kotlin's in KSP2. `2.3.6` is the KSP2 release number, not a Kotlin version. The plugin is still `id("com.google.devtools.ksp") version "2.3.6"` regardless of Kotlin 2.3.20.

**Important ML Kit note:** `com.google.mlkit:genai-prompt` is Beta as of March 2026. Artifact goes into `libs.versions.toml` now but implementation is deferred to the AI routing phase.

### Supporting Libraries (catalog only in Phase 1)

| Library | Version | When Active |
|---------|---------|-------------|
| Room | 2.8.4 | Phase 3+ |
| DataStore | 1.2.1 | Phase 2+ |
| WorkManager KTX | 2.11.1 | Phase 5+ |
| Media3 ExoPlayer | 1.9.2 | Phase 4+ |
| OkHttp + Retrofit | 5.0.0 / 3.0.0 | Phase 2+ |

### Installation

```bash
# Project-level — already in AGP + Kotlin plugin declarations
./gradlew wrapper --gradle-version 9.3.1
```

```toml
# gradle/libs.versions.toml — complete Phase 1 version catalog
[versions]
kotlin = "2.3.20"
agp = "9.1.0"
ksp = "2.3.6"

# AndroidX — verified
composeBom = "2026.03.00"
hilt = "2.57.1"
hiltAndroidx = "1.3.0"
room = "2.8.4"
datastore = "1.2.1"
workmanager = "2.11.1"
lifecycle = "2.10.0"
navigationCompose = "2.9.7"
activityCompose = "1.13.0"
media3 = "1.9.2"
appStartup = "1.2.0"
androidxTestJunit = "1.3.0"
espresso = "3.7.0"

# Kotlin ecosystem — verified
coroutines = "1.10.2"
kotlinxSerialization = "1.10.0"

# Networking — verified
okhttp = "5.0.0"
retrofit = "3.0.0"

# On-device AI — verified (Beta, catalog only)
mlKitGenAi = "1.0.0-beta1"

# Avatar animation — verified (catalog only)
lottie = "6.7.1"

# Testing — verified
turbine = "1.2.1"
mockk = "1.14.9"
robolectric = "4.14"

# Code quality — verified
timber = "5.0.1"
detekt = "1.23.8"
ktlint = "12.1.2"
```

---

## Architecture Patterns

### Recommended Project Structure

```
app/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── kotlin/com/example/aicompanion/
        ├── AICompanionApp.kt     # @HiltAndroidApp Application
        ├── MainActivity.kt       # @AndroidEntryPoint, setContent { AppNavHost() }
        └── navigation/
            └── AppNavHost.kt     # NavHost routing to all 15 feature destinations

core/
├── ui/                           # Shared design system stubs
├── domain/                       # Pure Kotlin models, interfaces, AppError sealed class
├── data/                         # Room + DataStore (stubs in Phase 1)
├── network/                      # OkHttp/Retrofit (stubs in Phase 1)
├── audio/                        # SpeechRecognizer/TTS abstractions (stubs)
├── ai/                           # AI router (stubs)
└── automation/                   # WorkManager workers (stubs)

feature/
├── voice/                        # Placeholder screen composable + route constant
├── chat/                         # Placeholder screen
├── avatar/                       # Placeholder screen
├── homecontrol/                  # Placeholder screen
├── memory/                       # Placeholder screen
├── tasks/                        # Placeholder screen
└── settings/                     # Placeholder screen

gradle/
└── libs.versions.toml            # All 15 module versions in single catalog

.github/
└── workflows/
    └── ci.yml                    # ktlint + Detekt + build check
```

### Pattern 1: Minimal Module Stub (per feature module)

**What:** Every feature module has exactly: one `build.gradle.kts` declaring its allowed dependencies, one placeholder `Screen.kt` file exposing a route constant and a `@Composable` screen that shows its name as a `Text`.

**When to use:** All 14 non-app modules in Phase 1.

**Example (`:feature:voice` stub):**
```kotlin
// feature/voice/src/main/kotlin/com/example/aicompanion/feature/voice/VoiceScreen.kt
package com.example.aicompanion.feature.voice

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

const val VOICE_ROUTE = "voice"

@Composable
fun VoiceScreen() {
    Text(text = "Voice Screen — placeholder")
}
```

```kotlin
// feature/voice/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.aicompanion.feature.voice"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.compose.bom)  // via BOM
    implementation(libs.lifecycle.viewmodel.compose)
}
```

### Pattern 2: Feature→Feature Import Ban via Gradle Configuration

**What:** Every feature module declares a `configurations.all` block that rejects any dependency on another `:feature:*` module. This causes a Gradle sync error if a banned dependency is added, rather than waiting for code review.

**When to use:** Applied to all 7 feature modules in their `build.gradle.kts`. Also add a CI script that verifies the dependency graph.

**Example:**
```kotlin
// Applies to all feature module build.gradle.kts files
configurations.configureEach {
    resolutionStrategy {
        eachDependency {
            // This alone doesn't block project deps — use the approach below
        }
    }
}

// Better: use a Gradle project-level rule in build.gradle.kts at root
// root build.gradle.kts
subprojects {
    configurations.configureEach {
        // Validate on resolution
    }
}
```

**Recommended enforcement approach — module path rule in root `build.gradle.kts`:**
```kotlin
// root/build.gradle.kts
subprojects {
    val projectPath = this.path
    if (projectPath.startsWith(":feature:")) {
        configurations.all {
            withDependencies {
                forEach { dependency ->
                    if (dependency is ProjectDependency) {
                        val depPath = dependency.dependencyProject.path
                        require(!depPath.startsWith(":feature:")) {
                            "Module dependency violation: $projectPath must not depend on $depPath. " +
                            "Feature modules may only depend on :core:* modules."
                        }
                    }
                }
            }
        }
    }
}
```

**CI verification script (simpler alternative — catches at CI even if Gradle hook is imprecise):**
```bash
# .github/scripts/check-feature-deps.sh
# Fails if any feature module's build.gradle.kts references another :feature: module
if grep -rn 'project(":feature:' feature/*/build.gradle.kts | grep -v "^feature/"; then
  echo "ERROR: Feature-to-feature dependency detected"
  exit 1
fi
```

**Note:** The Gradle `withDependencies` approach above operates at configuration resolution time and reliably blocks the build. The CI script is a belt-and-suspenders check that also catches it in the workflow file diff review.

### Pattern 3: Hilt App-Level Wiring

**What:** `@HiltAndroidApp` on the `Application` class; `@AndroidEntryPoint` on `MainActivity`; each core module provides a `@Module @InstallIn(SingletonComponent::class)` object with placeholder bindings. This establishes the full DI graph at Phase 1 even though most bindings are empty stubs.

**When to use:** Set up once in Phase 1; bindings are filled in by subsequent phases.

**Example:**
```kotlin
// app/src/main/kotlin/com/example/aicompanion/AICompanionApp.kt
@HiltAndroidApp
class AICompanionApp : Application()

// app/src/main/kotlin/com/example/aicompanion/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavHost() }
    }
}

// core/domain/src/main/kotlin/com/example/aicompanion/core/domain/di/DomainModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    // Placeholder — repository bindings added in Phase 3+
}
```

### Pattern 4: AppNavHost with Route Constants

**What:** A single `NavHost` in `:app` wires all feature routes. Route constants are defined in each feature module — `AppNavHost` imports the constants but each feature module is responsible for its own route string.

**Example:**
```kotlin
// app/src/main/kotlin/com/example/aicompanion/navigation/AppNavHost.kt
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = VOICE_ROUTE) {
        composable(VOICE_ROUTE) { VoiceScreen() }
        composable(CHAT_ROUTE) { ChatScreen() }
        composable(AVATAR_ROUTE) { AvatarScreen() }
        composable(HOME_CONTROL_ROUTE) { HomeControlScreen() }
        composable(MEMORY_ROUTE) { MemoryScreen() }
        composable(TASKS_ROUTE) { TasksScreen() }
        composable(SETTINGS_ROUTE) { SettingsScreen() }
    }
}
```

### Pattern 5: Baseline Error Model (SCAF-06)

**What:** A sealed `AppError` hierarchy in `:core:domain` (pure Kotlin, no Android imports) that all modules reference. A `Logger` interface in `:core:domain` with a `TimberLogger` implementation in `:app` that is injected via Hilt. This is the "error model and logging conventions" required by SCAF-06.

**Example:**
```kotlin
// core/domain/src/main/kotlin/com/example/aicompanion/core/domain/error/AppError.kt
sealed class AppError(override val message: String) : Exception(message) {
    data class Network(val cause: Throwable) : AppError("Network error: ${cause.message}")
    data class Storage(val cause: Throwable) : AppError("Storage error: ${cause.message}")
    data class AudioPipeline(val reason: String) : AppError("Audio error: $reason")
    data class HomeAssistant(val reason: String) : AppError("HA error: $reason")
    data object Unknown : AppError("Unknown error")
}

// core/domain/src/main/kotlin/com/example/aicompanion/core/domain/logging/Logger.kt
interface Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String)
}
```

### Pattern 6: Compose Compiler Gradle Plugin (NOT kotlinCompilerExtensionVersion)

**What:** In Kotlin 2.x, the Compose compiler moves with the Kotlin release. Configure via the plugin, not via `kotlinCompilerExtensionVersion`.

**Example (root `build.gradle.kts` or convention plugin):**
```kotlin
plugins {
    alias(libs.plugins.compose.compiler) apply false
}

// In each module that uses Compose:
plugins {
    alias(libs.plugins.compose.compiler)
}
// That is all — no kotlinCompilerExtensionVersion needed
```

```toml
# libs.versions.toml [plugins] section
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### Anti-Patterns to Avoid

- **`kotlinCompilerExtensionVersion` in any `build.gradle.kts`:** This is deprecated in Kotlin 2.x. Any occurrence is a build error waiting to happen with future Kotlin upgrades.
- **`kapt` for Hilt or Room:** kapt is deprecated for Kotlin 2.x; all annotation processing must use `ksp(...)` directive.
- **Feature module build.gradle.kts with `implementation(project(":feature:X"))`:** Violates the module dependency ban. Gradle will reject at configuration time if the enforcement pattern from Pattern 2 is in place.
- **String literal route constants inline in `AppNavHost`:** Route strings must live in their feature module as constants so each module owns its own contract.
- **`security-crypto` or `EncryptedSharedPreferences` in any dependency block:** Fully deprecated; do not add to the catalog at all.
- **`kotlinCompilerExtensionVersion` in any `build.gradle.kts`**

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Version management across 15 modules | Custom version variable DSL | `libs.versions.toml` (Gradle Version Catalogs) | Version Catalogs are built into Gradle 7.4+; type-safe accessors generated automatically; IDE autocomplete works |
| Feature→feature ban enforcement | Git hooks or documentation conventions | Gradle `configurations.configureEach` with `withDependencies` check | Gradle-level enforcement fails the build at sync time — no human review required |
| Hilt DI wiring | Manual service locator or factory pattern | Hilt 2.57.1 with KSP | Hilt is compile-time safe; 15 modules need a DI framework to avoid constructor-param explosion |
| Kotlin code formatting | Custom checkstyle or lint rules | ktlint | ktlint is the official Kotlin formatter with zero custom configuration needed for baseline use |
| In-app navigation graph | Custom fragment manager or backstack logic | Navigation Compose 2.9.7 | Handles back stack, deep links, predictive back gesture automatically |
| Error type hierarchy | Exception subclasses scattered per module | Sealed `AppError` in `:core:domain` | Centralizes all error cases; compiler exhaustiveness checking on `when` expressions |
| Debug logging | `Log.d()` calls scattered with raw tags | Timber + Logger interface | Timber strips debug logs in release builds; interface lets tests inject a no-op logger |

**Key insight:** In a 15-module project, any versioning or dependency management "convention" that relies on humans following it will drift. All structural rules must be machine-enforced at build time.

---

## Common Pitfalls

### Pitfall 1: Cyclic Gradle Module Dependencies

**What goes wrong:** A feature module imports another feature module (e.g., `:feature:chat` imports `:feature:voice` to access the transcript). Gradle refuses to build with a circular dependency error. Recovery requires extracting shared types to `:core:domain`, updating all importers — rated HIGH cost (1-3 days).

**Why it happens:** Feature modules feel like natural sharing points. The cyclic import isn't obvious until the second cross-feature edge is added.

**How to avoid:** The Gradle `withDependencies` enforcement pattern must be installed in Phase 1 Wave 1 — before any feature code exists. Any `feature.*` module that declares `implementation(project(":feature:*"))` causes immediate build failure.

**Warning signs:** Gradle sync shows `Circular dependency between the following tasks`. A feature module's `build.gradle.kts` contains a reference to another `:feature:` path.

### Pitfall 2: kapt Instead of KSP for Hilt and Room

**What goes wrong:** Hilt is configured with `kapt("com.google.dagger:hilt-android-compiler:...")` instead of `ksp(...)`. The build works initially but kapt is officially deprecated for Kotlin 2.x and will fail with future Kotlin or AGP updates. Build times are also significantly slower.

**Why it happens:** Older documentation and tutorials still show kapt. Copying from a pre-2024 project scaffold introduces this silently.

**How to avoid:** Use only `ksp(libs.hilt.compiler)` and `ksp(libs.room.compiler)`. Never declare `kapt(...)` for any processor. Search the project for `kapt` before declaring Phase 1 complete.

**Warning signs:** `apply plugin: 'kotlin-kapt'` appears in any `build.gradle.kts`. Build warnings about kapt deprecation.

### Pitfall 3: kotlinCompilerExtensionVersion in build files

**What goes wrong:** The Compose compiler version is specified as `kotlinCompilerExtensionVersion = "..."` in the `composeOptions` block. This is incompatible with Kotlin 2.x where the Compose compiler ships with the Kotlin plugin.

**Why it happens:** Pre-2024 Android documentation and many templates still show this pattern. It works until Kotlin 2.0 compatibility is fully enforced, then breaks at upgrade time.

**How to avoid:** Use the Compose Compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) instead. No `composeOptions` block needed.

### Pitfall 4: Missing `settings.gradle.kts` Module Declarations

**What goes wrong:** A module directory is created with a `build.gradle.kts` but not added to `settings.gradle.kts`. Gradle ignores the module silently. CI passes on a partial project.

**Why it happens:** With 15 modules created in bulk, it is easy to miss adding one or more `include(":feature:memory")` lines.

**How to avoid:** Create a checklist: for every module directory created, immediately add its `include()` line to `settings.gradle.kts`. Run `./gradlew projects` after adding all modules to verify all 15 appear.

### Pitfall 5: Compose BOM Without Explicit Platform Dependency

**What goes wrong:** The BOM is declared in the version catalog but individual modules forget to declare `implementation(platform(libs.compose.bom))` before listing Compose artifacts. Version mismatches silently resolve to arbitrary Compose versions.

**Why it happens:** The BOM pattern requires an extra `platform(...)` wrapper that is easy to forget when copying dependency blocks between modules.

**How to avoid:** In any module that uses Compose, the FIRST dependency must be `implementation(platform(libs.compose.bom))`. Verify by running `./gradlew :app:dependencies` and confirming Compose versions all pin to BOM values.

### Pitfall 6: ktlint Gradle Plugin Version Incompatibility

**What goes wrong:** ktlint Gradle plugin version does not match the ktlint engine version expected by the AGP version. CI reports cryptic errors about task configuration.

**Why it happens:** The ktlint Gradle plugin (`org.jlleitschuh.gradle.ktlint`) has its own compatibility matrix with ktlint engine versions. Mismatched versions break the CI pipeline silently or noisily depending on which combination is chosen.

**How to avoid:** Use ktlint Gradle plugin 12.1.2 with the bundled ktlint engine version. Do not separately specify the ktlint engine version — let the plugin manage it.

---

## Code Examples

Verified patterns from official sources or widely established Android conventions:

### libs.versions.toml Plugin Block

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### settings.gradle.kts — All 15 Modules

```kotlin
// settings.gradle.kts
rootProject.name = "AICompanion"

include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:audio")
include(":core:ai")
include(":core:automation")
include(":feature:voice")
include(":feature:chat")
include(":feature:avatar")
include(":feature:memory")
include(":feature:homecontrol")
include(":feature:tasks")
include(":feature:settings")
```

### Root build.gradle.kts — Feature Dependency Ban

```kotlin
// Source: Gradle documentation — configurations.configureEach + withDependencies pattern
subprojects {
    val currentPath = path
    afterEvaluate {
        if (currentPath.startsWith(":feature:")) {
            configurations.configureEach {
                withDependencies {
                    forEach { dep ->
                        if (dep is ProjectDependency) {
                            val depPath = dep.dependencyProject.path
                            check(!depPath.startsWith(":feature:")) {
                                "Dependency violation: $currentPath -> $depPath is not allowed. " +
                                "Feature modules must only depend on :core:* modules."
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### GitHub Actions CI Workflow (Claude's Discretion — Recommended)

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ master, main ]
  pull_request:
    branches: [ master, main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v4

      - name: Check ktlint formatting
        run: ./gradlew ktlintCheck

      - name: Run Detekt static analysis
        run: ./gradlew detekt
        continue-on-error: true   # Warning-only in Phase 1

      - name: Build all modules
        run: ./gradlew build

      - name: Verify no feature-to-feature dependencies
        run: |
          if grep -rn 'project(":feature:' feature/*/build.gradle.kts 2>/dev/null; then
            echo "ERROR: Feature-to-feature dependency detected in build files"
            exit 1
          fi
```

### Hilt Application + MainActivity Wiring

```kotlin
// Source: developer.android.com/training/dependency-injection/hilt-android
@HiltAndroidApp
class AICompanionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())  // Only in debug builds — release: no-op tree
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AICompanionTheme {
                AppNavHost()
            }
        }
    }
}
```

### Core Module Hilt Module Stub

```kotlin
// core/domain/src/main/kotlin/com/example/aicompanion/core/domain/di/DomainModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DomainModule
// Placeholder — bindings added by phases that implement each domain contract
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `kapt` for Hilt/Room annotation processing | `ksp` (KSP2) | Kotlin 2.0 (2024) | kapt is deprecated; build times faster with KSP; KSP1 not compatible with Kotlin 2.3.x |
| `kotlinCompilerExtensionVersion` in `composeOptions` | Compose Compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) | Kotlin 2.0 (2024) | Compose compiler ships with Kotlin; separate version no longer exists |
| `androidx.security:security-crypto` / `EncryptedSharedPreferences` | Android Keystore API + DataStore AES-GCM | July 2025 (all APIs deprecated) | `security-crypto` 1.1.0 is fully deprecated; do not use in new code |
| Google AI Edge SDK | ML Kit Prompt API (`com.google.mlkit:genai-prompt`) | 2026 | AI Edge SDK deprecated; Gemini Nano accessed via ML Kit Prompt API |
| Gradle `buildSrc` for shared build logic | `build-logic` convention plugins module OR inline `libs.versions.toml` | Gradle 7.4+ (2022) | `buildSrc` recompiles on every change; `build-logic` or version catalogs are faster |
| Hardcoded version strings in `build.gradle.kts` | `libs.versions.toml` Version Catalogs with type-safe accessors | Gradle 7.4 (2022) | Standard for 5+ module projects; IDE autocomplete, single source of truth |
| OkHttp 4.x | OkHttp 5.0.0 | July 2025 | First stable 5.x release; 4.x is in maintenance mode |
| Retrofit 2.x | Retrofit 3.0.0 | 2025 | New major version; check for breaking changes in network layer setup |

**Deprecated/outdated to exclude from Phase 1:**
- `kapt` plugin: Use `ksp` only
- `kotlinCompilerExtensionVersion`: Removed entirely with Kotlin 2.x Compose
- `security-crypto`: Do not add to `libs.versions.toml` at all
- Google AI Edge SDK: Do not add; `genai-prompt` is the replacement (catalog only in Phase 1)
- OkHttp 4.x: Use 5.0.0
- Retrofit 2.x: Use 3.0.0

---

## Open Questions

1. **KSP 2.3.6 and Kotlin 2.3.20 compatibility confirmation**
   - What we know: KSP decoupled its version scheme from Kotlin versions in KSP2. KSP 2.3.6 is the latest release (Feb 2026). KSP1 is not compatible with Kotlin 2.3.0+.
   - What's unclear: The exact KSP 2.3.6 release notes do not explicitly state Kotlin 2.3.20 compatibility. The KSP numbering `2.3.x` suggests it tracks Kotlin `2.3.x` releases.
   - Recommendation: The first task in Phase 1 must be: attempt `./gradlew projects` with KSP 2.3.6 + Kotlin 2.3.20 and confirm the build configures without error. If KSP 2.3.6 does not work, check for a newer KSP release at `github.com/google/ksp/releases` at that time.

2. **build-logic convention plugin module vs. inline build.gradle.kts**
   - What we know: This is explicitly Claude's discretion. `build-logic` convention plugins keep individual module `build.gradle.kts` files short but add a setup cost.
   - What's unclear: For a greenfield Phase 1, the tradeoff favors inline `build.gradle.kts` (simpler, less setup) vs. convention plugins (better long-term maintenance with 15 modules).
   - Recommendation: Use inline `build.gradle.kts` per module in Phase 1. Each module's build file is roughly 30-40 lines and is readable. Convention plugins can be introduced in a later refactor if maintenance burden grows. This keeps Phase 1 scope focused.

3. **Retrofit 3.0.0 Breaking Changes**
   - What we know: Retrofit 3.0.0 is confirmed as latest stable. It is a major version bump from 2.x.
   - What's unclear: Whether Retrofit 3.0.0 has breaking API changes from 2.x that affect the `kotlinx-serialization` converter or OkHttp 5.0.0 integration.
   - Recommendation: Retrofit is catalog-only in Phase 1 (no network code written). Before Phase 2 (AI routing), verify the `retrofit2-kotlinx-serialization-converter` compatibility with Retrofit 3.0.0 and OkHttp 5.0.0. Flag this as a Phase 2 research item.

4. **ML Kit Prompt API Beta stability**
   - What we know: `com.google.mlkit:genai-prompt:1.0.0-beta1` is the current artifact. Beta APIs may have breaking changes between releases.
   - What's unclear: Whether there is a more stable path or whether Beta is the only option.
   - Recommendation: Add to `libs.versions.toml` in Phase 1 but do not implement. Before the AI routing phase, re-check the ML Kit Prompt API release status. This is a v2 requirement (ROUT-V2-01) anyway.

---

## Sources

### Primary (HIGH confidence)
- `developer.android.com/training/dependency-injection/hilt-android` — Hilt 2.57.1, KSP setup, `@HiltAndroidApp` pattern
- `developer.android.com/build/releases/gradle-plugin` — AGP 9.1.0, Gradle 9.3.1, JDK 17 requirement
- `kotlinlang.org/docs/releases.html` — Kotlin 2.3.20 release date (March 16, 2026) confirmed
- `developer.android.com/jetpack/androidx/releases/navigation` — Navigation Compose 2.9.7 stable
- `developer.android.com/jetpack/compose/bom/bom-mapping` — Compose BOM 2026.03.00
- `developer.android.com/jetpack/androidx/releases/security` — security-crypto 1.1.0 deprecated
- `developer.android.com/ai/gemini-nano/ml-kit-genai` — ML Kit GenAI Prompt API recommended path
- `developer.android.com/topic/modularization/patterns` — Feature→feature dependency ban, DAG enforcement
- `.planning/research/STACK.md` — Pre-verified AndroidX versions; verified sources documented there

### Secondary (MEDIUM confidence — verified via WebFetch during this research)
- `github.com/google/ksp/releases` (WebFetch) — KSP 2.3.6 confirmed as latest stable (Feb 2026); KSP1 incompatible with Kotlin 2.3.0+
- `github.com/Kotlin/kotlinx.coroutines/releases` (WebFetch) — 1.10.2 confirmed latest stable
- `github.com/Kotlin/kotlinx.serialization/releases` (WebFetch) — 1.10.0 confirmed latest stable
- `github.com/square/okhttp CHANGELOG.md` (WebFetch) — OkHttp 5.0.0 confirmed stable (July 2025)
- `github.com/square/retrofit/releases` (WebFetch) — Retrofit 3.0.0 confirmed latest stable
- `github.com/cashapp/turbine/releases` (WebFetch) — Turbine 1.2.1 confirmed latest stable
- `github.com/mockk/mockk/releases` (WebFetch) — MockK 1.14.9 confirmed latest stable
- `github.com/robolectric/robolectric/releases` (WebFetch) — Robolectric 4.14 confirmed, API 35 supported
- `github.com/airbnb/lottie-android/releases` (WebFetch) — Lottie 6.7.1 confirmed latest stable
- `developers.google.com/ml-kit/genai/prompt/android/get-started` (WebFetch) — ML Kit Prompt API artifact `com.google.mlkit:genai-prompt:1.0.0-beta1` confirmed

### Tertiary (LOW confidence — flag for validation)
- KSP 2.3.6 + Kotlin 2.3.20 compatibility: KSP release notes do not explicitly confirm; needs runtime verification in Phase 1 Wave 0
- Retrofit 3.0.0 compatibility with kotlinx-serialization converter: Major version — verify before network code is written in Phase 2

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all AndroidX versions pre-verified in STACK.md against official release pages; TO_BE_VERIFIED entries now resolved via WebFetch against GitHub release pages
- Architecture: HIGH — standard Android multi-module Clean Architecture with Hilt + Navigation Compose; fully documented official patterns
- Pitfalls: HIGH — cyclic dependency and kapt pitfalls are well-documented; Gradle enforcement patterns are standard
- TO_BE_VERIFIED resolution: MEDIUM — WebFetch confirmed versions from GitHub release pages; KSP compatibility with Kotlin 2.3.20 is inferred from version numbering, needs runtime verification

**Research date:** 2026-03-18
**Valid until:** 2026-04-18 (30 days — stable stack with official release cadence)
