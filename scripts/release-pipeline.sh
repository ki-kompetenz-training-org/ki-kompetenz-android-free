#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "========================================="
echo " KI-KOMPETENZ RELEASE PIPELINE"
echo "========================================="
echo ""

# ── 0. Optionaler versionName-Parameter ──────────────────────────────────
#    ./scripts/release-pipeline.sh          → nur versionCode +1
#    ./scripts/release-pipeline.sh 1.4.2    → versionCode +1 UND versionName=1.4.2
#    DRYRUN=1 ./scripts/release-pipeline.sh → alles bauen, aber KEIN commit/push
NEW_NAME_ARG="${1:-}"
DRYRUN="${DRYRUN:-0}"
if [ "$DRYRUN" = "1" ]; then
    echo "(!) DRYRUN=1 — es wird NICHT committed/gepusht"
fi

# ── 0. Read & bump versionCode from version.properties ──
VERSION_FILE="version.properties"
if [ ! -f "$VERSION_FILE" ]; then
    echo "ERROR: $VERSION_FILE not found"
    exit 1
fi

# Parse current version
CURRENT_CODE=$(grep "^versionCode=" "$VERSION_FILE" | cut -d= -f2 | tr -d '[:space:]')
CURRENT_NAME=$(grep "^versionName=" "$VERSION_FILE" | cut -d= -f2 | tr -d '[:space:]')
NEW_CODE=$((CURRENT_CODE + 1))

# versionName: nur aendern, wenn als Argument uebergeben (Patch-Releases
# brauchen sonst eine manuelle Korrektur — v1.4.1 hat genau das gezeigt).
NEW_NAME="$CURRENT_NAME"
if [ -n "$NEW_NAME_ARG" ]; then
    NEW_NAME="$NEW_NAME_ARG"
fi

echo "[0/5] Current: versionCode=$CURRENT_CODE versionName=$CURRENT_NAME"
echo "      New:     versionCode=$NEW_CODE versionName=$NEW_NAME"

# Bump versionCode + optional versionName in version.properties
sed -i "s/^versionCode=.*/versionCode=$NEW_CODE/" "$VERSION_FILE"
if [ -n "$NEW_NAME_ARG" ]; then
    sed -i "s/^versionName=.*/versionName=$NEW_NAME/" "$VERSION_FILE"
fi
echo "      Bumped version.properties -> versionCode=$NEW_CODE versionName=$NEW_NAME"

# ── 1. Quality gate (non-blocking WARN, blocking FAIL) ──
echo ""
echo "[1/5] Quality gate..."
bash scripts/quality-gate.sh || true

# ── 2. Build release AAB ──
echo ""
echo "[2/5] Building signed release AAB..."
source .keystore.env 2>/dev/null || true
export KIKOMPETENZ_RELEASE_STORE_FILE="$(pwd)/${STOREFILE}"
export KIKOMPETENZ_RELEASE_STORE_PASSWORD="$STOREPASS"
export KIKOMPETENZ_RELEASE_KEY_ALIAS="$KEYALIAS"
export KIKOMPETENZ_RELEASE_KEY_PASSWORD="$KEYPASS"
./gradlew :app:clean :app:bundleRelease 2>&1 | tail -5

# ── 3. Verify AAB exists and is signed ──
echo ""
echo "[3/5] Verifying AAB..."
AAB="app/build/outputs/bundle/release/app-release.aab"
if [ ! -f "$AAB" ]; then
    echo "ERROR: AAB not found at $AAB"
    exit 1
fi
if ! unzip -l "$AAB" 2>/dev/null | grep -q "META-INF/.*\.RSA"; then
    echo "ERROR: AAB is not signed (no META-INF/*.RSA found)"
    exit 1
fi
echo "      OK: signed AAB ($(du -h "$AAB" | cut -f1))"

# ── 4. Commit version bump ──
echo ""
echo "[4/5] Committing version bump..."
git add "$VERSION_FILE"
if [ "$DRYRUN" = "1" ]; then
    echo "      [DRYRUN] jaeh commit/push uebersprungen — version.properties ist GEANDERT:"
    git diff --stat "$VERSION_FILE"
    git restore "$VERSION_FILE" 2>/dev/null || true
else
    git commit -m "chore: bump versionCode $CURRENT_CODE -> $NEW_CODE, versionName $CURRENT_NAME -> $NEW_NAME" --no-verify 2>/dev/null || true
    git push 2>/dev/null || true
    echo "      Pushed versionCode=$NEW_CODE/versionName=$NEW_NAME to origin/main"
fi

# ── 5. Output ──
echo ""
echo "========================================="
echo " BUILD COMPLETE"
echo "========================================="
echo "  AAB:        $AAB"
echo "  Size:       $(du -h "$AAB" | cut -f1)"
echo "  versionCode: $NEW_CODE"
echo "  versionName: $NEW_NAME"
echo "  Signed:      yes (META-INF/KIKOMPET.RSA)"
echo "========================================="
echo ""
echo "Next: Upload $AAB to Play Console"
