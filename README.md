# Stanza

[![License: GPL-3.0-or-later](https://img.shields.io/badge/license-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Android 12+](https://img.shields.io/badge/Android-12%2B-brightgreen.svg)](#install)

**A battery-respecting, frosted glass Pomodoro and interval timer for Android.**

In Italian, *stanza* means a room or stopping place—and in verse, a metered unit of rhythm. Stanza treats focused sessions as quiet rooms of time rather than an anxious ticking clock.

It is built with zero-compromise timer correctness, native notification chronometer rendering (zero battery-draining wakeups), and a layered glassmorphic design system.

---

## Highlights

- **Zero-Wakeup Timer Authority**: A ticking coroutine is never the source of truth. State is anchored to `SystemClock.elapsedRealtime()` and `System.currentTimeMillis()`. Reboots, app swipe-away, Doze, and process death are non-events.
- **Hardware Chronometer**: The ongoing notification uses Android's native `setUsesChronometer(true)` with countdown. The system UI renders the live second-by-second countdown on the lock screen and status bar without waking the CPU.
- **Unyielding Alarms**: `AlarmManager.setExactAndAllowWhileIdle` fires phase transitions on time, even under aggressive OEM task killers or deep battery optimization.
- **Glass UI System**: Real backdrop blur via [Haze](https://github.com/chrisbanes/haze), animated drifting mesh gradients, a 1 dp specular gradient border, and an OLED dither noise grain overlay.
- **Accessibility & Low-End Escape Hatch**: A "Reduce Transparency" toggle instantly replaces all glass blurs with solid, high-contrast dark tonal surfaces.
- **Pure Kotlin Domain**: `TimerEngine` has zero Android imports, operating as a deterministic pure reducer tested across all boundary conditions.
- **Insights & Heatmap**: GitHub-style annual focus rhythm heatmap, daily/weekly metrics, and streak calculations powered by indexed SQLite queries.
- **No bloat, no tracking, no ads**: No user accounts, no cloud sync, no analytics, no subscription prompts.

---

## Install

Requires **Android 12 (API 31)** or newer (for hardware-accelerated `RenderEffect` backdrop blur).

Available via GitHub Releases and F-Droid.

---

## Architecture

Single-module clean architecture with manual container DI:

```
app/src/main/kotlin/io/github/melastore/stanza/
├── StanzaApp.kt                // Application entry point
├── di/
│   └── AppContainer.kt         // Manual constructor DI (No Hilt/Koin)
├── domain/                     // Pure Kotlin: Clock, Phase, TimerEngine, TimerState
├── data/
│   ├── db/                     // StanzaDatabase SQLite helper with WAL & indexing
│   ├── datastore/              // DataStore preferences & atomic state persistence
│   └── repository/             // SessionRepository & TaskRepository
├── service/                    // TimerService (specialUse), AlarmScheduler, NotificationFactory
├── widget/                     // StanzaTileService (Quick Settings) & Glance widget
└── ui/                         // Compose: theme, glass/ (GlassSurface, MeshGradient), screens
```

---

## Building

```sh
git clone https://github.com/melastore/stanza.git
cd stanza
./gradlew test assembleDebug
```

Run code formatting and verification:

```sh
./gradlew spotlessCheck
```
