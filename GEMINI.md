# GEMINI.md — AI Companion App Context

## Project Overview
**AI Companion App** is a personal, voice-first Android application built with Kotlin and Jetpack Compose. It serves three primary roles:
1.  **Voice-first AI Companion:** Provides an expressive AI avatar with realistic voice conversation.
2.  **Home Control Command Center:** Integrates with **Home Assistant** to control smart-home devices and routines.
3.  **Task & Memory Operator:** Assists with briefings, reminders, and automations, maintaining a persistent user memory.

The project is designed for **privacy and offline reliability**, using a hybrid AI routing system (Deterministic Parser → Local AI → Cloud AI) and keeping core functionality (home control, reminders, local TTS) functional without a network.

## Architecture & Tech Stack
The project follows a **Modular Monolith** architecture with **Clean Architecture** principles and **MVVM/MVI** patterns.

### Modules (15 Total)
-   **`:app`**: Application entry point, Hilt setup, and global navigation.
-   **`:core:*`**: Shared infrastructure (`ui`, `domain`, `data`, `network`, `audio`, `ai`, `automation`).
-   **`:feature:*`**: Decoupled feature modules (`voice`, `chat`, `avatar`, `memory`, `homecontrol`, `tasks`, `settings`).

### Key Technologies
-   **UI**: Jetpack Compose, Material 3, Navigation Compose.
-   **DI**: Hilt (Dagger).
-   **Persistence**: Room (Database), DataStore (Preferences), Android Keystore (Encryption).
-   **Networking**: Retrofit, OkHttp.
-   **Background Tasks**: WorkManager, AlarmManager.
-   **AI/ML**: ML Kit GenAI (Local), Cloud AI (Hybrid Routing).
-   **Audio**: Android Speech Services (STT), Media3 (Audio), Two-tier TTS (Local + Cloud).
-   **Animation**: Lottie (for Avatar).

## Development Conventions

### Architectural Rules
-   **Feature Isolation**: Feature modules must **NOT** depend on other feature modules. Violation fails the build.
-   **Shared Logic**: All shared business logic, models, and interfaces must reside in `:core:domain`.
-   **No UI Logic**: Business logic is strictly prohibited in Composables; use ViewModels and StateHolders.
-   **Clean Networking**: No network calls are allowed from UI modules; all execution must go through typed repository contracts.

### Code Quality & Standards
-   **Kotlin**: Version 2.1.20+, JVM 17.
-   **Logging**: Use `Timber`.
-   **Linting**: **Ktlint** (formatting) and **Detekt** (static analysis) are enforced.
-   **Testing**: JUnit 4, MockK, Turbine (for Flows), Robolectric.

## Building and Running

### Common Gradle Commands
-   **Build**: `./gradlew build`
-   **Install/Run**: `./gradlew :app:installDebug`
-   **Unit Tests**: `./gradlew test`
-   **Instrumented Tests**: `./gradlew connectedAndroidTest`
-   **Linting**: `./gradlew detekt` or `./gradlew ktlintCheck`
-   **Formatting**: `./gradlew ktlintFormat`

### Environment Requirements
-   **JDK**: 17+
-   **Android SDK**: Compile SDK 35, Min SDK 26.
-   **Android Studio**: Ladybug or newer recommended.

## Current Project State
-   **Phase 1 (Foundation)**: COMPLETE. 15-module scaffold, CI/CD, DI, and Navigation are wired.
-   **Phase 2 (MVP)**: IN PROGRESS. Implementing voice loop, Home Assistant integration, and core persistence.

*Last updated: Thursday, March 19, 2026*
