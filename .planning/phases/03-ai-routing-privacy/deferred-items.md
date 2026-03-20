# Deferred Items — Phase 03

## Pre-existing Build Issues (Out of Scope)

### AGP 9.1.0 + kotlin.android plugin + Hilt 2.57.1 compatibility
- **Symptom:** `Failed to apply plugin 'org.jetbrains.kotlin.android'` in `app/build.gradle.kts`; removing the plugin breaks Hilt (`Android BaseExtension not found`)
- **Affected:** `app/build.gradle.kts` specifically; library modules appear unaffected
- **Status:** Partially addressed 2026-03-19 — `ksp` plugin moved before `hilt` in `app/build.gradle.kts` (KSP must precede Hilt for proper annotation processor registration). If the `kotlin.android` application error persists, upgrade Hilt to a version verified compatible with AGP 9.x.
- **Remaining fix (if still needed):** Upgrade `hilt` from 2.57.1 to the latest AGP 9.x-compatible release

### ROUT-01 / ROUT-02 Documentation Gap

- **Status:** Resolved 2026-03-19 — REQUIREMENTS.md traceability table updated; 03-VERIFICATION.md Requirements Coverage table updated. Both requirements were already satisfied in code.

### Root build.gradle.kts — ProjectDependency.dependencyProject API removed in Gradle 9
- **Symptom:** `Unresolved reference 'dependencyProject'` on line 27 of root build.gradle.kts
- **Status:** Fixed in 03-02 (auto-fixed, Rule 1 Bug) — updated to use `withType(ProjectDependency::class.java)` and `.path` directly
