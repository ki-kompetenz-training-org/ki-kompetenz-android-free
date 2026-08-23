#!/usr/bin/env bash
#
# publish-fdroid.sh — Publish unsigned APK to self-hosted F-Droid repo
#
# Usage:
#   bash scripts/publish-fdroid.sh [versionCode]
#
# Prerequisites:
#   - SSH key at ~/.ssh/id_ed25519_ansible
#   - Server: contextual-intelligence.org
#   - fdroidserver installed on server (pip install fdroidserver)
#   - Keystore at /home/weiss/fdroid/keystore.p12
#
# What this script does:
#   1. Download unsigned APK from GitHub releases (or use local build)
#   2. Upload to server:/home/weiss/fdroid/unsigned/
#   3. SSH: fdroid publish (sign APK with repo key)
#   4. SSH: fdroid update (regenerate repo index)
#   5. Verify HTTPS endpoints
#
set -euo pipefail

# ── Config ──────────────────────────────────────────────────────────────────
SERVER="weiss@195.90.216.159"
SSH_KEY="${HOME}/.ssh/id_ed25519_ansible"
SSH_OPTS="-o IdentitiesOnly=yes -o ConnectTimeout=15"
FDROID_BASE="/home/weiss/fdroid"
REPO_URL="https://fdroid.contextual-intelligence.org/fdroid/repo"
APP_ID="ai.ki_kompetenz_training_org.free"

# ── Args ────────────────────────────────────────────────────────────────────
VERSION_CODE="${1:-}"
if [[ -z "$VERSION_CODE" ]]; then
  VERSION_CODE=$(grep versionCode version.properties 2>/dev/null | cut -d= -f2 || echo "")
  if [[ -z "$VERSION_CODE" ]]; then
    echo "ERROR: versionCode not provided and version.properties not found"
    echo "Usage: bash scripts/publish-fdroid.sh [versionCode]"
    exit 1
  fi
fi

VERSION_NAME=$(grep versionName version.properties 2>/dev/null | cut -d= -f2 || echo "")
echo "==> Publishing $APP_ID v$VERSION_NAME (versionCode=$VERSION_CODE) to F-Droid"

# ── Step 1: Get the unsigned APK ────────────────────────────────────────────
APK_LOCAL="/tmp/${APP_ID}_${VERSION_CODE}.apk"

if [[ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]]; then
  echo "==> Using local build"
  cp "app/build/outputs/apk/release/app-release-unsigned.apk" "$APK_LOCAL"
elif command -v gh &>/dev/null; then
  echo "==> Downloading from GitHub releases"
  gh release download "v${VERSION_NAME}" \
    --repo ki-kompetenz-training-org/ki-kompetenz-android-free \
    --pattern "*.apk" \
    --dir "$(dirname "$APK_LOCAL")" 2>/dev/null || true
  # Rename if needed
  if [[ -f "/tmp/app-release-unsigned.apk" ]]; then
    mv "/tmp/app-release-unsigned.apk" "$APK_LOCAL"
  fi
else
  echo "ERROR: No local APK and gh CLI not available"
  exit 1
fi

if [[ ! -f "$APK_LOCAL" ]]; then
  echo "ERROR: APK not found at $APK_LOCAL"
  exit 1
fi

echo "==> APK: $(du -h "$APK_LOCAL" | cut -f1)"

# ── Step 2: Upload to server ────────────────────────────────────────────────
echo "==> Uploading to server..."
scp $SSH_OPTS -i "$SSH_KEY" "$APK_LOCAL" "${SERVER}:${FDROID_BASE}/unsigned/${APP_ID}_${VERSION_CODE}.apk"

# ── Step 3: Sign APK (fdroid publish) ──────────────────────────────────────
echo "==> Signing APK (fdroid publish)..."
ssh $SSH_OPTS -i "$SSH_KEY" "${SERVER}" "
  cd ${FDROID_BASE}
  export FDROID_KEY_STORE_PASS=changeit
  export FDROID_KEY_PASS=changeit
  ~/.local/bin/fdroid publish --verbose 2>&1
"

# ── Step 4: Regenerate index (fdroid update) ───────────────────────────────
echo "==> Regenerating index (fdroid update)..."
ssh $SSH_OPTS -i "$SSH_KEY" "${SERVER}" "
  cd ${FDROID_BASE}
  export FDROID_KEY_STORE_PASS=changeit
  export FDROID_KEY_PASS=changeit
  ~/.local/bin/fdroid update --pretty 2>&1
"

# ── Step 5: Verify HTTPS endpoints ──────────────────────────────────────────
echo "==> Verifying HTTPS endpoints..."
sleep 2

ENTRY=$(curl -s -o /dev/null -w '%{http_code}' "${REPO_URL}/entry.jar")
INDEX_V2=$(curl -s -o /dev/null -w '%{http_code}' "${REPO_URL}/index-v2.json")
APK=$(curl -s -o /dev/null -w '%{http_code}' "${REPO_URL}/${APP_ID}_${VERSION_CODE}.apk")

echo "   entry.jar:       $ENTRY"
echo "   index-v2.json:   $INDEX_V2"
echo "   APK:             $APK"

if [[ "$ENTRY" == "200" && "$INDEX_V2" == "200" && "$APK" == "200" ]]; then
  echo ""
  echo "✅ Published successfully!"
  echo "   F-Droid repo: ${REPO_URL}"
  echo "   APK:          ${REPO_URL}/${APP_ID}_${VERSION_CODE}.apk"
  echo ""
  echo "   Add to F-Droid client: ${REPO_URL}"
else
  echo ""
  echo "⚠️  Some endpoints returned non-200:"
  echo "   entry.jar: $ENTRY, index-v2.json: $INDEX_V2, APK: $APK"
  exit 1
fi
