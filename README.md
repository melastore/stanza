# Stanza

[![License: GPL-3.0-or-later](https://img.shields.io/badge/license-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Android 12+](https://img.shields.io/badge/Android-12%2B-brightgreen.svg)](#install)

A Pomodoro and interval timer for Android with a frosted glass look.

Each focus session is called a stanza. A cycle is a few stanzas with short breaks between
them and a long break at the end.

## Features

- Focus, short break and long break timers with adjustable lengths and stanzas per cycle
- Optional auto-start for breaks and the next focus session
- Countdown in the notification and on the lock screen
- Keeps time through reboots, Doze and the app being swiped away
- Tasks with estimated and completed stanzas
- Stats: daily and weekly totals, streaks, and a yearly heatmap
- Several timer layouts and colour themes, including a custom one
- Quick Settings tile and a home screen widget
- "Reduce Transparency" setting for slower phones or if you find the blur hard to read
- No accounts, no internet, no tracking, no ads

## How the timer works

The app doesn't count seconds itself. When a session starts it saves the end time, both as
`elapsedRealtime` and as wall-clock time, and sets an exact alarm with
`setExactAndAllowWhileIdle`. The notification uses Android's built-in chronometer, so the
system draws the countdown and the phone doesn't wake up every second.

After a reboot, the saved wall-clock end time is used to either finish the session or pick it
back up where it was.

## Install

Needs Android 12 (API 31) or newer, because the blur uses `RenderEffect`.

APKs are on the [releases page](https://github.com/melastore/stanza/releases).

## Code layout

```
app/src/main/kotlin/io/github/melastore/stanza/
├── di/          manual DI (AppContainer)
├── domain/      TimerEngine and timer state, no Android imports
├── data/        SQLite, DataStore, repositories
├── service/     foreground service, alarms, notifications
├── widget/      Quick Settings tile, Glance widget
└── ui/          Compose screens, theme, glass effects
```

## Building

```sh
git clone https://github.com/melastore/stanza.git
cd stanza
./gradlew test assembleDebug
./gradlew spotlessCheck
```

## License

GPL-3.0-or-later
