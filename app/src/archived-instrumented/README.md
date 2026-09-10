# Archivierte Instrumented-Tests (bewusst deaktiviert)

Diese UI-Tests laufen **nicht** mehr in `connectedDebugAndroidTest` — sie sind
gegen eine **prä-Redesign-UI** geschrieben (englische Navigation „SRS“, „Quiz“,
„Team“, „Gamification“, „Premium“, ForKids/ForSeniors als Top-Level) und
schlagen heute systematisch fehl, ohne App-Bugs aufzudecken (die App ist durch
1029 Unit-Tests + Device-Verifikation + UserStoryE2ETest abgedeckt).

Entscheid vom 2026-09-09 (Option 2): Nur UserStoryE2ETest (UIAutomator,
echte User-Journeys) + DailyChallengeCardTest (app-zustandsunabhängiger
Compose-Test) bleiben aktiv.

## Archiviert
- PerformanceTest.kt          — Near-Platzhalter + stale Nav-Clicks („Quiz"/„SRS“)
- SecurityTest.kt             — 5× assert(true)-Platzhalter + 1 stale Nav-Click
- ui/auth/AccountDeletionTest.kt   — stale (Login-Flow der Vorgänger-UI)
- ui/forkids/ForKidsCoppaTest.kt   — stale (ForKids ist heute via Home-Karte)
- ui/HomeScreenTest.kt        — stale (alte Top-Level-Navigation)
- ui/quiz/QuizIntegrationTest.kt   — stale („Quiz“-Einstieg existiert nicht mehr)
- ui/srs/SrsIntegrationTest.kt     — stale („SRS“-Einstieg existiert nicht mehr)

## Wiederherstellen
Die Dateien liegen kompilier-frei unter `archived-instrumented/`. Um eine
Klasse zu reaktivieren: zurück nach `app/src/androidTest/...` verschieben und
auf die aktuelle deutsche UI anpassen (Labels: „Lektionen“, „KI-Score“,
„Mini-Spiele“, „Daily Check-in“, „Intervall-Wiederholung“, „Dein Profil“,
„Für Kinder“/„Für Senioren“ unten auf der Home — Home ist ein langer
ScrollView, erst scrollen).

## Parity
Beide Repos (MAIN + FREE) archivieren die identischen Dateien unter demselben
Pfad — `scripts/parity-check.sh` bleibt grün.
