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

---

## EPIC: Onboarding & First Experience

### US-51: Personalisierte Onboarding-Einstellungen
**Als** neuer Nutzer
**moechte ich** beim Onboarding meine Lernziele auswaehlen,
**damit** die App Inhalte passend zu meinen Interessen vorschlaegt.

**Akzeptanzkriterien:**
- Onboarding erweitert: Schritt 4 mit Lernziel-Auswahl (Beruf, Studium, Privat, Kinder)
- Auswahl wird in SharedPreferences gespeichert
- HomeScreen zeigt personalisierte Empfehlungen basierend auf Auswahl
- Nutzer kann Auswahl spaeter in Einstellungen aendern

### US-52: AHA-Moment in ersten 60 Sekunden
**Als** neuer Nutzer
**moechte ich** innerhalb der ersten 60 Sekunden ein Erfolgserlebnis haben,
**damit** ich motiviert bleibe, die App weiter zu nutzen.

**Akzeptanzkriterien:**
- Erste Interaktion (z.B. Mini-Game oder Quiz) direkt nach Onboarding
- Sofortiges XP-Feedback nach erster Aktion
- Visuelles Success-Feedback (Animation, Sound-Option)
- Tagesziel-Fortschritt sichtbar nach erster Aktion

### US-53: Interaktives Tutorial
**Als** neuer Nutzer
**moechte ich** ein kurzes interaktives Tutorial der Hauptfunktionen,
**damit** ich die App schnell verstehen kann.

**Akzeptanzkriterien:**
- Tutorial-Overlay beim ersten Oeffnen der HomeScreen
- Highlights fuer: Lektionen, Mini-Games, KI-Score, Intervall-Wiederholung
- Skip-Button jederzeit verfuegbar
- Tutorial wird nicht erneut angezeigt nach Abschluss

---

## EPIC: Offline-First & Performance

### US-54: Offline-Lektionen
**Als** Nutzer mit schlechter Internetverbindung
**moechte ich** Lektionen offline lesen,
**damit** ich auch ohne Internet lernen kann.

**Akzeptanzkriterien:**
- Alle 14 interaktiven Lektionen sind lokal gespeichert (kein API-Aufruf noetig)
- InteractiveLessonScreen funktioniert ohne Netzwerk
- ForKids- und ForSeniors-Lektionen funktionieren vollstaendig offline
- Offline-Indikator wird angezeigt wenn keine Verbindung

### US-55: Lernfortschritt synchronisieren
**Als** Nutzer mit Geraetewechsel
**moechte ich** meinen Lernfortschritt synchronisieren,
**damit** ich auf einem neuen Geraet dort weitermachen kann, wo ich aufgehört habe.

**Akzeptanzkriterien:**
- Fortschritt (XP, Level, Streak, abgeschlossene Lektionen) wird serverseitig gespeichert
- Beim Login auf neuem Geraet wird Fortschritt geladen
- Konfliktloesung: hoechster Stand gewinnt bei Konflikt
- Sync erfolgt automatisch im Hintergrund

### US-56: Caching fuer Quiz-Fragen
**Als** Nutzer
**moechte ich** dass Quiz-Fragen gecacht werden,
**damit** das Quiz schnell laedt auch bei langsamer Verbindung.

**Akzeptanzkriterien:**
- Quiz-Fragen werden in Room-Datenbank gecacht
- Cache-Gueltigkeit: 24 Stunden
- Bei Cache-Hit: sofortige Anzeige
- Bei Cache-Miss: API-Aufruf mit Lade-Indikator

---

## EPIC: Gamification Deepening

### US-57: Wochen-Challenge
**Als** motivierter Nutzer
**moechte ich** eine woechentliche Herausforderung haben,
**damit** ich regelmaessig ueber mehrere Tage hinweg lerne.

**Akzeptanzkriterien:**
- Woechentliche Challenge: 7 Tages-Challenges an 7 aufeinanderfolgenden Tagen
- Fortschrittsbalken zeigt 0/7 bis 7/7
- Abschluss-Bonus: 100 XP bei allen 7 Tagen
- Wochen-Challenge wird jeden Montag zurueckgesetzt
- Separate Streak-Anzeige fuer Wochen-Challenge

### US-58: Erfolge (Achievements)
**Als** Nutzer
**moechte ich** spezielle Erfolge freischalten,
**damit** ich额外 motiviert bin, ungewoehnliche Ziele zu erreichen.

**Akzeptanzkriterien:**
- 10+ Achievements definiert (z.B. "Erste Woche", "100 Quiz-Fragen", "Alle Lektionen", "30-Tage-Streak")
- Achievement-Notification bei Freischaltung
- Achievements-Screen mit Liste aller Erfolge (freigeschaltet/gesperrt)
- EP-Bonus bei Achievement-Freischaltung

### US-59: Lerngruppen
**Als** Nutzer
**moechte ich** mit Freunden eine Lerngruppe bilden,
**damit** wir gemeinsam motiviert bleiben.

**Akzeptanzkriterien:**
- Gruppe erstellen mit Code-Einladung
- Gruppen-Leaderboard (anonym)
- Gemeinsamer Wochen-Challenge-Fortschritt
- Gruppen-Chat nur fuer Mitglieder (ohne PII)
- DSGVO: Keine persoenlichen Daten, nur App-interne IDs

---

## EPIC: Accessibility & Inclusion

### US-60: Screen-Reader-Unterstuetzung
**Als** sehbeeintraechtigter Nutzer
**moechte ich** die App mit Screen-Reader nutzen koennen,
**damit** ich alle Funktionen barrierefrei erreichen kann.

**Akzeptanzkriterien:**
- Alle interaktiven Elemente haben contentDescription
- Mini-Game-Optionen werden fuer TalkBack vorgelesen
- Lektionsinhalt ist semantisch strukturiert (heading, paragraph)
- Fokus-Reihenfolge ist logisch (oben nach unten)
- Test mit TalkBack: alle Screens navigierbar

### US-61: Kontrast-Modi und Schriftgroesse
**Als** Nutzer mit Seheinschraenkung
**moechte ich** Kontrast und Schriftgroesse anpassen,
**damit** ich die Inhalte gut lesen kann.

**Akzeptanzkriterien:**
- Dark Mode / Light Mode / High-Contrast-Modus
- Schriftgroesse: Systemeinstellung wird respektiert (Small, Default, Large, Huge)
- Mindestens WCAG 2.1 AA Kontrast (4.5:1) in allen Modi
- Einstellung wird in SharedPreferences gespeichert

### US-62: Reduzierte Animationen
**Als** Nutzer mit Motion-Sensitivity
**moechte ich** Animationen deaktivieren koennen,
**damit** ich die App ohne Unwohlsein nutzen kann.

**Akzeptanzkriterien:**
- Einstellung "Animationen reduzieren" in Settings
- Wenn aktiviert: keine Partikel, keine Uebergangs-Animationen
- Systemeinstellung "Reduce Motion" wird respektiert
- Sofortige Anwendung ohne App-Neustart

---

## EPIC: Data Export & Portability

### US-63: DSGVO-Datenexport
**Als** Nutzer
**moechte ich** alle meine Daten als JSON exportieren,
**damit** ich der DSGVO-Auskunftspflicht nachkommen kann.

**Akzeptanzkriterien:**
- Einstellungen > "Meine Daten exportieren"
- Exportiert: XP, Level, Streak, abgeschlossene Lektionen, Quiz-Ergebnisse, Einstellungen
- Format: JSON-Datei mit Zeitstempel
- Keine PII ausser Nutzernamen (falls eingeloggt)
- Export funktioniert offline (alle Daten lokal)

### US-64: Fortschritts-Backup
**Als** Nutzer
**moechte ich** meinen Fortschritt als Datei sichern,
**damit** ich ihn bei Neuinstallation wiederherstellen kann.

**Akzeptanzkriterien:**
- Backup: Speichert SharedPreferences als verschluesselte Datei
- Restore: Importiert Backup-Datei und stellt Fortschritt wieder her
- Backup enthaelt: XP, Level, Streak, Daily-Challenge-Streak, abgeschlossene Lektionen
- Bestaetigungs-Dialog vor Restore (ueberschreibt aktuelle Daten)

### US-65: Konto-Loeschung (DSGVO Art. 17)
**Als** Nutzer
**moechte ich** mein Konto und alle Daten loeschen,
**damit** ich mein Recht auf Vergessenwerden ausueben kann.

**Akzeptanzkriterien:**
- Account-Loeschung verfuegbar in Einstellungen
- Bestaetigungs-Dialog mit Typ-Bestätigung ("LOESCHEN" eingeben)
- Loeschung entfernt: Konto, Fortschritt, Cache, Token lokal
- Bei eingeloggtem Nutzer: API-Aufruf an /api/auth/delete-account
- Erfolgs-Bestaetigung nach Loeschung
- App kehrt zum Onboarding zurueck

---

## EPIC: Content Quality & Validation

### US-66: Content-Review-Pipeline
**Als** Entwickler
**moechte ich** dass alle Lektionsinhalte automatisch validiert werden,
**damit** keine fehlerhaften oder unvollstaendigen Inhalte ausgeliefert werden.

**Akzeptanzkriterien:**
- Unit-Tests pruefen alle 14 Lektionen auf: nicht-leere Titel, Beschreibungen, Bloecke
- Unit-Tests pruefen alle Quiz-Optionen: genau 1 korrekte Antwort
- Unit-Tests pruefen alle Kids/Seniors-Lektionen: Quiz-Struktur, Optionen-Anzahl
- CI-Pipeline blockiert bei fehlerhaften Inhalten
- Tests laufen bei jedem PR

### US-67: Nutzer-Feedback-Kanal
**Als** Nutzer
**moechte ich** Feedback zu Lektionen geben koennen,
**damit** die Inhalte verbessert werden koennen.

**Akzeptanzkriterien:**
- Feedback-Button am Ende jeder Lektion (Daumen hoch/runter)
- Optionaler Freitext-Kommentar (max 500 Zeichen)
- Feedback wird lokal gespeichert und bei naechster Sync gesendet
- DSGVO: Keine PII, nur App-Version und Lektions-ID
- Aggregiertes Feedback in Admin-Dashboard sichtbar

### US-68: Content-Versioning
**Als** Content-Manager
**moechte ich** Lektionen versionieren,
**damit** ich Aenderungen nachverfolgen kann.

**Akzeptanzkriterien:**
- Jede Lektion hat eine Versionsnummer
- Bei Inhaltsaenderung wird Version inkrementiert
- Nutzer sieht "Aktualisiert am"-Datum bei geaenderten Lektionen
- Bei Major-Update: Lektion wird als "Neu" markiert

---

## EPIC: Multi-Platform Consistency

### US-69: Feature-Paritaet Android und Web
**Als** Nutzer der Android-App und Website
**moechte ich** die gleichen Funktionen auf beiden Plattformen finden,
**damit** ich nahtlos zwischen Geraet und Browser wechseln kann.

**Akzeptanzkriterien:**
- Mini-Games: 8 Spiele auf beiden Plattformen identisch
- Daily Challenge: Gleiche Auswahl, gleiche XP-Regeln
- Lektionen: 14 interaktive Lektionen auf beiden Plattformen
- Gamification: XP, Level, Streak, Badges konsistent
- SRS: Intervall-Wiederholung auf beiden Plattformen
- ForKids und ForSeniors auf beiden Plattformen

### US-70: Einheitliches Design-System
**Als** Nutzer
**moechte ich** ein konsistentes visuelles Erlebnis,
**damit** die App professionell und vertrauenswuerdig wirkt.

**Akzeptanzkriterien:**
- Farbpalette: Material 3 auf Android, Tailwind aequivalent auf Web
- Typography: Gleiche Hierarchie (Headline, Body, Caption)
- Spacing: Konsistente Abstaende (4dp/8dp/16dp/24dp)
- Eckradius: Konsistent (8dp/12dp/16dp)
- Dark Mode auf beiden Plattformen
- Icon-Set: Gleiche Ikonographie (Material Icons / Lucide)

## EPIC: Content Authoring & Editing

### US-71: Inline-Content-Editor
Als Content-Manager moechte ich Lektionen und Quiz-Fragen direkt im Admin-Bereich der Website bearbeiten koennen, ohne Code-Aenderungen zu erforderen, damit ich Inhalte schnell aktualisieren kann.
- Admin-Dashboard mit Markdown-Editor fuer Lektionen
- Quiz-Fragen Editor mit Vorschau
- Versionierung und Rollback

### US-72: Mehrsprachige Content-Verwaltung
Als Content-Manager moechte ich Inhalte in mehreren Sprachen (DE, EN, FR, ZH) verwalten koennen, damit Nutzer weltweit lernen koennen.
- Uebersetzungsschnittstelle fuer alle Inhalte
- Automatische Erkennung fehlender Uebersetzungen
- Sprachspezifische Vorschau

### US-73: Content-Validierung
Als Content-Manager moechte ich eine Validierung fuer Quiz-Fragen haben, die prueft, ob jede Frage genau eine korrekte Antwort hat, erklaerungen vorhanden sind und Optionen nicht leer sind, damit fehlerhafte Inhalte nicht publiziert werden.
- Automatische Validierung beim Speichern
- Warnung bei fehlenden Erklaerungen
- Blockierung bei mehreren korrekten Antworten

### US-74: Content-Import und -Export
Als Content-Manager moechte ich Inhalte als JSON/CSV importieren und exportieren koennen, damit ich Inhalte zwischen Systemen migrieren kann.
- Export aller Lektionen als JSON
- Import von Quiz-Fragen aus CSV
- Validierung beim Import

## EPIC: Analytics & Insights

### US-75: Lernfortschritts-Analytics
Als Nutzer moechte ich detaillierte Statistiken ueber meinen Lernfortschritt sehen (Fragen beantwortet, Genauigkeit, Zeit pro Frage, Schwaechebereiche), damit ich weiss, wo ich mich verbessern muss.
- Dashboard mit Fortschrittsdiagrammen
- Kategorien-basierte Genauigkeitsanalyse
- Trend-Analyse ueber 7/30/90 Tage

### US-76: Vergleich mit Durchschnitt
Als Nutzer moechte ich meinen Fortschritt mit dem Durchschnitt anderer Nutzer vergleichen koennen, damit ich meine Leistung realistisch einschaetzen kann.
- Anonymisierter Vergleichs-Durchschnitt
- Prozent-Ranking
- Schwaeche-Bereich im Vergleich

### US-77: Lernzeit-Tracking
Als Nutzer moechte ich sehen, wie viel Zeit ich insgesamt gelernt habe, damit ich meine Lerngewohnheiten optimieren kann.
- Automatische Zeiterfassung pro Session
- Wochen- und Monatsuebersicht
- Durchschnittliche Session-Laenge

## EPIC: Search & Discovery

### US-78: Volltext-Suche
Als Nutzer moechte ich alle Lektionen, Quiz-Fragen und Mini-Spiele durchsuchen koennen, damit ich gezielt Themen finden kann.
- Suchfeld mit Autovervollstaendigung
- Highlighting von Treffern
- Filter nach Inhaltstyp (Lektion, Quiz, Spiel)

### US-79: Empfohlene Lektionen
Als Nutzer moechte ich personalisierte Lektionsempfehlungen erhalten, basierend auf meinen Schwaechebereichen, damit ich effizient lerne.
- Algorithmus basierend auf falsch beantworteten Fragen
- Empfehlung auf dem Dashboard
- Empfehlung nach每Quiz-Session

### US-80: Kategorien-Filter
Als Nutzer moechte ich Lektionen und Spiele nach Kategorien filtern koennen (z.B. Grundlagen, Ethik, DSGVO, Prompt-Engineering), damit ich gezielt Themen vertiefen kann.
- Filter-Chips auf Lektions- und Spiele-Seite
- Mehrfachauswahl moeglich
- Kombination mit Suchfunktion

## EPIC: Social Learning

### US-81: Lerngruppen
Als Nutzer moechte ich Lerngruppen erstellen und beitreten koennen, damit ich mit anderen gemeinsam KI-Kompetenz aufbauen kann.
- Gruppe erstellen mit Name und Beschreibung
- Einladungs-Link
- Gemeinsames Leaderboard innerhalb der Gruppe

### US-82: Geteilte Fortschritte
Als Nutzer moechte ich meinen Lernfortschritt mit Freunden teilen koennen, damit wir uns gegenseitig motivieren.
- Share-Button mit generiertem Bild (Zertifikat-Style)
- Social-Media-Teilen (WhatsApp, Telegram, E-Mail)
- Optional: Oeffentliches Profil mit Fortschritt

### US-83: Monats-Herausforderung
Als Nutzer moechte ich monatliche Herausforderungen haben (z.B. "Beantworte 50 Fragen in 30 Tagen"), damit ich langfristig motiviert bleibe.
- Automatische Challenge-Generierung
- Fortschrittsanzeige
- Belohnung bei Abschluss (Badge + XP)

## EPIC: Certification & Assessment

### US-84: Zertifikat-Generierung
Als Nutzer moechte ich nach Abschluss eines Kurses ein PDF-Zertifikat mit meinem Namen, Score und Datum erhalten, das ich als Nachweis verwenden kann.
- PDF-Generierung mit Name, Score, Tier, Datum
- Verifizierungs-URL und QR-Code
- Download und Teilen-Funktion

### US-85: KI-Score-Assessment
Als Nutzer moechte ich einen umfassenden KI-Score-Test absolvieren koennen, der mein Wissen in allen Bereichen prueft und einen Score von 0-1000 liefert.
- 30+ Fragen ueber alle KI-Domaene
- Score-Berechnung mit Gewichtung
- Tier-Einstufung (KI-Novize bis KI-Visionaer)
- Teilen des Scores mit Freunden

### US-86: Pruefungs-Modus
Als Nutzer moechte ich einen zeitgesteuerten Pruefungs-Modus haben, in dem ich 20 Fragen in 15 Minuten beantworten muss, um meine KI-Kompetenz unter Zeitdruck zu testen.
- Timer mit Countdown
- Automatische Auswertung
- Ergebnisse mit Erklaerungen nach Abschluss

### US-87: Wiederholungs-Modus
Als Nutzer moechte ich falsch beantwortete Fragen gezielt wiederholen koennen, damit ich aus meinen Fehlern lerne.
- Sammlung falscher Fragen
- Intervall-Wiederholung fuer falsche Fragen
- Statistik ueber Verbesserung

## EPIC: API & Integration

### US-88: OEffentliche API
Als Entwickler moechte ich eine oeffentliche REST-API nutzen koennen, um KI-Kompetenz-Daten in andere Anwendungen zu integrieren.
- API-Dokumentation (OpenAPI/Swagger)
- API-Key-Authentifizierung
- Rate-Limiting (60 Req/Min)
- Endpoints: Lektionen, Quiz, Mini-Spiele, Score

### US-89: Webhook-Integration
Als Entwickler moechte ich Webhooks fuer Ereignisse (z.B. Nutzer-Registrierung, Zertifikat-Abschluss) konfigurieren koennen, damit ich externe Systeme synchronisieren kann.
- Webhook-Konfiguration im Dashboard
- HMAC-Signatur fuer Verifizierung
- Retry bei Fehlern

### US-90: SDK fuer Mobile
Als Entwickler moechte ich ein TypeScript-SDK fuer die Integration der KI-Kompetenz-API in mobile Anwendungen (React Native, Expo) haben.
- NPM-Paket mit TypeScript-Typen
- Automatische Token-Erneuerung
- Offline-Caching

