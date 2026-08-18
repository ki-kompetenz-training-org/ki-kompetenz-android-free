# KI Quiz Free Edition — F-Droid App Improvements

**Date:** 2026-08-17
**Repo:** `ki-kompetenz-android-free` (tobias-weiss-ai-xr)
**Current version:** v1.1.1 (versionCode 9)
**Scope:** UX overhaul, i18n fixes, 3D companion, accessibility, error states

## Problem Statement

The KI Quiz Free Edition has a functional but unpolished user experience: the home screen is an overwhelming list of 11+ feature cards, strings are hardcoded in Kotlin (breaking i18n), English translations contain incorrect content, and there's no emotional engagement or visual delight. The app needs to feel like a premium product worthy of F-Droid featured status.

## Design

### 1. Bottom Navigation Architecture

Replace single-start `HOME` with a persistent `NavigationBar` with 4 destinations:

| Tab | Icon | Screen | Description |
|-----|------|--------|-------------|
| Home | `HomeFilled` | Slimmed HomeScreen | KiBot 3D companion, quick-action grid, gamification summary |
| Games | `SportsEsports` | MiniGamesMenuScreen (existing) | 8 free + 8 premium mini-games |
| Learn | `MenuBook` | LessonsScreen (existing) | 14 lessons, free/premium split |
| Profile | `Person` | GamificationScreen (expanded) | XP, level, badges, streak, language toggle, premium status |

**Navigation host:** Update `KiKompetenzNavHost` to use bottom nav. Team/Ranking accessible from Profile tab. Premium upsell from Profile or inline chips.

### 2. Home Screen Redesign

From 11 feature cards to:

1. **KiBot 3D companion** — animated robot with XP progress ring, emotional states
2. **Gamification summary** — streak, XP, daily check-in button (below KiBot)
3. **Quick-action grid** (2×2): KI-Score, Mini-Games, Lessons, SRS
4. **"More" section:** Kids + Seniors (compact, horizontal)
5. **Premium upsell chip** (subtle, only for non-premium)
6. **DSGVO footer**

### 3. KiBot — AI Tamagotchi Companion

A 3D robot mascot that lives on the home screen and grows with the user.

#### Growth Stages (tied to user level)

| Level | Stage | Visual |
|-------|-------|--------|
| 1–2 | Neonate | Small, simple, glowing eyes, wobbles, one antenna |
| 3–5 | Learner | Bigger, gains arms, antenna splits, idle bobbing |
| 6–9 | Thinker | Brain dome, brighter eyes, thinking particles |
| 10–14 | Expert | Full body, jet boosters, aura, holographic displays |

#### Emotional States

| Trigger | State | Animation |
|---------|-------|-----------|
| Daily check-in | Happy | Bounces, sparkles, warm glow |
| Quiz/game correct | Celebrating | Spins, confetti burst |
| Quiz/game wrong | Confused | Head tilt, floating "?" particles |
| Streak milestone | Thrilled | Backflip, fireworks |
| Not checked in 2+ days | Sleepy | Half-closed eyes, slow breath, Zzz |
| Premium unlocked | Upgraded | Golden glow, new accessories |
| Tap | Reactive | Bounces, waves, surprised |

#### 3D & Effects

- **3D model:** Procedurally generated Compose primitives (spheres, cylinders, rounded boxes) — no external .glb file needed. Fallback: `sceneview` if a .glb becomes available later.
- **Particle system:** Custom Compose `Canvas` animations (sparkles, confetti, "?", "Zzz")
- **Progress ring:** Animated gradient ring around KiBot showing XP to next level
- **Tilt parallax:** Subtle position shift based on device accelerometer
- **Dark mode:** Adjusted lighting, brighter robot glow

#### State Machine

`KiBotState` derived from existing `GamificationRepository` data:
- `level`, `xpIntoLevel`, `xpNeeded` → growth stage
- `streak`, `lastCheckIn` → emotional baseline
- `checkedInToday` → check-in state
- Recent quiz/game results → celebration/confused reactions

No new database tables needed.

### 4. String Extraction & i18n

**~40 hardcoded strings** to extract to `strings.xml` / `values-en/strings.xml`:

| Source file | Strings |
|-------------|---------|
| `MiniGamesMenuScreen.kt` | Menu subtitle, premium header, difficulty labels (Anfänger/Fortgeschritten/Experte) |
| `QuizScreen.kt` | All inline text: "Nächste Frage", "Mein Ergebnis", "KI-Score", intro description, "Punkte", "Max Combo", "Leben übrig" |
| `KidsScreen.kt` | "ForKids", "Für Eltern — Datenschutz-Info", all quiz strings, "Geschafft!", "Verstanden" |
| `HomeScreen.kt` | "Premium", kids/seniors titles and descriptions, "Starten" |
| `GamificationScreen.kt` | "Tages-Serie", "Noch keine Serie" |

**English fixes:**

| Key | Current | Fixed |
|-----|---------|-------|
| `premium_cta` | "Subscribe to Premium now" | "Discover Premium in Google Play" |
| `premium_trust` | "Secure payments via Stripe · 30-day money-back guarantee" | "This open-source edition contains 8 free games. All 16 games + 14 lessons: Google Play version" |

### 5. Language Toggle

- Add `language_preference` to existing DataStore prefs (via `TokenStore` or a new `SettingsStore`)
- Options: System default, Deutsch, English
- Language toggle in Profile tab
- Replace `currentLang()` (Locale.getDefault) with app-controlled language that falls back to system default
- New strings: `settings_language`, `settings_language_system`, `settings_language_de`, `settings_language_en`

### 6. Error States & Offline

| Screen | Scenario | UI |
|--------|----------|-----|
| Quiz intro | API unreachable | KiBot confused state, error message, retry button, "Play mini-games offline" link |
| Lessons list | API unreachable | Error card with retry, KiBot confused |
| Mini-Games | Never needs API | Already works offline — no change |

**Implementation:** Simple `ConnectivityObserver` using `ConnectivityManager`, exposed via `KiKompetenzApp`.

### 7. Accessibility

| Issue | Fix |
|-------|-----|
| Emojis as decorative icons | Add `semantics { contentDescription }` to containers |
| KiBot 3D scene | `semantics { contentDescription = "Dein KI-Begleiter, Level $level" }` |
| Hardcoded "Zurück" | Replace with `stringResource(R.string.nav_back)` |
| Quiz options | `semantics { contentDescription = "Option A: $text" }` |
| KiBot tap target | 48dp minimum touch target |
| State changes | Announce KiBot state changes to screen readers |

### 8. Test Package Migration

Move 10 test files from `de.kikompetenz.app` → `ai.ki_kompetenz_training_org`:

- `CoreLogicTest.kt`
- `GamificationRulesTest.kt`
- `PropertyBasedTests.kt`
- `SecurityTest.kt`
- `SrsSessionTest.kt`
- `api/ApiContractTest.kt`
- `data/repo/ContentRepositoryTest.kt`
- `data/repo/PremiumRepositoryTest.kt`
- `lesson/LessonIntegrationTest.kt`
- `ui/lessons/LessonDetailViewModelTest.kt`

### 9. Dark Mode

Existing dark theme works. Additions:
- KiBot 3D scene adjusts lighting/environment for dark mode
- Particle colors adapt to `isSystemInDarkTheme()`
- Progress ring gradient shifts

## Out of Scope

- New mini-games or lesson content
- Backend API changes
- F-Droid repo metadata updates (separate task)
- KiBot sound effects (v1 — can add later)
- KiBot customization/wardrobe (v1 — can add later)

## Reusable Existing Code (No Rewrite)

The paid `ki-kompetenz-android` and free `ki-kompetenz-android-free` repos share identical code (only `applicationId` and version differ). Extensive search across all repos confirmed no prior KiBot/3D/bottom-nav work exists. However, strong foundations exist:

| Existing Asset | Reuse For |
|---------------|----------|
| `sceneview` dependency in `build.gradle.kts` | 3D rendering — already declared, just import & use |
| `GamificationRepository` (XP, level, streak, check-in, badges) | KiBot state machine data source — no new DB tables |
| `currentLang()` + bilingual `MiniGame` data (de/en) | Language toggle plugs into this existing infrastructure |
| `Theme.kt` light/dark colors (`BrandPrimary`, `BrandPrimaryDark`) | KiBot lighting/glow adapts to existing schemes |
| `KiKompetenzApp` service locator pattern | Add `ConnectivityObserver` and `SettingsStore` there |
| `MiniGameScreen` animation patterns (`animateColorAsState`, press states) | Same patterns for KiBot confetti, sparkles, bounce |
| Kids COPPA-notice dialog pattern | Same dialog pattern for KiBot "hello" dialog on first launch (explains companion concept) |
| `PremiumRepository` gating logic | KiBot premium stage/accessories gated same way |
| `MiniGames.FREE` / `MiniGames.PREMIUM` lists | Games tab content already structured for bottom nav |
| `LessonsScreen` / `LessonsViewModel` | Learn tab content already structured for bottom nav |
| `GamificationScreen` / `GamificationViewModel` | Profile tab content already structured for bottom nav |

**We build from scratch:** Bottom `NavigationBar` + restructured `NavHost`, KiBot 3D compositor, particle system, accelerometer parallax, `SettingsStore`, `ConnectivityObserver`, ~40 new string resources.

## Dependencies

No new external dependencies. All 3D/effects built with:
- `sceneview` (already in build.gradle.kts)
- Compose `Canvas` / `graphics` (already available)
- `Accelerometer` sensor API (Android standard)
- `ConnectivityManager` (Android standard)
- `DataStore` (already in build.gradle.kts)
