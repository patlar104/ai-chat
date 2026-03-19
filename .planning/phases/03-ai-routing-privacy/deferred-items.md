# Deferred Items — Phase 03

## Pre-existing Build Issues (Out of Scope)

### AGP 9.1.0 + kotlin.android plugin + Hilt 2.57.1 compatibility
- **Symptom:** `Failed to apply plugin 'org.jetbrains.kotlin.android'` in `app/build.gradle.kts`; removing the plugin breaks Hilt (`Android BaseExtension not found`)
- **Affected:** `app/build.gradle.kts` specifically; library modules appear unaffected
- **Status:** Pre-existing — not introduced by 03-02 changes; root cause is version incompatibility between AGP 9.1.0 and Hilt 2.57.1
- **Fix needed:** Upgrade Hilt to a version compatible with AGP 9.x OR configure the kotlin.android plugin application order correctly

### Root build.gradle.kts — ProjectDependency.dependencyProject API removed in Gradle 9
- **Symptom:** `Unresolved reference 'dependencyProject'` on line 27 of root build.gradle.kts
- **Status:** Fixed in 03-02 (auto-fixed, Rule 1 Bug) — updated to use `withType(ProjectDependency::class.java)` and `.path` directly
