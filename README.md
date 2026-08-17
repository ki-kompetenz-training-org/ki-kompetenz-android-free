# KI Quiz — Free Edition (Open Source / F-Droid)

Die **Open-Source-Edition** von KI Quiz (KI-Kompetenz-Training) für F-Droid.
Diese Version enthält alle **8 kostenlosen Mini-Spiele** und ist vollständig
offen lizenziert.

**Premium-Inhalte** (8 weitere Mini-Spiele, 14 interaktive Lektionen,
Zertifikat, Team-Features) gibt es in der **Google-Play-App**
[KI Quiz](https://play.google.com/store/apps/details?id=ai.ki_kompetenz_training_org)
mit der App-ID `ai.ki_kompetenz_training_org`.

| | Free Edition (dieses Repo) | Google Play |
|---|---|---|
| App-ID | `ai.ki_kompetenz_training_org.free` | `ai.ki_kompetenz_training_org` |
| Mini-Spiele | 8 (frei) | 16 (8 frei + 8 Premium) |
| Lektionen | 1–8 (frei, Server) | 1–14 |
| Preis | 0 € | Freemium (Abonnement) |
| License | Apache-2.0 + CC BY 4.0 (Inhalte) | proprietäre Premium-Inhalte |
| Distribution | F-Droid / direkt installierbar | Google Play |

## Inhalt der Free Edition

- **8 Mini-Spiele** (je 10 Runden, Deutsch + Englisch):
  🤖 Human or AI · 🤥 Fact or Hallucination · ⚠️ High-Risk Blitz ·
  🚦 Agent-Ampel · 🕵️ Shadow-AI-Check · ⌨️ Prompt-Profis ·
  ⚖️ Bias-Spotter · 🔐 DSGVO-Check
- **KI-Score-Quiz** (50 Fragen), **Gamification** (XP, Level, Badges, Streaks)
  — offline gespeichert (Room, DSGVO-konform, kein Tracking)
- Freie Lektionen 1–8 werden vom öffentlichen Server
  `ki-kompetenz-training.org` geladen (Login optional).

## Build

```bash
./gradlew :app:assembleDebug        # Debug-APK
./gradlew :app:testDebugUnitTest    # Unit-Tests
```

Voraussetzungen: JDK 17, Android SDK (compileSdk 35).

Die App benötigt **keinen Keystore** zum Bauen (unsigniert) — F-Droid und
andere Distributionen signieren selbst.

## Server-API (Free-Umfänge)

Die Free-Edition kommuniziert nur mit öffentlich zugänglichen, kostenlosen
Endpunkten des Servers `https://ki-kompetenz-training.org`:

| Endpoint | Zweck |
|---|---|
| `GET /api/content/ki-score` | 50 KI-Score-Quizfragen (de) |
| `GET /api/content/lessons` | Lektions-Übersicht |
| `GET /api/content/lessons/[slug]` | Lektions-Inhalte (1–8 frei) |
| `POST /api/auth/login` · `POST /api/auth/register` | optionaler Account |
| `GET /api/store/subscription-status` | Premium-Status der Play-Version |

Keine Tracking-SDKs, keine Google Play Services, keine Werbung.
Premium-Gate für Lektionen 9–14 erfolgt serverseitig.

## F-Droid

Die App ist als F-Droid-Repo verfügbar (siehe README des Repos) und kann
alternativ als direkte APK installiert werden.

## Lizenz

- Software: **Apache-2.0** (siehe [LICENSE](LICENSE))
- Inhalte (Fragen, Texte): **CC BY 4.0** (siehe [LICENSE-CONTENT](LICENSE-CONTENT))

© 2026 KI Kompetenz Training — ki-kompetenz-training.org