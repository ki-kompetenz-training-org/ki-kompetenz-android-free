#!/usr/bin/env bash
# run-tests.sh
# Automatisierte Test-Suite für Android MVP Testing

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🧪 KI-Kompetenz Android - Automated Test Suite"
echo "=============================================="

# Check device connected
echo "📱 Checking for connected device..."
DEVICE_COUNT=$(adb devices | grep -c "device$" || true)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ No device found. Please connect your Pixel device."
    echo "💡 Enable USB Debugging in Developer Options"
    exit 1
fi

echo "✅ Device found: $(adb devices | grep "device$" | head -1 | awk '{print $1}')"

# Get device info
echo ""
echo "📊 Device Info:"
echo "  Model: $(adb shell getprop ro.product.model | tr -d '\r')"
echo "  Android: $(adb shell getprop ro.build.version.release | tr -d '\r')"
echo "  API Level: $(adb shell getprop ro.build.version.sdk | tr -d '\r')"

# Install app (if not already installed)
echo ""
echo "📦 Installing app..."
cd "$PROJECT_ROOT"
./gradlew installRelease

# Run all tests
echo ""
echo "🚀 Running Automated Tests..."
echo "=============================================="

# Unit Tests (JVM)
echo ""
echo "📝 Unit Tests (JVM)..."
./gradlew testReleaseUnitTest

# Integration Tests (Android)
echo ""
echo "📱 Android Integration Tests..."
./gradlew connectedAndroidTest

# Generate Coverage Report
echo ""
echo "📊 Generating Coverage Report..."
./gradlew jacocoTestReport

# Show Results
echo ""
echo "=============================================="
echo "✅ Test Suite Completed!"
echo ""

# Check for failures
TEST_OUTPUT=$(./gradlew testReleaseUnitTest --console=plain 2>&1)
if echo "$TEST_OUTPUT" | grep -q "FAILED"; then
    echo "❌ Some tests failed. Check logs below."
    echo "$TEST_OUTPUT" | grep -A5 "FAILED"
    exit 1
else
    echo "✅ All tests passed!"
fi

# Show coverage
echo ""
echo "📈 Coverage Report:"
if [ -f "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.html" ]; then
    echo "  HTML: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.html"
fi
if [ -f "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml" ]; then
    echo "  XML: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
fi

echo ""
echo "🎉 MVP Testing Complete!"
echo "=============================================="
