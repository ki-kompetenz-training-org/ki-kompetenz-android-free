# Release Automation Scripts

Vollautomatisierte Build & Release Pipeline — **kein LLM "Brain Power" mehr nötig**.

## 🚀 Quick Start

### EIN BEFEHL — Alles automatisiert:
```bash
bash scripts/release-pipeline.sh
```

**Output:**
- ✅ Quality Gate (7 Checks)
- ✅ Clean Build
- ✅ Signed AAB (4.5MB)
- ✅ Signature Verified

## 📜 Scripts

### `release-pipeline.sh` — Vollautomatische Pipeline
```bash
bash scripts/release-pipeline.sh
```

**Was passiert:**
1. Quality Gate (7 Checks, <30s)
2. Clean build + Sign AAB (<60s)
3. Signature verification
4. Artifact listing

**Exit code:** 0 = success, 1 = failure

### `quality-gate.sh` — Quality Checks
```bash
bash scripts/quality-gate.sh
```

**7 Checks:**
1. Compile
2. 164 Unit Tests
3. Hardcoded colors (WARN if found)
4. Hardcoded strings (WARN if found)
5. Duplicate imports
6. Content descriptions (skipped)
7. Debug APK exists

**Exit code:** 0 = all PASS, 1 = any FAIL

## 🔐 Signing Configuration

### `.keystore.env` (nicht commited)
```bash
STOREFILE=ki-kompetenz-release.jks
STOREPASS=your_store_password
KEYPASS=your_key_password
KEYALIAS=kikompetenz
```

### Environment Variables (exported by script)
```bash
KIKOMPETENZ_RELEASE_STORE_FILE
KIKOMPETENZ_RELEASE_STORE_PASSWORD
KIKOMPETENZ_RELEASE_KEY_ALIAS
KIKOMPETENZ_RELEASE_KEY_PASSWORD
```

## ✅ Google Play Compliance

- ✅ **Signed AAB** — `META-INF/KIKOMPET.RSA` present
- ✅ **Target SDK 35** — Android 15 compliant
- ✅ **R8/ProGuard** — 88% size reduction (19MB → 4.5MB)
- ✅ **Certificate Pinning** — Let's Encrypt ISRG X1/X2

## 📦 Output Artifacts

```bash
app/build/outputs/bundle/release/app-release.aab  # 4.5MB, signed
```

**Signature verification:**
```bash
unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF/.*\.RSA
# Output: META-INF/KIKOMPET.RSA
```

## 🔄 CI/CD Integration

### GitHub Actions
```yaml
- name: Build Release
  run: bash scripts/release-pipeline.sh
  
- name: Upload AAB
  uses: actions/upload-artifact@v4
  with:
    name: app-release
    path: app/build/outputs/bundle/release/app-release.aab
```

## 🛠️ Troubleshooting

### "Keystore not found"
```bash
ls -la ki-kompetenz-release.jks
# Or create new:
./scripts/generate-keystore.sh
```

### "Signature verification failed"
```bash
# Check .keystore.env exists and has all variables
cat .keystore.env

# Rebuild
bash scripts/release-pipeline.sh
```

## 📊 Build Statistics

- **Compile time:** ~10s
- **Tests:** 164 unit tests, <15s
- **R8/ProGuard:** ~20s
- **Total:** <60s
- **AAB size:** 4.5MB (88% reduction from 19MB)

## 🎯 Next Steps After Build

1. **Upload to Google Play Console**
   ```bash
   # AAB path
   app/build/outputs/bundle/release/app-release.aab
   ```

2. **Verify signature** (optional)
   ```bash
   unzip -l app/build/outputs/bundle/release/app-release.aab | grep META-INF
   ```

3. **Deploy** — Manual steps in Play Console:
   - Create release
   - Upload AAB
   - Content rating
   - Store listing
   - Rollout

---

**No LLM needed. Hard-wired automation.**
