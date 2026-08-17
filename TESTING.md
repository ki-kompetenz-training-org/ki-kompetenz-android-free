# KI-Kompetenz Android - Testing Guide

Automatisierte Test-Suite für MVP Testing auf Pixel-Geräten.

## 🚀 Quick Start

### 1. Pixel-Gerät vorbereiten

```bash
# USB Debugging aktivieren
# Einstellungen → Über das Telefon → 7x auf "Build-Nummer" tippen
# Zurück → Entwickleroptionen → USB Debugging aktivieren

# Gerät verbinden und überprüfen
adb devices
# Sollte anzeigen: <device-id> device
```

### 2. Tests ausführen

**Vollständige Test-Suite:**
```bash
./scripts/run-tests.sh
```

**Einzelne Test-Kategorien:**
```bash
# Unit Tests (JVM)
./gradlew testReleaseUnitTest

# Android Integration Tests
./gradlew connectedAndroidTest

# Performance Tests
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ai.ki_kompetenz_training_org.PerformanceTest

# Security Tests
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ai.ki_kompetenz_training_org.SecurityTest
```

## 📋 Test-Coverage

### Unit Tests (JVM)
- ✅ Core Business Logic
- ✅ Repository Layer
- ✅ ViewModel Layer
- ✅ API Contract Tests
- ✅ Property-Based Tests
- ✅ Security Tests

### Android Integration Tests
- ✅ **Home Screen** - Navigation, Features
- ✅ **ForKids** - COPPA Compliance
- ✅ **Quiz** - 10 Questions, Scoring
- ✅ **SRS** - Spaced Repetition
- ✅ **Account Deletion** - GDPR Art. 17
- ✅ **Performance** - Startup < 3s, No ANRs
- ✅ **Security** - No Tracking, HTTPS Only

### Test Files

| Datei | Zweck | Tests |
|-------|-------|-------|
| `HomeScreenTest.kt` | Navigation & Features | 5 |
| `ForKidsCoppaTest.kt` | COPPA Compliance | 6 |
| `QuizIntegrationTest.kt` | Quiz Functionality | 5 |
| `SrsIntegrationTest.kt` | SRS System | 4 |
| `AccountDeletionTest.kt` | GDPR Compliance | 4 |
| `PerformanceTest.kt` | Performance | 3 |
| `SecurityTest.kt` | Security & Privacy | 6 |

**Gesamt: 33+ Automatisierte Tests**

## 📊 Coverage Report

Nach Test-Ausführung:

```bash
# HTML Report öffnen
open app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.html

# XML Report (CI/CD)
cat app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

**Target Coverage:** > 95% (6 Sigma Standard)

## 🐛 Debugging

### Logs anzeigen
```bash
# Live Logs
adb logcat

# Filter nach App
adb logcat | grep "KiKompetenz"

# Crash Logs
adb logcat -d | grep -A20 "AndroidRuntime"
```

### Test spezifisch debuggen
```bash
# Einzelnen Test ausführen
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=ai.ki_kompetenz_training_org.ui.forkids.ForKidsCoppaTest

# Einzelne Test-Methode
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=ai.ki_kompetenz_training_org.ui.forkids.ForKidsCoppaTest \
  -Pandroid.testInstrumentationRunnerArguments.method=forKids_noLoginRequired
```

## 📱 Pixel-Spezifische Tests

### Device Info
```bash
# Modell
adb shell getprop ro.product.model

# Android Version
adb shell getprop ro.build.version.release

# API Level
adb shell getprop ro.build.version.sdk
```

### Performance Profiling
```bash
# Memory Usage
adb shell dumpsys meminfo de.kikompetenz.app

# CPU Usage
adb shell dumpsys cpuinfo

# Network Traffic
adb shell netstat -an | grep <port>
```

## ✅ MVP Testing Checkliste

### Vor dem Testing
- [ ] Pixel-Gerät verbunden (`adb devices`)
- [ ] USB Debugging aktiviert
- [ ] App installiert (`./gradlew installRelease`)
- [ ] Alle Dependencies geladen

### Automatisierte Tests
- [ ] `./scripts/run-tests.sh` erfolgreich
- [ ] Unit Tests: 116/116 passing
- [ ] Integration Tests: 33/33 passing
- [ ] Coverage: > 95%
- [ ] Performance: Startup < 3s
- [ ] Security: No tracking, HTTPS only

### Manuelle Tests (ergänzend)
- [ ] Login/Logout funktioniert
- [ ] Alle 12 Lektionen laden
- [ ] Quiz (10 Fragen) vollständig
- [ ] SRS-Karten wiederholbar
- [ ] Team-Funktion aktiv
- [ ] ForKids Parental Gate (PIN "1234")
- [ ] Account deletion (GDPR Art. 17)
- [ ] Offline-Mode funktioniert

## 🔧 Troubleshooting

### "No device found"
```bash
# USB Kabel wechseln
# USB Debugging neu aktivieren
# adb server neu starten
adb kill-server
adb start-server
adb devices
```

### "Tests failed"
```bash
# Cache löschen
./gradlew clean

# Neu installieren
./gradlew installRelease

# Tests neu ausführen
./scripts/run-tests.sh
```

### "Coverage < 95%"
```bash
# Test Coverage prüfen
./gradlew jacocoTestReport

# Fehlende Tests identifizieren
cat app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml | grep -A5 "missed"
```

## 📈 CI/CD Integration

Tests laufen automatisch bei jedem Push:

```yaml
# .github/workflows/6sigma-ci.yml
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - ./gradlew test
      - ./gradlew connectedAndroidTest
      - ./gradlew jacocoTestReport
```

## 🎯 Next Steps

Nach erfolgreichem Testing:
1. **Bug-Fixing** (falls nötig)
2. **Coverage erhöhen** (auf > 95%)
3. **Performance optimieren** (Startup < 2s)
4. **Production Release** (Google Play)

---

**Viel Erfolg beim MVP Testing!** 🚀
