---
plan: 01-06
phase: 01-foundation-scaffold
subsystem: core-domain
status: complete
tags: [error-model, logging, hilt, sealed-class, interface]
dependency_graph:
  requires: [01-04, 01-05]
  provides: [AppError, Logger, TimberLogger, AppModule]
  affects: [all-modules]
tech_stack:
  added: []
  patterns: [sealed-error-hierarchy, injectable-logger, hilt-binds-abstract-class]
key_files:
  created:
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/error/AppError.kt
    - core/domain/src/main/kotlin/com/example/aicompanion/core/domain/logging/Logger.kt
    - app/src/main/kotlin/com/example/aicompanion/logging/TimberLogger.kt
    - app/src/main/kotlin/com/example/aicompanion/di/AppModule.kt
  modified: []
decisions:
  - "AppModule uses abstract class (not object) — required for @Binds in Hilt; object cannot have abstract members"
  - "Logger interface placed in :core:domain with no Timber import — keeps domain layer pure Kotlin and testable without Android dependencies"
  - "TimberLogger placed in :app — Timber dependency only exists in :app, not in :core:domain"
metrics:
  duration: ~5m
  completed: 2026-03-18
  tasks_completed: 2
  files_created: 4
  files_modified: 0
---

# Phase 1 Plan 6: Baseline Error Model + Logger Summary

Established project-wide sealed error hierarchy (AppError) and injectable logging contract (Logger) in :core:domain, with TimberLogger bound via Hilt @Binds in :app's AppModule abstract class.

## Files Created

- `core/domain/src/main/kotlin/com/example/aicompanion/core/domain/error/AppError.kt`
- `core/domain/src/main/kotlin/com/example/aicompanion/core/domain/logging/Logger.kt`
- `app/src/main/kotlin/com/example/aicompanion/logging/TimberLogger.kt`
- `app/src/main/kotlin/com/example/aicompanion/di/AppModule.kt`

## AppError Sealed Class

5 subtypes: `Network(cause: Throwable)`, `Storage(cause: Throwable)`, `AudioPipeline(reason: String)`, `HomeAssistant(reason: String)`, `Unknown` (data object).

All pure Kotlin — no Android or Timber imports in :core:domain. `when(error)` expressions in UI/presentation layers are exhaustively checked by the compiler.

## Logger Interface

4 methods: `d()`, `e()`, `w()`, `i()`. Pure Kotlin interface — no Timber dependency in :core:domain. Tests can inject a no-op or capturing logger without any Android runtime dependency.

## Hilt Binding

AppModule is `abstract class` (required for `@Binds` — an `object` cannot have abstract members and would fail KSP code generation).

`@Binds @Singleton abstract fun bindLogger(impl: TimberLogger): Logger`

Any `@Inject constructor` receiving `Logger` will get `TimberLogger` at runtime. Timber.DebugTree is planted in `AICompanionApp.onCreate()` for debug builds only.

## Decisions Made

1. `AppModule` must be `abstract class`, not `object` — Hilt `@Binds` requires abstract members.
2. `Logger` interface has no Timber import — keeps :core:domain pure Kotlin, enables test injection without Android runtime.
3. `TimberLogger` lives in `:app` — Timber is only declared as a dependency in `app/build.gradle.kts`.

## Deviations from Plan

None - plan executed exactly as written.

## Phase 1 Complete

All 6 SCAF requirements are now addressed across the 6 plans of Phase 1:

- SCAF-01: 15 modules compile (Plans 01-01, 01-02)
- SCAF-02: Feature-to-feature dependency ban enforced (Plan 01-01)
- SCAF-03: CI pipeline with ktlint + detekt + build (Plan 01-03)
- SCAF-04: Hilt DI wired end-to-end (Plan 01-04)
- SCAF-05: Navigation host wired to all 7 screens (Plan 01-05)
- SCAF-06: AppError + Logger baseline (Plan 01-06)

## Self-Check: PASSED
