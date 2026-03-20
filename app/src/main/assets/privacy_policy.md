# Aria Privacy Policy

**Last updated:** 2026-03-20

## Overview

Aria is a privacy-first AI voice companion for home automation. This policy explains exactly what data the app collects, stores, and transmits — and what it does not.

---

## What Data Aria Collects

### Voice Input
- Your voice is captured by Android's built-in SpeechRecognizer and converted to text on your device.
- Voice audio recordings are **not stored** anywhere — neither on your device nor on any server.

### Chat Messages
- Conversations between you and Aria are stored **locally on your device** in an encrypted Room database.
- Chat history never leaves your device unless you explicitly share it.

### Memories
- When Aria stores a memory (a fact you've told it), that memory is saved **locally on your device** only.
- You can view and delete memories at any time from within the app.

### Reminders
- Reminders you set are stored **locally on your device** and managed by Android's AlarmManager.

### Home Automation Commands
- Commands sent to Home Assistant (e.g., "turn on the lights") are forwarded to your Home Assistant server.
- Aria communicates directly with **your own Home Assistant instance** — no data passes through Aria's servers.
- An audit log of commands is stored locally on your device so you can review what was executed.

### Settings & Credentials
- Your Home Assistant server URL and access token are stored **locally on your device**, encrypted using AES-256/GCM with a key stored in Android Keystore.
- Your Google AI API key (if configured) is stored locally in Android DataStore.

---

## When Data Leaves Your Device

### Cloud AI (Google Gemini)
- When **Privacy Mode is OFF**, open-ended questions that Aria cannot answer locally are sent to the Google Gemini API to generate a response.
- The text of your query is sent to Google's servers and is subject to [Google's Privacy Policy](https://policies.google.com/privacy).
- When **Privacy Mode is ON**, no data is sent to any cloud AI service. All requests are handled locally or declined.

### Home Assistant
- Commands and state queries are sent to your configured Home Assistant server (typically on your local network).
- If your Home Assistant is accessible over the internet, requests will travel over your internet connection to your server.

---

## What Aria Does NOT Collect

- Aria does **not** collect or transmit any data to the Aria developer.
- Aria has **no** analytics, crash reporting, or advertising SDKs.
- Aria does **not** share your data with any third parties, except Google (when Privacy Mode is off, as described above) and your own Home Assistant server.
- Aria does **not** use advertising identifiers or track you across apps.

---

## Data Deletion

You can delete all locally stored data at any time:

1. Open the app
2. Go to **Settings**
3. Tap **Clear All Data**
4. Confirm the deletion

This will permanently erase all messages, memories, reminders, audit logs, and saved credentials from your device.

---

## Children's Privacy

Aria is not directed at children under 13. We do not knowingly collect personal information from children.

---

## Changes to This Policy

If this policy changes, the updated version will be included in the next app update. Continued use of the app after an update constitutes acceptance of the revised policy.

---

## Contact

If you have questions about this privacy policy, please open an issue at the project repository.
