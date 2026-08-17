# KI-Kompetenz Android App

**EU AI Act Art. 4 compliant AI literacy training** — Android App für deutschsprachige Professionals.

## 🚀 Quick Start

### Voraussetzungen
- JDK 17+ (z.B. OpenJDK 17, Oracle JDK 17)
- Android SDK 35+
- Gradle 8.x
- Git

### 1. Repository klonen
```bash
git clone https://github.com/your-org/ki-kompetenz-android.git
cd ki-kompetenz-android
```

### 3. Release Pipeline (vollautomatisiert)
```bash
# EIN BEFEHL — alles automatisch:
# - Quality Gate (Tests, Checks)
# - Clean Build
# - Sign AAB
# - Signature Verify
bash scripts/release-pipeline.sh
```

### 4. AAB finden & verifizieren
```bash
# AAB
ls -lh app/build/outputs/bundle/release/app-release.aab

# Signatur prüfen
unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF/.*\.RSA
# Output: META-INF/KIKOMPET.RSA (signiert ✅)
```

## 🔐 Signing Configuration

### Lokaler Build
1. `.keystore.env` Datei erstellen (wird nicht commited):
```
STOREPASS=your_store_password
KEYPASS=your_key_password
KEYALIAS=kikompetenz
```

2. Umgebungsvariablen exportieren:
```bash
export KIKOMPETENZ_RELEASE_STORE_FILE="ki-kompetenz-release.jks"
export KIKOMPETENZ_RELEASE_STORE_PASSWORD="your_store_password"
export KIKOMPETENZ_RELEASE_KEY_ALIAS="kikompetenz"
export KIKOMPETENZ_RELEASE_KEY_PASSWORD="your_key_password"
```

### CI/CD (GitHub Actions)
In GitHub Secrets konfigurieren:
- `KIKOMPETENZ_RELEASE_JKS_BASE64` — Base64-encodierter Keystore
- `KIKOMPETENZ_RELEASE_STORE_PASSWORD`
- `KIKOMPETENZ_RELEASE_KEY_ALIAS`
- `KIKOMPETENZ_RELEASE_KEY_PASSWORD`

## 📱 Build Configuration

| Eigenschaft | Wert |
|------------|------|
| **Package Name** | `ai.ki_kompetenz_training_org` |
| **Min SDK** | 26 (Android 7.0) |
| **Target SDK** | **35** (Android 15) ✅ |
| **Compile SDK** | 35 |
| **Version Code** | 4 (automatisch inkrementierbar) |
| **Version Name** | 1.1.0 |

## 🏗️ Build & Release Automation

### Vollautomatisierte Pipeline (empfohlen)
```bash
bash scripts/release-pipeline.sh
```

**Was passiert:**
1. **Quality Gate** (7 Checks, <30s)
   - Compile
   - 164 Unit Tests
   - Hardcoded colors (WARN)
   - Hardcoded strings (WARN)
   - Duplicate imports
   - Content descriptions
   - Debug APK

2. **Release Build** (<60s)
   - Clean build
   - R8/ProGuard (88% size reduction)
   - Sign AAB with keystore
   - Verify signature

3. **Output**
   - `app/build/outputs/bundle/release/app-release.aab` (4.5MB, signed ✅)
   - `META-INF/KIKOMPET.RSA` (signature verified)

### Quality Gate Details
```bash
bash scripts/quality-gate.sh
```

7 automatische Checks:
- Compile & Tests
- Code quality (colors, strings, imports)
- Build artifacts

### Manuelles Build (optional)
```bash
# Nur AAB bauen
./gradlew :app:bundleRelease

# Mit Clean
./gradlew clean :app:bundleRelease
```

## 📦 Google Play Store

### ⚠️ WICHTIG: Manuelle Schritte erforderlich

**Die Automatisierung (CI/CD) erstellt nur das AAB.**
**Play Console erfordert manuelle Einmal-Einrichtung:**

1. **$25 Developer Account** bezahlen
2. **App erstellen** im Play Console Dashboard
3. **Store Listing** ausfüllen (Screenshots, Icons, Text)
4. **Content Rating** (IARC questionnaire)
5. **App Content Fragen**:
   - Finanzfunktionen: **Nein**
   - Gesundheitserklärung: **Ausfüllen** (AI-Aufklärung, keine Medizin)
   - Ads: **Nein** (DSGVO-konform)
6. **Länder/Regionen** auswählen (DE, AT, CH)
7. **AAB hochladen** (siehe unten)
8. **Review abwarten** (1-7 Tage)

**📋 Vollständige Checkliste:** [`PLAY_CONSOLE_CHECKLIST.md`](PLAY_CONSOLE_CHECKLIST.md)

### AAB hochladen

**Lokal erstellen:**
```bash
bash scripts/release-pipeline.sh
```

**AAB Pfad:**
```
app/build/outputs/bundle/release/app-release.aab
```

**In Play Console:**
1. Production → Create new release
2. AAB hochladen
3. Release notes eingeben
4. Review starten

### Versionscode

**Automatisch** (CI/CD):
```bash
# Push zu main/master → versionCode auto-increment
# 7 → 8 → 9 → ...
```

**Manuell:**
```bash
sed -i 's/versionCode = [0-9]*/versionCode = 8/' app/build.gradle.kts
bash scripts/release-pipeline.sh
```

**Google Play Requirements**:
- ✅ Target SDK ≥ 35 (erforderlich)
- ✅ VersionCode muss höher sein als vorheriger Release
- ✅ Signiert mit gültigem Keystore

## 🔒 Sicherheit

- **Keystore**: `ki-kompetenz-release.jks` (PKCS12, RSA 4096)
- **Passwörter**: `.keystore.env` (in `.gitignore`)
- **Certificate Pinning**: Let's Encrypt ISRG X1/X2
- **Network Security**: HTTPS only, no cleartext
- **No Tracking**: DSGVO-konform, keine Analytics

## 🧪 Tests

```bash
# Alle Tests ausführen
./gradlew test

# Coverage Report
./gradlew jacocoTestReport

# Lint Check
./gradlew lint
```

## 📱 Features

- ✅ EU AI Act Training (12 Lektionen)
- ✅ Gamification (XP, Badges, Leaderboards)
- ✅ SRS (Spaced Repetition System)
- ✅ Team & Multiplayer
- ✅ **ForKids** (COPPA-compliant, lokal)
- ✅ **ForSeniors** (große Schrift, einfache Sprache)
- ✅ Account deletion (GDPR Art. 17)
- ✅ Multi-language (DE/EN/FR/ZH)

## 🛠️ Troubleshooting

### "Keystore file not found"
```bash
# Stelle sicher, dass der Pfad korrekt ist
ls -la ki-kompetenz-release.jks

# Oder Pfad in .keystore.env anpassen
echo "STOREPASS=..." > .keystore.env
echo "KIKOMPETENZ_RELEASE_STORE_FILE=$(pwd)/ki-kompetenz-release.jks" >> .keystore.env
```

### "Target SDK zu niedrig"
```bash
# Target SDK auf 35 setzen (Google Play requirement)
sed -i 's/targetSdk = [0-9]*/targetSdk = 35/' app/build.gradle.kts
./gradlew clean bundleRelease
```

### "VersionCode already used"
```bash
# VersionCode erhöhen
sed -i 's/versionCode = [0-9]*/versionCode = 4/' app/build.gradle.kts
./gradlew clean bundleRelease
```

### "APK nicht signiert"
```bash
# Stelle sicher, dass Umgebungsvariablen gesetzt sind
source .keystore.env
export KIKOMPETENZ_RELEASE_STORE_FILE="ki-kompetenz-release.jks"
export KIKOMPETENZ_RELEASE_STORE_PASSWORD="$STOREPASS"
export KIKOMPETENZ_RELEASE_KEY_ALIAS="$KEYALIAS"
export KIKOMPETENZ_RELEASE_KEY_PASSWORD="$KEYPASS"

# Neu bauen
./gradlew clean bundleRelease
```

## 📄 License

Proprietary — KI-Kompetenz Training GmbH

## 📞 Support

- Email: ki-kompetenz-training@tobias-weiss.org
- Website: https://ki-kompetenz-training.org

## 📚 Research Integration

This project integrates findings from two research repositories:

1. **mobile-apps-best-practices** — Mobile development best practices
   - Security, architecture, testing, performance, deployment
   - Location: `~/git/mobile-apps-best-practices`

2. **ai-literacy-research** — AI-literacy research corpus (4,414 papers)
   - Evidence-based content validation
   - Research gaps: `org-implementation` & `evaluation`
   - Location: `~/git/ai-literacy-research`

**See [RESEARCH_INTEGRATION.md](RESEARCH_INTEGRATION.md) for details.**
