# User Stories — KI-Kompetenz Android App

## US-01: Onboarding (First Launch)
**Als** neuer Nutzer  
**möchte ich** beim ersten Start eine Einführung sehen,  
**damit** ich verstehe, was die App bietet und wie ich starte.

**Akzeptanzkriterien:**
- 3 Seiten mit HorizontalPager (Welcome, KiBot, Start)
- "Überspringen" und "Weiter"/"Start" Buttons
- Nach Abschluss: Onboarding wird nicht mehr angezeigt
- Page-Indikator (Punkte) zeigt aktuelle Seite

## US-02: Home-Navigation
**Als** Nutzer  
**möchte ich** vom Home-Screen alle Hauptfunktionen erreichen,  
**damit** ich schnell zu Lektionen, Quiz, Spielen komme.

**Akzeptanzkriterien:**
- Home zeigt: Lektionen, KI-Score, Mini-Spiele, Wiederholen, Profil, ForKids, ForSeniors
- ForKids und ForSeniors sind sichtbar
- Bottom Navigation: Home, Mini-Spiele, Lektionen, Profil

## US-03: Interaktive Lektionen
**Als** Lernender  
**möchte ich** interaktive Lektionen mit verschiedenen Block-Typen durchlaufen,  
**damit** ich den EU AI Act verstehe statt nur Text zu lesen.

**Akzeptanzkriterien:**
- Lektion 1 ("Was ist KI?") ist über Lektionen → Lesson 1 erreichbar
- Lektion zeigt Titel, Abschnitte und Content-Blocks
- "Lektion abschließen" Button ist sichtbar
- Zurück-Button funktioniert

## US-04: KI-Score Quiz
**Als** Nutzer  
**möchte ich** mein Wissen testen,  
**damit** ich meinen KI-Score kenne.

**Akzeptanzkriterien:**
- Quiz ist über Home → KI-Score erreichbar
- Quiz startet mit "Spiel starten"
- Fragen werden angezeigt
- Ergebnis wird nach Abschluss gezeigt

## US-05: Mini-Spiele
**Als** Nutzer  
**möchte ich** kurze Lernspiele spielen,  
**damit** ich Wissen spielerisch festige.

**Akzeptanzkriterien:**
- Mini-Spiele Menü zeigt alle 8 Spiele
- Jedes Spiel ist kostenlos (kein Premium-Lock)
- Spiel startet beim Klick
- Runden werden angezeigt

## US-06: ForKids (COPPA)
**Als** Kind  
**möchte ich** KI spielerisch lernen,  
**damit** ich ohne Angst vor Datenmissbrauch lerne.

**Akzeptanzkriterien:**
- ForKids erfordert kein Login
- ForKids zeigt COPPA-Hinweis
- ForKids-Lektionen sind erreichbar
- Keine Server-Kommunikation

## US-07: ForSeniors
**Als** Senior  
**möchte ich** KI im Alltag verstehen,  
**damit** ich Phishing und Deepfakes erkenne.

**Akzeptanzkriterien:**
- ForSeniors ist über Home erreichbar
- Lektionen zu Passwörter, Phishing, KI-Telefone sind sichtbar
- Kein Login erforderlich

## US-08: Gamification / Profil
**Als** Nutzer  
**möchte ich** meinen Lernfortschritt sehen,  
**damit** ich motiviert bleibe.

**Akzeptanzkriterien:**
- Profil zeigt Level, XP, Serie
- Badges werden angezeigt
- DSGVO-Hinweis: lokale Speicherung

## US-09: Spracheinstellungen
**Als** Nutzer  
**möchte ich** die Sprache ändern,  
**damit** ich in meiner bevorzugten Sprache lerne.

**Akzeptanzkriterien:**
- Settings zeigt Sprachoptionen (Deutsch, English, System)
- Sprachwechsel wirkt sich sofort aus

## US-10: Premium-Übersicht
**Als** Nutzer  
**möchte ich** Premium-Features sehen,  
**damit** ich entscheiden kann, ob ich upgraden will.

**Akzeptanzkriterien:**
- Premium-Screen zeigt Features und Preis
- "Premium abonnieren" Button sichtbar

---

# Epics & Further User Stories

## EPIC: Engagement & Retention

### US-11: Tages-Herausforderung (Daily Challenge)
**Als** taeglicher Nutzer  
**moechte ich** eine taegliche KI-Mini-Spiel-Herausforderung sehen,  
**damit** ich jeden Tag motiviert werde, die App zu oeffnen.

**Akzeptanzkriterien:**
- DailyChallengeCard wird auf dem Home-Screen oben angezeigt (vor dem Quick-Action-Grid)
- Zeigt heutiges Spiel (Emoji, Titel, Beschreibung)
- Zeigt XP-Preview (+X XP)
- Zeigt Streak (Flame-Icon)
- "Start" Button fuehrt zum heutigen Spiel
- Nach Abschluss: Checkmark statt Button, "Come back tomorrow"
- Deterministische Auswahl: gleiches Datum = gleiches Spiel (8-Tage-Zyklus)
- DSGVO: alle Daten lokal in SharedPreferences (kikompetenz_gamification)
- Lifecycle-aware: ladet State bei ON_RESUME neu

### US-12: Daily Challenge XP & Streak
**Als** Spieler  
**moechte ich** Bonus-XP fuer die Daily Challenge erhalten,  
**damit** sich das taegliche Spielen lohnt.

**Akzeptanzkriterien:**
- Basis: 20 XP, Perfekt-Bonus: +15 XP, Streak-Bonus: +5/Tag (max 30)
- Maximale Tagesbelohnung: 65 XP
- Streak ist separat vom Check-in-Streak
- XP wird an GamificationRepository.addXp() uebergeben
- MiniGameScreen zeigt "+X XP Daily Challenge" Badge im Ergebnis

### US-13: Daily Challenge (Web)
**Als** Web-Nutzer  
**moechte ich** die Daily Challenge auch auf der Website sehen,  
**damit** ich plattformuebergreifend dieselbe UX habe.

**Akzeptanzkriterien:**
- DailyChallengeCard auf /mini-games Seite (vor den kostenlosen Spielen)
- Hydration-safe Rendering (useState + useEffect mounted Pattern)
- MiniGamePlayer erkennt heutiges Spiel und vergibt Bonus-XP
- localStorage-Basis (kkt-dc-date, kkt-dc-streak)
- 17 Unit Tests in lib/daily-challenge.test.ts

---

## EPIC: Intervall-Wiederholung (SRS)

### US-14: SRS Uebungswarteschlange
**Als** Lernender  
**moechte ich** KI-Konzepte im Abstand wiederholen,  
**damit** ich das Gelernte langfristig behalte.

**Akzeptanzkriterien:**
- SRS-Screen ist ueber Home erreichbar
- Zeigt faellige Karten an (Titel, Abstand seit letzter Uebung)
- Login erforderlich (Server-basierte SRS)
- "Alle erledigt" Zustand wenn keine Karten faellig
- XP wird bei erfolgreicher Uebung vergeben
- Verknuepfung mit GamificationRepository

### US-15: SRS Erinnerungsbenachrichtigungen
**Als** Lernender  
**moechte ich** Benachrichtigungen wenn Karten zur Uebung anstehen,  
**damit** ich meinen Lernrhythmus nicht vergesse.

**Akzeptanzkriterien:**
- NotificationChannel: "srs_reminders" (IMPORTANCE_DEFAULT)
- NotificationChannel: "general" (IMPORTANCE_LOW)
- Benachrichtigung zeigt Anzahl faelliger Karten
- Beruecksichtigt Android 13+ Berechtigungsanfrage (POST_NOTIFICATIONS)
- PermissionBanner auf Home-Screen wenn Berechtigung fehlt
- WorkManager-basiert (SrsReminderWorker)
- 9 Benachrichtigungs-Strings in 3 Locales (de, en, default)

---

## EPIC: Konto & Datenschutz (Account & Privacy)

### US-16: Login & Registrierung
**Als** Nutzer  
**moechte ich** mich registrieren und einloggen,  
**damit** ich meinen Fortschritt serverseitig speichern kann.

**Akzeptanzkriterien:**
- AuthScreen mit WebView (ki-kompetenz-training.org/auth)
- Login ueber die Website API (cookie-basiert)
- Token wird lokal gespeichert (TokenStore)
- Nach Login: SRS, Team und Premium-Features verfuegbar
- 401-Interceptor leitet bei abgelaufenem Token zum Login

### US-17: Kontoloeschung (DSGVO Art. 17)
**Als** Nutzer  
**moechte ich** mein Konto selbst loeschen,  
**damit** ich meine DSGVO-Rechte ausueben kann.

**Akzeptanzkriterien:**
- Account-Loeschung ueber die Website (/api/auth/delete-account)
- API-Route implementiert in Next.js (ki-kompetenz-training)
- Loeschung entfernt Nutzerdaten serverseitig
- Bestaetigung erforderlich
- DSGVO-Art. 17 konform

### US-18: About / Disclaimer
**Als** Nutzer  
**moechte ich** einen Disclaimer sehen,  
**damit** ich weiss, dass dies keine offizielle Regierungs-App ist.

**Akzeptanzkriterien:**
- AboutScreen mit Disclaimer-Text
- Link zu offizieller Quelle (eur-lex.europa.eu)
- "Keine Gewaehrleistung" Hinweis
- "Quelle oeffnen" Button oeffnet Browser mit EUR-Lex
- Ueber Home -> "Ueber die App" erreichbar

---

## EPIC: App-Stabilitaet & Qualitaet

### US-19: Offline-Modus
**Als** Nutzer  
**moechte ich** die App auch ohne Internet nutzen,  
**damit** ich in Zuegen und Flugzeugen lernen kann.

**Akzeptanzkriterien:**
- Offline-Banner auf Home-Screen bei fehlender Verbindung
- Lektionen und Mini-Spiele sind offline verfuegbar
- Gamification und Daily Challenge funktionieren offline (SharedPreferences)
- ForKids und ForSeniors funktionieren komplett offline
- SRS und Team erfordern Online-Verbindung (Login noetig)

### US-20: Absturz-Sicherheit (CrashHandler)
**Als** Nutzer  
**moechte ich** dass die App bei Fehlern nicht abstuerzt,  
**damit** ich mein Lernen fortsetzen kann.

**Akzeptanzkriterien:**
- CrashHandler installiert (Thread.setDefaultUncaughtExceptionHandler)
- Bei Crash: Fehler wird geloggt, App zeigt Fehlermeldung
- Keine Daten gehen verloren (SharedPreferences persistent)
- Fehler-Log wird lokal gespeichert (kein Server-Upload, DSGVO)

### US-21: In-App Review
**Als** Nutzer  
**moechte ich** nach Nutzung zum Bewerten aufgefordert werden,  
**damit** andere Nutzer die App finden.

**Akzeptanzkriterien:**
- ReviewHelper trigger nach 3 abgeschlossenen Lektionen
- Google Play In-App Review API (com.google.android.play:review)
- "Bereits gezeigt" Flag in SharedPreferences (kikompetenz_review)
- Nur einmal pro Installation
- Keine eigenen UI-Elemente (System-Dialog)
- DSGVO: keine Analytics, nur lokales Flag

---

## EPIC: Barrierefreiheit & Design

### US-22: Dark/Light Theme
**Als** Nutzer  
**moechte ich** das System-Theme verwenden,  
**damit** die App sich meinem Geraet anpasst.

**Akzeptanzkriterien:**
- Automatische Theme-Auswahl (isSystemInDarkTheme)
- DarkColors und LightColors definiert (MaterialTheme.colorScheme)
- Alle Screens verwenden MaterialTheme.colorScheme (kein Hardcoding)
- Container-Farben: *Container/*ContainerHigh statt hartcodierter Farben

### US-23: WCAG 2.1 AA Barrierefreiheit
**Als** Nutzer mit Behinderung  
**moechte ich** die App barrierefrei nutzen,  
**damit** ich gleichberechtigt lernen kann.

**Akzeptanzkriterien:**
- Mindestens 48dp Touch-Targets (alle interaktiven Elemente)
- Alle Icons haben contentDescription (16 Icons korrigiert)
- Kontrastverhaeltnis >= 4.5:1 (pruefbar mit check-contrast.py)
- EN 301 549 / BITV 2.0 konform
- Keine Informationen nur ueber Farbe vermittelt

---

## EPIC: Team & Soziales

### US-24: Team-Grundung & Leaderboard
**Als** Lernender  
**moechte ich** ein Team gruenden oder beitreten,  
**damit** ich mit Kollegen gemeinsam lerne und mich vergleiche.

**Akzeptanzkriterien:**
- Team-Screen ueber Home erreichbar
- Team gruenden (Team-Name eingeben)
- Team beitreten (Code oder Link)
- Leaderboard zeigt Team-Mitglieder mit Scores
- DSGVO: nur Namen und Scores, keine E-Mails
- Login erforderlich fuer Team-Features

### US-25: Premium-Features
**Als** Power-User  
**moechte ich** Premium-Features freischalten,  
**damit** ich Zugriff auf erweiterte Lektionen und Spiele habe.

**Akzeptanzkriterien:**
- Premium-Screen zeigt Features und Preis
- "Premium abonnieren" Button sichtbar
- 8 Premium-Mini-Spiele (locked hinter Premium-Wall)
- Premium-Lektionen (Lektion 9-14)
- In-App Review API (Google Play)

---

## EPIC: Web-Plattform

### US-26: Web Daily Challenge
**Als** Web-Nutzer  
**moechte ich** die Tages-Herausforderung im Browser spielen,  
**damit** ich auch ohne App am Daily Challenge teilnehmen kann.

**Akzeptanzkriterien:**
- DailyChallengeCard auf /mini-games Seite
- Zeigt heutiges Spiel, Streak, XP-Preview
- MiniGamePlayer vergibt Bonus-XP bei Abschluss
- localStorage-Persistenz (DSGVO-konform, kein Server)
- 17 Unit Tests (selector, XP, streak, dates)

### US-27: Web Mini-Spiele
**Als** Web-Nutzer  
**moechte ich** Mini-Spiele im Browser spielen,  
**damit** ich die App nicht installieren muss.

**Akzeptanzkriterien:**
- /mini-games Seite zeigt 8 kostenlose Spiele
- /mini-games/[id] Seite zeigt einzelnes Spiel mit MiniGamePlayer
- Score, Leben, Combo, Timer Mechanik
- Highscore in localStorage gespeichert
- 8 Premium-Spiele (locked, Upgrade-Link)

### US-28: Web KI-Score
**Als** Web-Nutzer  
**moechte ich** meinen KI-Score online testen,  
**damit** ich mein Wissen einschaetzen kann.

**Akzeptanzkriterien:**
- /ki-score Seite mit KI-Score Quiz
- Ergebnis-Seite mit Score und Auswertung
- Embed-faehige Version (/ki-score/embed)
- Lead-Capture fuer B2B

### US-29: Web SRS (Intervall-Wiederholung)
**Als** Web-Nutzer  
**moechte ich** Karten online wiederholen,  
**damit** ich mein Wissen langfristig behalte.

**Akzeptanzkriterien:**
- /api/srs/cards, /api/srs/due, /api/srs/review API-Routen
- Server-basierte SRS (Login erforderlich)
- XP-Integration mit Gamification
- Due-Cards werden mit Abstand angezeigt

### US-30: Web Konto-Loeschung (DSGVO Art. 17)
**Als** Web-Nutzer  
**moechte ich** mein Konto ueber die Website loeschen,  
**damit** ich meine DSGVO-Rechte ausueben kann.

**Akzeptanzkriterien:**
- /api/auth/delete-account API-Route implementiert
- Loeschung entfernt alle Nutzerdaten serverseitig
- Bestaetigung erforderlich
- DSGVO-Art. 17 konform

---

## EPIC: Inhalte & Ressourcen

### US-31: Zertifikat-Generierung
**Als** Lernender  
**moechte ich** ein Zertifikat nach Abschluss erhalten,  
**damit** ich meine KI-Kompetenz nachweisen kann.

**Akzeptanzkriterien:**
- Web: /ai-literacy/certificate Seite mit Zertifikat
- PDF-Generierung mit pdfkit
- Druck-Button (PrintButton Komponente)
- Name, Datum, Score auf Zertifikat
- Download als PDF

### US-32: Lernprofile (VARK)
**Als** Lernender  
**moechte ich** mein Lernprofil kennen,  
**damit** ich effektiver lernen kann.

**Akzeptanzkriterien:**
- /learner-profile Seite mit VARK-Modell
- Visual, Aural, Read/Write, Kinesthetic Profil
- Empfehlungen basierend auf Profil
- Lokale Speicherung (DSGVO)

### US-33: Blog & Ressourcen
**Als** Besucher  
**moechte ich** Blog-Artikel und Ressourcen lesen,  
**damit** ich mich weitergehend informiere.

**Akzeptanzkriterien:**
- /blog und /blog/[slug] Seiten
- /resources Seite mit Checklisten, KI-Inventur, Schulungsplan
- B2B-Seite (/b2b) mit Cross-Sell
- Open-Source-Statement und Impressum
- Sitemap und robots.txt

### US-34: Workshops
**Als** Unternehmen  
**moechte ich** Massgeschneiderte Workshops buchen,  
**damit** ich mein Team schulen kann.

**Akzeptanzkriterien:**
- /workshops Seite
- B2B Lead-Form (B2BLeadForm Komponente)
- Workshop-Themen und Preisinformation
- Kontaktmoeglichkeit

---

## EPIC: Sicherheit & Compliance

### US-35: Certificate Pinning
**Als** Nutzer  
**moechte ich** dass meine Daten verschluesselt uebertragen werden,  
**damit** niemand meine Daten abfangen kann.

**Akzeptanzkriterien:**
- Certificate Pinning mit Let-s Encrypt ISRG X1/X2
- Network Security Config (network_security_config.xml)
- Retrofit/OkHttp mit CertificatePinner konfiguriert
- Kein allowBackup (AndroidManifest: allowBackup=false)
- Backup Rules (backup_rules.xml)

### US-36: Portrait-Orientierung
**Als** Nutzer  
**moechte ich** dass die App im Hochformat bleibt,  
**damit** ich nicht versehentlich das Geraet drehe und die UI bricht.

**Akzeptanzkriterien:**
- android:screenOrientation="portrait" in AndroidManifest
- Alle Screens fuer Hochformat optimiert
- Keine Rotation-bedingten Layout-Bugs

### US-37: R8/ProGuard Optimierung
**Als** Nutzer  
**moechte ich** eine kleine App-Groesse,  
**damit** ich wenig Speicher brauche.

**Akzeptanzkriterien:**
- R8/ProGuard aktiviert (minifyEnabled true)
- APK-Groesse: 2.3 MB (88% Reduktion)
- ProGuard-Regeln fuer Kotlin Serialization, Retrofit, OkHttp, Room, Tink
- Keine Runtime-Abstuerze durch Obfuscation

### US-38: F-Droid Version
**Als** Open-Source-Nutzer  
**moechte ich** die App ueber F-Droid installieren,  
**damit** ich keine Google-Abhaengigkeit habe.

**Akzeptanzkriterien:**
- Separate applicationId: ai.ki_kompetenz_training_org.free
- Keine Google Play APIs (kein In-App Review)
- Unsigned release APK (F-Droid signiert selbst)
- Self-hosted F-Droid Repo: fdroid.contextual-intelligence.org
- Alle Features identisch (ausser In-App Review)
- 239 Unit-Tests, 0 Failures

---

## EPIC: Lernmaterial & KI-Kompetenz

### US-39: 14 Interaktive Lektionen
**Als** Lernender  
**moechte ich** interaktive Lektionen mit verschiedenen Block-Typen durchlaufen,  
**damit** ich KI-Kompetenz praxisnah erwerbe.

**Akzeptanzkriterien:**
- 14 interaktive Lektionen (Lektion 1-14)
- 8 Content-Block-Typen: Text, Info, Warning, Quiz, Classification, Matching, Scenario, RiskThermometer
- RiskThermometer: animiertes Thermometer mit 4 EU AI Act Risikostufen
- "Lektion abschliessen" Button nach letztem Block
- Lektions-Fortschritt wird lokal gespeichert
- Bilingual: Deutsch und Englisch

### US-40: 8 KI-Mini-Spiele
**Als** Spieler  
**moechte ich** kurze Lernspiele mit verschiedenen Schwierigkeitsgraden,  
**damit** ich Wissen spielerisch festige.

**Akzeptanzkriterien:**
- 8 kostenlose Spiele: human_or_ai, fact_or_hallucination, high_risk_blitz, agent_ampel, shadow_ai_check, prompt_profis, bias_spotter, dsgvo_check
- 8 Premium-Spiele (locked)
- MiniGameRound mit bilingualen Prompts, Optionen, Erklaerungen
- Difficulty: BEGINNER, INTERMEDIATE, EXPERT
- Difficulty-basierte XP: 15/20/25 Basis + 25 Perfekt-Bonus
- Score, Leben (3), Combo, Timer (20 Sekunden/Runde)
- Highscore in SharedPreferences

### US-41: ForKids (COPPA-konform)
**Als** Kind  
**moechte ich** KI spielerisch lernen,  
**damit** ich ohne Angst vor Datenmissbrauch lerne.

**Akzeptanzkriterien:**
- 6 ForKids-Lektionen (KidsLesson mit KidsSection, KidsQuiz)
- Kein Login erforderlich
- Keine Server-Kommunikation (COPPA)
- Keine Analytics, keine Werbung
- Lokale Speicherung aller Daten
- Kindgerechte Sprache und Inhalte

### US-42: ForSeniors
**Als** Senior  
**moechte ich** KI im Alltag verstehen,  
**damit** ich Phishing und Deepfakes erkenne.

**Akzeptanzkriterien:**
- 7 ForSeniors-Lektionen
- Themen: Passwoerter, Phishing, KI-Telefone, Deepfakes, Online-Banking, Social Media, Datenschutz
- Grossere Schrift und einfachere Sprache
- Kein Login erforderlich
- Lokale Speicherung

---

## EPIC: Internationalisierung

### US-43: Mehrsprachigkeit (i18n)
**Als** Nutzer  
**moechte ich** die App in meiner Sprache nutzen,  
**damit** ich KI-Konzepte in meiner Muttersprache lerne.

**Akzeptanzkriterien:**
- 3 Locales: Deutsch (default), Englisch (values-en), Deutsch (values-de)
- 256+ Strings pro Locale (inkl. Daily Challenge, Onboarding, Notifications)
- Sprachauswahl: Deutsch, Englisch, System
- Sofortiger Wechsel ohne App-Neustart
- Alle UI-Texte in strings.xml (kein Hardcoding)

### US-44: Onboarding (3-Screen)
**Als** neuer Nutzer  
**moechte ich** eine gefuehrte Einfuehrung,  
**damit** ich die App-Funktionen verstehe.

**Akzeptanzkriterien:**
- 3 Onboarding-Seiten (Welcome, KiBot, Start)
- HorizontalPager mit Page-Indikator
- "Ueberspringen" und "Weiter"/"Start" Buttons
- Nach Abschluss: Onboarding wird nicht mehr angezeigt (SharedPreferences)
- Onboarding-Strings in 3 Locales

---

## EPIC: Web-Plattform Erweiterung

### US-45: Web Dashboard & Gamification
**Als** Web-Nutzer  
**moechte ich** meinen Lernfortschritt im Dashboard sehen,  
**damit** ich motiviert bleibe und meinen Level verfolge.

**Akzeptanzkriterien:**
- /dashboard Seite mit Level, XP, Streak
- LevelProgressCard zeigt Fortschritt zum naechsten Level
- BadgeGallery mit errungenen Badges
- StreakHeatmap visuell (GitHub-Style)
- Gamification: checkDailyChallenge, completeDailyChallenge in lib/gamification.ts
- SRS-Integration mit Due-Cards

### US-46: Web Lernpfade (ForKids & ForSeniors)
**Als** Kind oder Senior  
**moechte ich** altersgerechte KI-Lerninhalte auf der Website,  
**damit** ich ohne App-Installation lernen kann.

**Akzeptanzkriterien:**
- /for-kids Seite mit kinderfreundlichen Inhalten (MDX)
- /for-seniors Seite mit seniorengerechten Inhalten (MDX)
- /for-kids/learn/[slug] und /for-seniors/learn/[slug] Lektionen
- COPPA-konform: keine PII, keine Server-Kommunikation fuer ForKids
- Lokale Datenspeicherung (localStorage)

### US-47: Web B2B & Lead-Generierung
**Als** Unternehmen  
**moechte ich** KI-Schulungen fuer mein Team buchen,  
**damit** ich meine Organisation kompetent mache.

**Akzeptanzkriterien:**
- /b2b Seite mit B2BLeadForm
- B2BCrossSell auf relevanten Seiten
- /workshops Seite
- KI-Inventur (/resources/ki-inventur)
- Schulungsplan (/resources/schulungsplan)
- Checklist Art. 4 (/resources/checkliste-art4)

### US-48: Web Blog & Content Marketing
**Als** Besucher  
**moechte ich** Blog-Artikel ueber KI-Themen lesen,  
**damit** ich mich kontinuierlich weiterbilde.

**Akzeptanzkriterien:**
- /blog und /blog/[slug] Seiten
- SEO-optimiert (Metadata, JSON-LD)
- /promo/agentic-leverage Landing-Page
- Open-Source-Statement
- Sitemap (app/sitemap.ts) und robots.txt (app/robots.ts)

---

## EPIC: KI-Score & Assessment

### US-49: KI-Score Quiz (Web)
**Als** Web-Nutzer  
**moechte ich** meinen KI-Score online testen,  
**damit** ich meine KI-Kompetenz einschaetzen kann.

**Akzeptanzkriterien:**
- /ki-score Seite mit interaktivem Quiz
- KiScoreGame Komponente mit Fragen und Auswertung
- Ergebnis-Seite (/ki-score/result/[id]) mit detaillierter Auswertung
- Embed-faehige Version (/ki-score/embed) fuer externe Einbindung
- Lead-Capture fuer B2B (ki-score/lead API)
- Score-Counter und ConfidenceRating Komponenten

### US-50: KI-Score API
**Als** Entwickler  
**moechte ich** die KI-Score API nutzen,  
**damit** ich KI-Scores programmatisch abrufen kann.

**Akzeptanzkriterien:**
- /api/ki-score (POST: erstellt Score)
- /api/ki-score/[id] (GET: ruft Score ab)
- /api/ki-score/count (GET: Gesamtanzahl)
- /api/ki-score/lead (POST: speichert Lead)
- /api/content/ki-score (GET: Fragen fuer KI-Score)
- DSGVO: Lead-Daten minimiert, nur bei Einwilligung
