---
phase: 02-voice-home-control-mvp
plan: 02
subsystem: core/ui + app/navigation
tags: [theme, design-system, navigation, compose, material3]
dependency_graph:
  requires: []
  provides: [AICompanionTheme, Spacing, AppColors, BottomNavBar]
  affects: [all feature screens, MainActivity, AppNavHost]
tech_stack:
  added: [material-icons-extended (via compose-bom)]
  patterns: [darkColorScheme, MaterialTheme, Scaffold+NavigationBar, popUpTo back-stack management]
key_files:
  created:
    - core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Color.kt
    - core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Spacing.kt
    - app/src/main/kotlin/com/example/aicompanion/navigation/BottomNavBar.kt
  modified:
    - core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Theme.kt
    - app/src/main/kotlin/com/example/aicompanion/navigation/AppNavHost.kt
    - app/src/main/kotlin/com/example/aicompanion/MainActivity.kt
    - app/build.gradle.kts
    - gradle/libs.versions.toml
decisions:
  - AppNavHost navController changed from defaulted rememberNavController() to required parameter — caller (MainActivity) owns the NavController instance to share it with BottomNavBar
  - material-icons-extended added to version catalog as compose-bom-managed dependency (no explicit version)
  - enableEdgeToEdge() removed from MainActivity in favor of Scaffold-managed insets via innerPadding
metrics:
  duration: 2m
  completed_date: "2026-03-18"
  tasks_completed: 2
  files_created: 3
  files_modified: 5
---

# Phase 02 Plan 02: AICompanionTheme and Bottom Navigation Shell Summary

**One-liner:** Dark Material3 theme (AppColors/Spacing/Typography) wired into Scaffold with 5-destination NavigationBar using popUpTo back-stack management.

---

## Tasks Completed

| Task | Name | Commit | Key Files |
|------|------|--------|-----------|
| 1 | Create AICompanionTheme with dark color scheme, typography, and spacing tokens | 3f78eaf | Theme.kt, Color.kt, Spacing.kt |
| 2 | Wire bottom NavigationBar into Scaffold and update AppNavHost | 8e559c2 | BottomNavBar.kt, AppNavHost.kt, MainActivity.kt |

---

## What Was Built

**Task 1 — Theme Foundation:**
- `Color.kt`: `AppColors` object with 11 color tokens matching UI-SPEC (background `#111318`, accent `#4F8EF7`, error `#E05353`, audit-success `#4CAF50`, and 7 more)
- `Spacing.kt`: `Spacing` object with 7 dp tokens (xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp, xxl=48dp, xxxl=64dp)
- `Theme.kt`: `AICompanionTheme` composable wrapping `MaterialTheme` with `darkColorScheme()` and custom `Typography` (4 roles: bodyLarge 16sp, labelMedium 14sp, headlineSmall 20sp, displaySmall 28sp)

**Task 2 — Navigation Shell:**
- `BottomNavBar.kt`: Material3 `NavigationBar` with 5 `NavigationBarItem` entries (Voice/Mic, Chat/Chat, Home/Home, Tasks/Alarm, Settings/Settings). Selected/unselected icon variants. `popUpTo(startDestinationId)` with `saveState/restoreState` prevents back-stack buildup.
- `AppNavHost.kt`: Signature updated — `navController: NavHostController` now required (no default), `modifier: Modifier = Modifier` added. All 7 `composable()` routes unchanged.
- `MainActivity.kt`: Replaced bare `AppNavHost()` call with `AICompanionTheme { Scaffold(bottomBar = { BottomNavBar(...) }) { ... AppNavHost(modifier = innerPadding) } }`. `@AndroidEntryPoint` retained.
- `libs.versions.toml` + `app/build.gradle.kts`: Added `compose-material-icons-extended` catalog entry and implementation dependency.

---

## Decisions Made

1. **AppNavHost navController as required parameter:** Changed from `= rememberNavController()` default to required. MainActivity creates one shared `NavHostController` instance used by both `BottomNavBar` and `AppNavHost` — if each created their own instance, navigation state would diverge.

2. **material-icons-extended via version catalog:** Added as a catalog entry (no version — managed by compose-bom). Keeps icon dependency consistent with the rest of Compose.

3. **enableEdgeToEdge() removed:** Original `MainActivity` called `enableEdgeToEdge()`. The new `Scaffold` provides `innerPadding` via `WindowInsets` automatically. Retaining both would cause double-padding. Scaffold's approach is the correct Material3 pattern.

---

## Deviations from Plan

**1. [Rule 2 - Missing critical functionality] Added material-icons-extended to version catalog**
- **Found during:** Task 2
- **Issue:** `libs.versions.toml` had no entry for `material-icons-extended` — required for `BottomNavBar.kt` imports (`Icons.Filled.Mic`, etc.)
- **Fix:** Added `compose-material-icons-extended` catalog entry in `libs.versions.toml` and `implementation(libs.compose.material.icons.extended)` in `app/build.gradle.kts`
- **Files modified:** `gradle/libs.versions.toml`, `app/build.gradle.kts`
- **Commit:** 8e559c2

**2. [Rule 1 - Bug] Removed enableEdgeToEdge() conflict**
- **Found during:** Task 2
- **Issue:** `enableEdgeToEdge()` in the original `MainActivity` would conflict with Scaffold's `innerPadding` inset handling, causing content to be double-padded at the bottom nav bar edge
- **Fix:** Removed `enableEdgeToEdge()` call — Scaffold handles window insets correctly via `innerPadding`
- **Files modified:** `app/src/main/kotlin/com/example/aicompanion/MainActivity.kt`
- **Commit:** 8e559c2

---

## Self-Check: PASSED

Files verified:
- core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Color.kt — FOUND
- core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Spacing.kt — FOUND
- core/ui/src/main/kotlin/com/example/aicompanion/core/ui/Theme.kt — FOUND (updated)
- app/src/main/kotlin/com/example/aicompanion/navigation/BottomNavBar.kt — FOUND
- app/src/main/kotlin/com/example/aicompanion/navigation/AppNavHost.kt — FOUND (updated)
- app/src/main/kotlin/com/example/aicompanion/MainActivity.kt — FOUND (updated)

Commits verified:
- 3f78eaf — FOUND (feat(02-02): create AICompanionTheme...)
- 8e559c2 — FOUND (feat(02-02): wire bottom NavigationBar...)
