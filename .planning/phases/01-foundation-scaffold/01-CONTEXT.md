# Phase 1: Foundation & Scaffold - Context

**Gathered:** 2026-03-18
**Status:** Ready for planning

<domain>
## Phase Boundary

A compilable, correctly structured 15-module Android project with all architectural guardrails in place before any feature code is written. This phase delivers the build skeleton, Hilt DI graph, Navigation host, CI pipeline, and module dependency enforcement rules. No user-facing features are built in this phase. Phase 2 is the first phase that produces something a user can run.

</domain>

<decisions>
## Implementation Decisions

### Dependency Versioning Strategy
- Use `libs.versions.toml` (Gradle version catalog) for ALL dependency versions — single source of truth
- Verified versions pre-filled at project init: Kotlin 2.3.20, Compose BOM 2026.03.00, Hilt 2.57.1, Room 2.8.4, DataStore 1.2.1, WorkManager 2.11.1, Navigation Compose 2.9.7, Lifecycle 2.10.0, Media3 ExoPlayer 1.9.2
- Versions marked `TO_BE_VERIFIED` in research (OkHttp, Retrofit, kotlinx-coroutines, kotlinx-serialization, KSP, ML Kit Prompt API, Lottie, Turbine, MockK, Robolectric) must be resolved as the FIRST task in Phase 1 by checking official GitHub release pages before any module code is written
- `security-crypto` / `EncryptedSharedPreferences` is fully deprecated — DO NOT add it. Use Android Keystore API directly + DataStore for encrypted storage.
- Google AI Edge SDK is deprecated — DO NOT add it. On-device AI path is ML Kit Prompt API (Gemini Nano via AICore).
- Compose Compiler is configured via the Compose Compiler Gradle plugin (NOT `kotlinCompilerExtensionVersion` — that is deprecated in Kotlin 2.x)

### Module Creation Scope
- All 15 modules created in Phase 1 with placeholder implementations (matching the starter scaffold from provided spec documents)
- Module list: `:app`, `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:audio`, `:core:ai`, `:core:automation`, `:feature:voice`, `:feature:chat`, `:feature:avatar`, `:feature:memory`, `:feature:homecontrol`, `:feature:tasks`, `:feature:settings`
- Each module has a compilable `build.gradle.kts`, correct namespace, and at least one placeholder Kotlin file
- `settings.gradle.kts` includes all 15 modules from day 1

### Module Dependency Enforcement
- Feature→feature imports are BANNED — a `:feature:X` module must not depend on another `:feature:Y` module
- Enforcement via Gradle configuration: each feature module only declares allowed dependencies (`core.*` and `app` as permitted callers); any feature→feature dependency causes a Gradle configuration error at sync time
- This rule must be verified in CI: a test that confirms the dependency graph has no feature→feature edges
- Enforcement is established in Phase 1 and never relaxed

### Hilt Dependency Injection
- Hilt 2.57.1 with KSP (NOT kapt — kapt is legacy)
- `@HiltAndroidApp` on `AICompanionApp` in `:app` module
- `@AndroidEntryPoint` wired on `MainActivity`
- Each core module exposes a Hilt module (`@Module @InstallIn`) so feature modules can inject without additional wiring
- DI graph covers at minimum: placeholder bindings for repository interfaces in `:core:domain`

### Navigation
- Navigation Compose 2.9.7
- Single `AppNavHost` composable in `:app` module routes to all feature screens
- Each feature module exposes a navigation route constant (not a string literal inline)
- Phase 1 routes are all placeholder screens — each feature screen shows the feature name as a `Text` composable

### CI Baseline
- ktlint for Kotlin formatting enforcement — fails CI on violations
- Detekt for static analysis — runs on every push, initially warning-only to avoid blocking on new project
- Gradle `build` task validates all 15 modules compile from clean checkout
- CI runs on every push (GitHub Actions or equivalent — implementation detail for planner)
- No test coverage requirement in Phase 1 (tests are added per feature in subsequent phases)

### Baseline Conventions (from constitution)
- Package name: `com.example.aicompanion` (or user-specified — planner to confirm)
- `compileSdk = TO_BE_VERIFIED` (resolve to current stable Android SDK in Phase 1 kickoff)
- `minSdk = TO_BE_VERIFIED` (must support voice, foreground services, Compose — likely API 26 or 28 minimum; planner to verify and document decision)
- `targetSdk = TO_BE_VERIFIED` (resolve to compileSdk)

### Claude's Discretion
- Exact CI provider and workflow file structure (GitHub Actions YAML, Bitrise, etc.)
- Whether to include a `build-logic` convention plugin module or keep `build.gradle.kts` inline per module for Phase 1
- Test infrastructure setup (unit test dependencies can be added but no tests written in Phase 1)
- Exact Detekt configuration rules

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Requirements
- `.planning/REQUIREMENTS.md` — SCAF-01 through SCAF-06 define the Phase 1 acceptance criteria
- `.planning/PROJECT.md` — Constraints section: Android-native first, no invented versions, local-first privacy
- `.planning/research/STACK.md` — Verified technology stack with confirmed versions and TO_BE_VERIFIED flags

### Architecture Rules
- `.planning/research/ARCHITECTURE.md` — Module dependency DAG, boundary rules, build order implications
- `.planning/research/PITFALLS.md` — Cyclic Gradle dependencies pitfall (highest recovery cost — must prevent from Phase 1)

### Research Summary
- `.planning/research/SUMMARY.md` — Executive summary with stack decisions, critical deprecation warnings (security-crypto, Google AI Edge SDK, kotlinCompilerExtensionVersion)

### Spec Documents (from user-provided idea documents)
- The constitution rules from `ai_companion_spec_workflow.md` section 2 define hard rules:
  - No invented versions (use TO_BE_VERIFIED)
  - No fabricated compatibility claims
  - No direct network calls from UI modules
  - No business logic in composables
  - All tool execution via typed contracts

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- None — this is a greenfield project. The user-provided starter scaffold documents define the target structure.

### Established Patterns
- The starter scaffold (`starter_android_project_pack.md`) provides placeholder Kotlin files for all 15 modules — these are the implementation targets for Phase 1.
- The spec (`ai_companion_spec_workflow.md`) defines module responsibilities and boundary rules.

### Integration Points
- `:app` is the assembly point — all 15 modules flow into it
- `AppNavHost` in `:app` is the integration point where feature modules register their screens
- Hilt DI graph in `:app:di/AppModule.kt` is where all bindings are wired

</code_context>

<specifics>
## Specific Ideas

- The starter scaffold document contains ready-to-use placeholder Kotlin files for all 15 modules. The planner should use these as the target file contents for Phase 1 tasks rather than writing new implementations from scratch.
- The spec constitution explicitly says: "If a version is not explicitly known from a trusted source or a repository file, the builder must leave the version blank or mark it as TO_BE_VERIFIED." This applies to every `build.gradle.kts` during Phase 1.
- Cyclic dependency prevention is the single most important enforcement mechanism — research confirmed this has the highest recovery cost (1-3 days) if not established from the first commit.

</specifics>

<deferred>
## Deferred Ideas

- None — Phase 1 is purely infrastructure; discussion stayed within phase scope.

</deferred>

---

*Phase: 01-foundation-scaffold*
*Context gathered: 2026-03-18*
