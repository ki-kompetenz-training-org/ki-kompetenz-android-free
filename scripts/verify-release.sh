#!/usr/bin/env bash
# verify-release.sh — reproduzierbare Release-Verifikation (v1.3.1-Prozess als Skript)
# Phase A (lokal): version/badging/signatur/proguard/aab; Phase B (--device): install+launch+UI
set -uo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO"
APK="app/build/outputs/apk/release/app-release.apk"
AAB="app/build/outputs/bundle/release/app-release.aab"
MAPPING="app/build/outputs/mapping/release/mapping.txt"
EXPECTED_FP="3e6ed9b3e3d8a71250edbf6a483697952a0f5c4e05602c41d4c4e9dffcdc2a98"
FORBIDDEN_PKG="de.kikompetenz.app"
AAB_REQUIRED=0
DEVICE=0
while [ $# -gt 0 ]; do
  case "$1" in
    --apk) APK="$2"; shift 2 ;;
    --aab) AAB="$2"; shift 2 ;;
    --aab-required) AAB_REQUIRED=1; shift ;;
    --device) DEVICE=1; shift ;;
    --fingerprint) EXPECTED_FP="$2"; shift 2 ;;
    -h|--help) grep '^#' "$0" | head -22; exit 0 ;;
    *) echo "UNBEKANNTES ARGUMENT: $1"; exit 2 ;;
  esac
done
PASS=0; FAIL=0
ok()   { echo "  PASS  $1"; PASS=$((PASS+1)); }
fail() { echo "  FAIL  $1"; FAIL=$((FAIL+1)); }

# ── Toolchain ────────────────────────────────────────────────────────────────
AH="${ANDROID_HOME:-$HOME/AppData/Local/Android/Sdk}"
AHU="$(cygpath -u "$AH" 2>/dev/null || echo "$AH")"
BT=""
for d in "$AHU/build-tools"/*; do
  { [ -f "$d/aapt2.exe" ] || [ -f "$d/aapt2" ]; } || continue
  { [ -f "$d/apksigner.bat" ] || [ -f "$d/apksigner" ]; } && BT="$d"
done
[ -n "$BT" ] || { echo "FAIL: keine build-tools mit aapt2/apksigner unter $AHU/build-tools"; exit 2; }
AAPT2="$BT/aapt2.exe";         [ -f "$AAPT2" ]     || AAPT2="$BT/aapt2"
APKSIGNER="$BT/apksigner.bat"; [ -f "$APKSIGNER" ] || APKSIGNER="$BT/apksigner"
# keytool: PATH hat Vorrang, sonst JAVA_HOME/bin (Leerzeichen-sicher via cygpath)
KEYTOOL=""
if command -v keytool >/dev/null 2>&1; then
  KEYTOOL="$(command -v keytool)"
elif [ -n "${JAVA_HOME:-}" ]; then
  JHU="$(cygpath -u "$JAVA_HOME" 2>/dev/null || echo "$JAVA_HOME")"
  [ -f "$JHU/bin/keytool.exe" ] && KEYTOOL="$JHU/bin/keytool.exe"
  [ -f "$JHU/bin/keytool" ]     && KEYTOOL="$JHU/bin/keytool"
fi
[ -n "$KEYTOOL" ] || { echo "FAIL: keytool weder auf PATH noch in JAVA_HOME/bin"; exit 2; }
command -v python >/dev/null  || { echo "FAIL: python nicht auf PATH";  exit 2; }
echo "== Toolchain: $BT"

# ── Keystore-Creds (env hat Vorrang, sonst .keystore.env) ────────────────────
if [ -z "${KIKOMPETENZ_RELEASE_STORE_FILE:-}" ] && [ -f .keystore.env ]; then
  # shellcheck disable=SC1091
  . ./.keystore.env
  export KIKOMPETENZ_RELEASE_STORE_FILE="$STOREFILE" \
         KIKOMPETENZ_RELEASE_STORE_PASSWORD="$STOREPASS" \
         KIKOMPETENZ_RELEASE_KEY_ALIAS="$KEYALIAS" \
         KIKOMPETENZ_RELEASE_KEY_PASSWORD="$KEYPASS"
fi
KS="${KIKOMPETENZ_RELEASE_STORE_FILE:-ki-kompetenz-release.jks}"
case "$KS" in /*) ;; *) KS="$REPO/$KS" ;; esac
[ -f "$KS" ] || { echo "FAIL: Keystore nicht gefunden: $KS"; exit 2; }

# ── Erwartungswerte ──────────────────────────────────────────────────────────
VC=$(grep -oP '^versionCode=\K.*' version.properties)
VN=$(grep -oP '^versionName=\K.*' version.properties)
PKG=$(grep -oP 'applicationId\s*=\s*"\K[^"]+' app/build.gradle.kts | head -1)
[ -n "$VC" ] && [ -n "$VN" ] && [ -n "$PKG" ] || { echo "FAIL: version.properties/gradle nicht lesbar"; exit 2; }
# Basis-Package (ohne .free-Suffix) fuer Mapping-Check (Source-Code-Package != applicationId)
BASE_PKG="${PKG%.free}"
echo "== Erwartet: package=$PKG versionCode=$VC versionName=$VN"

echo "== Phase A: lokale Verifikation"
if [ -f "$APK" ]; then ok "APK vorhanden ($APK, $(stat -c%s "$APK") Bytes)"; else fail "APK fehlt: $APK"; fi
CERTS="$("$APKSIGNER" verify --print-certs "$APK" 2>/dev/null)"
[ -n "$CERTS" ] && ok "apksigner: Signatur gueltig" || fail "apksigner: Signatur UNGUELTIG"
APKFP=$(echo "$CERTS" | grep -oP 'SHA-256 digest: \K[0-9a-fA-F]+' | head -1 | tr -d ':' | tr 'A-F' 'a-f')
KSFPR=$("$KEYTOOL" -list -v -keystore "$KS" -storepass "$KIKOMPETENZ_RELEASE_STORE_PASSWORD" \
        -alias "$KIKOMPETENZ_RELEASE_KEY_ALIAS" 2>/dev/null | grep -oP 'SHA256:\s*\K[0-9A-Fa-f:]+' | head -1 | tr -d ':' | tr 'A-F' 'a-f')
if [ -n "$APKFP" ] && [ -n "$KSFPR" ] && [ "$APKFP" = "$KSFPR" ]; then
  ok "APK-Cert == Keystore-Cert (${APKFP:0:16}...)"
else
  fail "Fingerprint-Mismatch: APK=${APKFP:0:16}... Keystore=${KSFPR:0:16}..."
fi
if [ "$APKFP" = "$EXPECTED_FP" ]; then
  ok "Pin: bekannter Release-Fingerprint stimmt"
else
  fail "Pin-Verletzung: $APKFP != $EXPECTED_FP (falsches Keystore? neu erzeugt?)"
fi

# 2) Badging vs. Erwartung
BADGE="$("$AAPT2" dump badging "$APK" 2>/dev/null)"
APKPKG=$(echo "$BADGE" | grep -oP "^package: name='\K[^']+" | head -1)
APKVC=$(echo "$BADGE" | grep -oP "^package: name='[^']*' versionCode='\K[^']+" | head -1)
APKVN=$(echo "$BADGE" | grep -oP "versionName='\K[^']+" | head -1)
[ "$APKPKG" = "$PKG" ] && ok "package = $APKPKG" || fail "package: $APKPKG != $PKG"
[ "$APKVC" = "$VC" ]   && ok "versionCode = $APKVC" || fail "versionCode: $APKVC != $VC"
[ "$APKVN" = "$VN" ]   && ok "versionName = $APKVN" || fail "versionName: $APKVN != $VN"

# 3) ProGuard-Regression (Bug #8) — nur Nicht-Kommentar-Zeilen pruefen
if grep -v '^\s*#' app/proguard-rules.pro | grep -q "$FORBIDDEN_PKG"; then
  fail "proguard-rules.pro enthaelt verbotenes Altpaket $FORBIDDEN_PKG (in aktiver Regel)"
else
  ok "proguard-rules.pro frei von Altpaket $FORBIDDEN_PKG (in aktiven Regeln)"
fi
if [ -f "$MAPPING" ] && grep -q "$BASE_PKG" "$MAPPING"; then
  ok "mapping.txt vorhanden und enthaelt $BASE_PKG"
else
  fail "mapping.txt fehlt oder enthaelt $BASE_PKG nicht (Release-Minify gelaufen?)"
fi

# 4) AAB
if [ -f "$AAB" ]; then
  ok "AAB vorhanden ($AAB, $(stat -c%s "$AAB") Bytes)"
elif [ "$AAB_REQUIRED" = "1" ]; then
  fail "AAB required, aber fehlt: $AAB"
else
  echo "  WARN  AAB fehlt (nur Warnung): $AAB"
fi

# ── Phase B: Geraet ──────────────────────────────────────────────────────────
if [ "$DEVICE" = "1" ]; then
  echo "== Phase B: Geraete-Smoke-Test"
  if adb get-state >/dev/null 2>&1; then
    ok "Geraet via adb verbunden"
    if ! adb install -r -g "$APK" >/dev/null 2>&1; then
      echo "  INFO  install -r fehlgeschlagen (Signatur-Konflikt Debug<->Release) -> uninstall + retry"
      adb uninstall "$PKG" >/dev/null 2>&1
      adb install -r -g "$APK" >/dev/null 2>&1 && ok "Release-APK installiert (nach Neuinstallation)" \
        || fail "Installation fehlgeschlagen"
    else
      ok "Release-APK installiert (Update ueber bestehende Installation)"
    fi
    adb shell input keyevent KEYCODE_WAKEUP 2>/dev/null
    sleep 1
    adb shell am force-stop "$PKG" 2>/dev/null
    adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 \
      && ok "App gestartet (Launcher-Intent)" || fail "App-Start fehlgeschlagen"
    sleep 8
    # Keyguard/Notification-Shade erkennen (Geraet kann sich waehrend sleep auto-sperren)
    FOCUS="$(adb shell dumpsys window 2>/dev/null | grep -oP 'mCurrentFocus=\K[^ ]+' | head -1)"
    if echo "$FOCUS" | grep -qiE 'NotificationShade|Keyguard|lock'; then
      fail "Geraet gesperrt (Keyguard aktiv: $FOCUS) — bitte Geraet entsperren und --device erneut ausfuehren"
      echo "== Ergebnis: $PASS PASS, $FAIL FAIL"
      [ "$FAIL" = "0" ] && exit 0 || exit 1
    fi
    DUMP="$REPO/build/ui-dump.xml"
    adb shell uiautomator dump //sdcard//ui.xml >/dev/null 2>&1 && adb pull //sdcard//ui.xml "$DUMP" >/dev/null 2>&1
    MARKERS="${KIKOMPETENZ_UI_MARKERS:-Lessons|Mini-Games|Profile|KI Competence}"
    if [ -f "$DUMP" ] && python -X utf8 -c "
import re, sys
xml = open(sys.argv[1], encoding='utf-8', errors='replace').read()
sys.exit(0 if re.search(sys.argv[2], xml) else 1)" "$DUMP" "$MARKERS"; then
      ok "UI-Marker gefunden ($MARKERS)"
    else
      fail "kein UI-Marker im Home-Screen-Dump ($MARKERS)"
    fi
    # Volltest: Lessons-Tab + Online-Laden (empirischer ProGuard-Fix-Nachweis)
    if [ "${KIKOMPETENZ_DEVICE_FULL:-0}" = "1" ]; then
      LX="${KIKOMPETENZ_TAB_X:-631}"; LY="${KIKOMPETENZ_TAB_Y:-2136}"
      adb shell input tap "$LX" "$LY"
      sleep 7
      adb shell uiautomator dump //sdcard//ui.xml >/dev/null 2>&1 && adb pull //sdcard//ui.xml "$DUMP" >/dev/null 2>&1
      LMARKERS="${KIKOMPETENZ_LESSON_MARKERS:-KI-Lernen|EU AI Act|Was ist|Lessons could not}"
      if [ -f "$DUMP" ] && python -X utf8 -c "
import re, sys
xml = open(sys.argv[1], encoding='utf-8', errors='replace').read()
hits = re.findall(sys.argv[2], xml)
print('      Treffer:', sorted(set(hits))[:3])
sys.exit(0 if hits else 1)" "$DUMP" "$LMARKERS"; then
        ok "Lessons-Inhalte sichtbar (Release-Build laedt Lektionen)"
      else
        fail "kein Lektionsinhalt im Lessons-Tab (Release-ProGuard-Verdacht!)"
      fi
    fi
  else
    fail "kein Geraet via adb verbunden"
  fi
fi

echo "== Ergebnis: $PASS PASS, $FAIL FAIL"
[ "$FAIL" = "0" ] && exit 0 || exit 1
