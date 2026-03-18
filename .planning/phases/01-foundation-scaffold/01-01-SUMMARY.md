---
phase: 01-foundation-scaffold
plan: 01-01
status: complete
subsystem: gradle-scaffold
tags: [gradle, version-catalog, build-config, architecture]
dependency_graph:
  requires: []
  provides: [version-catalog, module-declarations, feature-ban, gradle-wrapper]
  affects: [all-15-modules]
tech_stack:
  added: [kotlin-2.3.20, agp-9.1.0, ksp-2.3.6, compose-bom-2026.03.00, hilt-2.57.1, room-2.8.4, detekt-1.23.8, ktlint-12.1.2]
  patterns: [version-catalog, feature-feature-ban, ksp-only-annotation-processing]
key_files:
  created:
    - gradle/libs.versions.toml
    - settings.gradle.kts
    - build.gradle.kts
    - gradle/wrapper/gradle-wrapper.properties
    - .gitignore
  modified: []
decisions:
  - "KSP-only annotation processing — no kapt references anywhere"
  - "Compose Compiler configured via plugin alias, not kotlinCompilerExtensionVersion"
  - "ML Kit GenAI in catalog but inactive until Phase 3"
  - "Lottie in catalog but inactive until Phase 5"
metrics:
  duration_minutes: 5
  completed_date: "2026-03-18"
  tasks_completed: 5
  files_created: 5
  files_modified: 0
---

# Phase 1 Plan 01: Gradle Root Scaffold Summary

**One-liner:** Gradle root scaffold with version catalog (30+ deps pinned), 15-module declarations, feature-to-feature dependency ban, and Gradle 9.3.1 wrapper.

## Files Created

- `gradle/libs.versions.toml` — version catalog with all 30+ dependencies pinned, single source of truth for all 15 modules
- `settings.gradle.kts` — 15 module include declarations (:app, :core:*, :feature:*)
- `build.gradle.kts` — root build config with feature->feature ban via withDependencies check + detekt project-wide config
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.3.1 pinned
- `.gitignore` — standard Android ignores, preserves gradle wrapper files

## Versions Pinned

| Library | Version |
|---------|---------|
| Kotlin | 2.3.20 |
| AGP | 9.1.0 |
| KSP | 2.3.6 |
| Compose BOM | 2026.03.00 |
| Hilt | 2.57.1 |
| HiltAndroidx | 1.3.0 |
| Room | 2.8.4 |
| Navigation Compose | 2.9.7 |
| Lifecycle | 2.10.0 |
| Coroutines | 1.10.2 |
| OkHttp | 5.0.0 |
| Retrofit | 3.0.0 |
| Detekt | 1.23.8 |
| ktlint | 12.1.2 |
| Timber | 5.0.1 |
| Turbine | 1.2.1 |
| MockK | 1.14.9 |
| Robolectric | 4.14 |

## Architectural Ban Installed

Feature->feature import ban installed in root `build.gradle.kts` via `withDependencies` check. Any `:feature:*` module depending on another `:feature:*` module will fail Gradle configuration immediately with an explicit error message directing developers to move shared types to `:core:domain`.

## Module Declarations (15 total)

- `:app`
- `:core:ui`, `:core:domain`, `:core:data`, `:core:network`, `:core:audio`, `:core:ai`, `:core:automation`
- `:feature:voice`, `:feature:chat`, `:feature:avatar`, `:feature:memory`, `:feature:homecontrol`, `:feature:tasks`, `:feature:settings`

## Deprecated Patterns — Confirmed Absent

- `security-crypto`: NOT present (deprecated EncryptedSharedPreferences)
- `kapt`: NOT present (KSP-only processing)
- `kotlinCompilerExtensionVersion`: NOT present (Compose Compiler plugin used instead)
- Google AI Edge SDK: NOT present (ML Kit Prompt API only)

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

All files confirmed present on disk:
- FOUND: gradle/libs.versions.toml
- FOUND: settings.gradle.kts
- FOUND: build.gradle.kts
- FOUND: gradle/wrapper/gradle-wrapper.properties
- FOUND: .gitignore
- FOUND: .planning/phases/01-foundation-scaffold/01-01-SUMMARY.md

All commits confirmed in git log:
- 13c1447: chore(01-01): add standard Android .gitignore entries
- 59fab4a: chore(01-01): add settings.gradle.kts, root build.gradle.kts, and gradle wrapper
- 122c548: chore(01-01): add gradle/libs.versions.toml version catalog
