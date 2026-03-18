---
plan: 01-03
status: complete
phase: 01-foundation-scaffold
subsystem: ci-pipeline
tags: [github-actions, detekt, ktlint, ci, feature-ban]
dependency_graph:
  requires: [01-01]
  provides: [ci-pipeline, detekt-config, feature-dep-check]
  affects: [all-15-modules]
tech_stack:
  added: [github-actions-v4, detekt-yml-config]
  patterns: [blocking-ktlint, warning-only-detekt, belt-and-suspenders-feature-ban]
key_files:
  created:
    - .github/workflows/ci.yml
    - .github/scripts/check-feature-deps.sh
    - detekt.yml
  modified: []
decisions:
  - "ktlint is blocking in CI — formatting violations fail the pipeline immediately, no continue-on-error"
  - "detekt is warning-only in Phase 1 (continue-on-error: true) — tightened to blocking in Phase 6"
  - "feature-dep check runs at both Gradle config time (withDependencies) and CI file-level (bash script) for belt-and-suspenders protection"
  - "FunctionNaming ignoreAnnotated: Composable allows VoiceScreen, AppNavHost naming conventions"
metrics:
  duration_minutes: 5
  completed_date: "2026-03-18"
  tasks_completed: 2
  files_created: 3
  files_modified: 0
---

# Phase 1 Plan 03: CI Pipeline + Detekt Config Summary

**One-liner:** GitHub Actions CI pipeline with blocking ktlint, warning-only detekt, and belt-and-suspenders feature-to-feature dependency enforcement via Gradle hook + bash script.

## Files Created

- `.github/workflows/ci.yml` — GitHub Actions CI pipeline
- `.github/scripts/check-feature-deps.sh` — Feature-to-feature dep check script (executable)
- `detekt.yml` — Detekt Phase 1 baseline configuration

## CI Pipeline Steps (in order)

1. Checkout (actions/checkout@v4)
2. Set up JDK 17 (temurin distribution)
3. Set up Gradle (gradle/actions/setup-gradle@v4)
4. ktlintCheck — BLOCKING (no continue-on-error)
5. detekt — WARNING-ONLY (continue-on-error: true)
6. build — validates all 15 modules compile
7. check-feature-deps.sh — belt-and-suspenders feature ban check

## Enforcement Model

- ktlint: blocking (formatting violations fail CI immediately)
- detekt: warning-only in Phase 1 (tightened in Phase 6)
- feature-dep script: blocking (any :feature: -> :feature: dep fails CI)

## Detekt Key Config

- warningsAsErrors: false (warning-only)
- FunctionNaming ignoreAnnotated: Composable (allows VoiceScreen, AppNavHost etc.)
- All rule weights set to 0 (Phase 1 permissive baseline)
- LongMethod threshold: 60 lines
- LongParameterList: function 6, constructor 7

## build.gradle.kts State

No changes required — Plan 01-01 already established:
- withDependencies feature ban block (intact, verified)
- apply(plugin = "io.gitlab.arturbosch.detekt") (intact, verified)
- tasks.withType<Detekt> block with config.setFrom("detekt.yml") (intact, verified)

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

Files confirmed present on disk:
- FOUND: .github/workflows/ci.yml
- FOUND: .github/scripts/check-feature-deps.sh (executable)
- FOUND: detekt.yml

Verification checks passed:
- ktlintCheck appears exactly 1 time in ci.yml
- check-feature-deps.sh appears exactly 1 time in ci.yml
- detekt.yml contains warningsAsErrors: false (1 match)
- withDependencies present in build.gradle.kts (feature ban intact)
- ktlintCheck has no continue-on-error (blocking)
- detekt has continue-on-error: true (warning-only)

Commits confirmed:
- 85532ed: ci(01-03): add GitHub Actions CI workflow and feature-dep check script
- 4c2af45: chore(01-03): add detekt.yml Phase 1 baseline configuration
