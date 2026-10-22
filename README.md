<div align="center">

# Jarly

### A shopping list and pantry jar for Android — offline, private, and yours

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-purple.svg)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.4-green.svg)](https://developer.android.com/build)
[![Material You](https://img.shields.io/badge/Material--3%20Expressive-1.5.0--beta01-teal.svg)](https://m3.material.io)
[![minSdk](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/about/versions/8)
[![targetSdk](https://img.shields.io/badge/targetSdk-37-brightgreen.svg)](https://developer.android.com/about/versions/17)

<br>

**Version:** v0.2.0 · `just drop it in the Jarly`

</div>

---

## What it does

Jarly keeps two lists in one warm, honey-colored jar:

- **Shopping List** — type an item, tick it off, send it to the pantry when you get home.
- **Pantry** — give food an expiry date and a home (fridge / freezer / cupboard). Jarly
  groups everything into "expires today", "≤ 3 days", "≤ 7 days" and later, and can remind
  you before things go bad.

Plus:

- **History** — everything you threw away, with a one-tap *Buy again*.
- **Waste report** — how much food left your pantry this month and what it cost.
- **Repeating items** — mark staples (milk, bread) as weekly / biweekly / monthly and they
  hop back onto the list automatically.
- **Smart add bar** — the input is wrapped in a glowing rainbow ring that slowly spins.
  It's the little thing that makes the app feel alive.
- **Home-screen widget**, exact-timer reminders, full JSON backup export/import,
  light / dark / follow-system themes, and 140+ currencies (Polish zloty to Swiss franc).

> [!IMPORTANT]
> **Jarly never needs the internet.** The app asks for zero network permission — your list
> and your data physically cannot leave the phone. The optional AI bricks (text reasoning,
> receipt reader, photo eye) are Stage-3+ features marked *(soon)* in Settings, and even
> those will install locally with your explicit OK.

---

## Screenshots

Coming soon — real screenshots will be added here.

---

## How it works

| Piece | What it is |
|---|---|
| `data/` | `JarlyStore` (single JSON file on `filesDir`, atomic tmp-rename saves), `Models`, alarm-based `Alerts` |
| `domain/` | Pure Kotlin: freshness logic, currency list (ISO-4217), units, price/date formatting — fully unit-testable |
| `ui/` | Compose M3 Expressive: `JarlyAppRoot` (tabs + rainbow input), list/pantry/history screens, settings, dialogs, widget |

- **Persistence:** one pretty-printed `jarly-data.json`. Human-readable, so a backup is a
  backup. Settings live in `SharedPreferences` (`jarly_settings`).
- **Reminders:** an `AlarmManager` alarm set to 09:00 on the nearest expiry morning; a
  `BOOT_COMPLETED` receiver re-arms it after reboot. If the exact-alarm permission is
  missing, Jarly falls back to a 15-minute window instead of crashing.
- **Currency:** tapping *"All currencies and countries"* opens a searchable dialog over a
  built-in ISO-4217 list — no network lookup, ever.

---

## Build it

```bash
git clone https://github.com/arbuz181pl/Jarly.git
cd Jarly
./gradlew :app:assembleDebug       # debug APK: app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest   # unit tests (freshness, prices, JSON round-trip)
```

Requirements: JDK 17+, Android SDK 37. In Android Studio: *File → Open* and press Run.

`keystore.properties` (release signing) is optional and never committed — the release
build silently falls back to unsigned when it is missing.

---

## Project status

v0.2.0 — Stages 1–2 of the plan are done and hand-tested on a physical device:
check/tick flow, pantry with expiry groups, history with buy-again, widget, reminders,
backups, themes, currency picker, internet toggle, rainbow input.

Next up (Stage 3+): "Try AI?" first-run offer, share/CSV export, language section,
and the local AI bricks *(soon)*.

---

<div align="center">

Made with honey and no internet · GitHub: [@arbuz181pl](https://github.com/arbuz181pl)

</div>
