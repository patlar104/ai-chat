# AI Companion App

## What This Is

A personal Android Kotlin app that combines three roles in one system: a voice-first AI companion, a device control command center, and a remote task operator. Built for personal daily use, the app understands spoken commands, controls smart-home devices and routines, assists in the background with briefings and automations, and presents an expressive AI avatar with realistic voice conversation.

## Core Value

The assistant must handle real daily commands quickly and reliably — if voice command execution, home control, and spoken replies don't work offline, nothing else matters.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] User can issue voice commands via button-to-talk
- [ ] Assistant parses commands deterministically for common actions
- [ ] Assistant executes named home control routines and device commands
- [ ] Assistant replies with spoken TTS confirmation
- [ ] Avatar visually reflects voice session states (idle, listening, thinking, speaking, error)
- [ ] User can set and retrieve reminders
- [ ] Conversation history persists across sessions
- [ ] Device and room aliases resolve correctly
- [ ] Hybrid AI router escalates to cloud for open-ended reasoning
- [ ] Background briefings and scheduled automations run via WorkManager
- [ ] Memory stores user preferences, aliases, and notable facts
- [ ] User can review and delete stored memories
- [ ] Privacy mode prevents cloud requests when disabled
- [ ] App functions in reduced mode when offline

### Out of Scope

- Always-on hotword detection — battery impact not yet validated
- Broad cross-app UI automation — too complex as a core dependency for v1
- Fully autonomous purchasing or high-risk transactions — confirmation model not yet built
- Full 3D avatar realism — delays real-world usefulness
- Multi-user shared household intelligence — personal use first
- Cross-platform (iOS, KMP) — Android-native first

## Context

The spec pack (repo_ready_spec_pack.md, starter_android_project_pack.md, ai_companion_spec_workflow.md) provides a fully designed architecture, module blueprint, voice state machine, tool contracts, data models, and 7-phase delivery plan. The starter scaffold includes placeholder Kotlin files for all 15 modules.

Key technical decisions already made in the spec:
- **Platform:** Android Kotlin, Jetpack Compose + Material 3, Navigation Compose
- **Architecture:** Modular monolith, Clean Architecture, MVVM (MVI for voice/chat flows)
- **Modules:** app, core.{ui,domain,data,network,audio,ai,automation}, feature.{voice,chat,avatar,memory,homecontrol,tasks,settings}
- **Storage:** Room + DataStore for local persistence
- **AI routing:** Deterministic parser → local AI → cloud AI (hybrid, privacy-aware)
- **Home control:** Home Assistant as primary integration fabric
- **TTS:** Two-tier — local Android TTS for utility, cloud neural TTS for companion mode
- **Avatar:** 2D expressive states first (presence before realism)
- **STT:** Platform Android speech services as baseline

Anti-hallucination rules from constitution: Never invent dependency versions or SDK versions. Use `TO_BE_VERIFIED`, `OPTIONAL_IF_SUPPORTED`, `SELECT_FROM_EXISTING_REPO`, or `USER_DECISION_REQUIRED` for unknowns.

## Constraints

- **Platform:** Android Kotlin only — no cross-platform until Android quality is proven
- **Versions:** Never invent Gradle plugin, SDK, or library versions — mark as `TO_BE_VERIFIED`
- **Privacy:** User data local by default; long-term memory promotion must be explicit or reviewable
- **Performance:** Cold start mobile-friendly; simple commands < 2s in local mode; avatar must not impair voice responsiveness
- **Offline:** Core commands, home control (local), reminders, local TTS, and memory must work without network
- **Architecture:** No network calls from UI modules; no business logic in composables; all tool execution via typed contracts

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Android-native Kotlin only | Cross-platform adds complexity before value is proven | — Pending |
| Hybrid AI routing (deterministic → local → cloud) | Balances speed, privacy, and intelligence | — Pending |
| Home Assistant as home control fabric | Broad device support, established ecosystem | — Pending |
| Two-tier TTS (local utility + cloud companion) | Speed for confirmations, quality for conversation | — Pending |
| 2D avatar first | Presence before realism; don't let avatar delay daily usefulness | — Pending |
| Modular monolith (15 modules) | Replaceable AI/audio/avatar without full rewrite | — Pending |
| Room + DataStore | Standard Android persistence, no over-engineering | — Pending |

---
*Last updated: 2026-03-18 after initialization*
