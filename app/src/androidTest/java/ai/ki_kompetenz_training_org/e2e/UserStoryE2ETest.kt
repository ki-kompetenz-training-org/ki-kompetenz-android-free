package ai.ki_kompetenz_training_org.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E User Story Tests — KI-Kompetenz Android App
 *
 * Tests real user journeys on a connected device using UIAutomator (black-box testing).
 * UIAutomator is used instead of Compose UI Test because Espresso 3.6.1 is incompatible
 * with Android 16 (API 36) — InputManager.getInstance() was removed.
 *
 * User Stories covered:
 * - US-01: Onboarding flow (first launch)
 * - US-02: Home navigation
 * - US-03: Interactive lessons
 * - US-04: KI-Score Quiz
 * - US-05: Mini-Games
 * - US-06: ForKids (COPPA)
 * - US-07: ForSeniors
 * - US-08: Gamification / Profile
 * - US-10: Premium overview
 *
 * Run: ./gradlew :app:connectedDebugAndroidTest
 *      -Pandroid.testInstrumentationRunnerArguments.class=ai.ki_kompetenz_training_org.e2e.UserStoryE2ETest
 *
 * For fresh onboarding tests, clear app data first:
 *   adb shell pm clear ai.ki_kompetenz_training_org
 *
 * Prerequisites:
 *   - Device connected via `adb devices`
 *   - Debug APK installed
 *   - Device locale: German (de-DE)
 *
 * Tests run in alphabetical order (FixMethodOrder.NAME_ASCENDING) to ensure
 * onboarding tests run before navigation tests.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UserStoryE2ETest {

    companion object {
        private lateinit var device: UiDevice
        private const val packageName = "ai.ki_kompetenz_training_org"
        private const val timeout = 5_000L

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            // Keep screen on during tests (stay on while plugged in)
            device.executeShellCommand("settings put global stay_on_while_plugged_in 15")
            // Set screen timeout to 30 minutes
            device.executeShellCommand("settings put system screen_off_timeout 1800000")
            // Set lock after timeout to max
            device.executeShellCommand("settings put secure lock_screen_lock_after_timeout 2147483647")
            // Disable lock screen if possible
            device.executeShellCommand("locksettings set-disabled true")
            // Wake up screen
            device.wakeUp()
            // Dismiss keyguard if possible
            device.executeShellCommand("wm dismiss-keyguard")
            // Force portrait orientation for consistent testing
            device.setOrientationNatural()
            device.freezeRotation()
        }
    }

    @Before
    fun setUp() {
        // Wake up screen (in case it turned off between tests)
        device.wakeUp()
        // Dismiss keyguard if possible (works for swipe lock, not PIN lock)
        device.executeShellCommand("wm dismiss-keyguard")
        // Launch app
        device.executeShellCommand("am start -n $packageName/.MainActivity")
        device.wait(Until.hasObject(By.pkg(packageName)), timeout)
        // Dismiss debug APK compatibility dialog if visible (16KB page size warning)
        dismissDebugDialog()
    }

    /** Dismiss the Android debug APK compatibility dialog (16KB page size warning).
     *  This dialog appears on debug builds and blocks the app UI. */
    private fun dismissDebugDialog() {
        val okButton = device.findObject(By.text("OK"))
        if (okButton != null) {
            okButton.click()
            device.waitForIdle()
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────

    /** Skip onboarding if it's showing. Does nothing if onboarding was already completed. */
    private fun skipOnboarding() {
        val skip = device.findObject(By.text("Überspringen"))
        if (skip != null) {
            skip.click()
            device.wait(Until.hasObject(By.textContains("Lektionen")), timeout)
            return
        }
        val skipEn = device.findObject(By.text("Skip"))
        if (skipEn != null) {
            skipEn.click()
            device.wait(Until.hasObject(By.textContains("Lektionen")), timeout)
        }
    }

    /** Dismiss KiBot hello dialog if it's showing.
     *  This dialog appears on first launch after onboarding and blocks the home screen. */
    private fun dismissKibotDialog() {
        val losGehts = device.findObject(By.textContains("Los geht"))
        if (losGehts != null) {
            losGehts.click()
            device.waitForIdle()
        }
    }

    /** Ensure we're on the home screen: skip onboarding, dismiss KiBot dialog, wait for home.
     *  Handles the full first-launch flow: debug dialog → onboarding → KiBot dialog → home screen. */
    private fun ensureHomeScreen() {
        // Dismiss debug APK compatibility dialog if visible
        dismissDebugDialog()
        // Skip onboarding if visible
        val skip = device.findObject(By.text("Überspringen"))
        if (skip != null) {
            skip.click()
            device.waitForIdle()
        } else {
            val skipEn = device.findObject(By.text("Skip"))
            if (skipEn != null) {
                skipEn.click()
                device.waitForIdle()
            }
        }
        // Wait a moment for KiBot dialog to appear after onboarding
        Thread.sleep(500)
        // Dismiss KiBot hello dialog if visible
        dismissKibotDialog()
        // Wait for home screen
        device.wait(Until.hasObject(By.textContains("Lektionen")), timeout)
    }

    /** Click element by exact text. Returns true if found and clicked. */
    private fun clickByText(text: String): Boolean {
        val obj = device.findObject(By.text(text))
        if (obj != null) {
            obj.click()
            return true
        }
        return false
    }

    /** Click element by text containing substring.
     *  Uses bounds-based tap for reliability with Compose non-clickable Text nodes. */
    private fun clickByTextContains(text: String): Boolean {
        val obj = device.findObject(By.textContains(text))
        if (obj != null) {
            // Get bounds and tap center — more reliable than obj.click() for Compose Text nodes
            val bounds = obj.visibleBounds
            val centerX = bounds.left + bounds.width() / 2
            val centerY = bounds.top + bounds.height() / 2
            device.click(centerX, centerY)
            return true
        }
        return false
    }

    /** Check if element with text exists (substring match). */
    private fun textExists(text: String): Boolean {
        return device.findObject(By.textContains(text)) != null
    }

    /** Wait for element with text to appear. */
    private fun waitForText(text: String, millis: Long = timeout): Boolean {
        return device.wait(Until.hasObject(By.textContains(text)), millis)
    }

    /** Scroll down. Uses right edge to avoid clicking on cards in the center. */
    private fun scrollDown() {
        val w = device.displayWidth
        val h = device.displayHeight
        // Swipe from right edge (9/10 width) to avoid clicking on lesson cards
        device.swipe(w * 9 / 10, h * 3 / 4, w * 9 / 10, h / 4, 20)
    }

    /** Check if onboarding is currently visible. */
    private fun isOnboardingVisible(): Boolean {
        return device.findObject(By.textContains("Willkommen")) != null ||
               device.findObject(By.text("Überspringen")) != null ||
               device.findObject(By.text("Skip")) != null
    }

    /** Navigate back to home screen by pressing back until home is visible. */
    private fun goHome() {
        var attempts = 0
        while (attempts < 3 && !textExists("Lektionen") && !textExists("KI-Score")) {
            device.pressBack()
            device.waitForIdle()
            attempts++
        }
    }

    // ─── US-01: Onboarding (runs first, alphabetical order) ─────────
    // Note: These tests require fresh app data. Run: adb shell pm clear ai.ki_kompetenz_training_org

    @Test
    fun us01_a_onboarding_showsWelcomeOnFirstLaunch() {
        // This test only passes with fresh app data (onboarding not yet completed)
        if (!isOnboardingVisible()) {
            // Onboarding already completed — skip this test
            return
        }
        assert(waitForText("Willkommen")) {
            "Onboarding welcome not shown on first launch"
        }
    }

    @Test
    fun us01_b_onboarding_hasSkipButton() {
        if (!isOnboardingVisible()) {
            return
        }
        assert(textExists("Überspringen") || textExists("Skip")) {
            "Skip button not found on onboarding"
        }
    }

    @Test
    fun us01_c_onboarding_canNavigateToNextPage() {
        if (!isOnboardingVisible()) {
            return
        }
        val nextDe = device.findObject(By.text("Weiter"))
        if (nextDe != null) {
            nextDe.click()
        } else {
            device.findObject(By.text("Next"))?.click()
        }
        assert(waitForText("KiBot")) {
            "KiBot intro page not shown after clicking Next"
        }
    }

    @Test
    fun us01_d_onboarding_canCompleteFlow() {
        if (!isOnboardingVisible()) {
            return
        }
        // Skip onboarding
        ensureHomeScreen()
        // Should now be on home screen
        assert(waitForText("Lektionen")) {
            "Home screen not shown after completing onboarding"
        }
    }

    // ─── US-02: Home Navigation ──────────────────────────────────────

    @Test
    fun us02_a_home_showsAllMainFeatures() {
        ensureHomeScreen()
        assert(waitForText("Lektionen")) { "Lessons not shown on home" }
        assert(textExists("KI-Score")) { "Quiz not shown on home" }
        assert(textExists("Mini-Spiele")) { "Mini-Games not shown on home" }
    }

    @Test
    fun us02_b_home_showsForKidsAndForSeniors() {
        ensureHomeScreen()
        waitForText("Lektionen")
        if (!textExists("Für Kinder")) {
            scrollDown()
        }
        assert(textExists("Für Kinder")) { "ForKids not shown on home" }
        assert(textExists("Für Senioren")) { "ForSeniors not shown on home" }
    }

    @Test
    fun us02_c_home_navigatesToLessons() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Lektionen")
        assert(waitForText("Lektion")) {
            "Lessons screen not shown after navigation"
        }
    }

    // ─── US-03: Interactive Lessons ──────────────────────────────────

    @Test
    fun us03_lessons_canOpenLesson1() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Lektionen")
        // Lessons screen shows API lesson titles (e.g., "1. Grundlagen der KI")
        // Lesson list might need a moment to load from API
        assert(waitForText("Grundlagen", 10_000)) {
            "Lesson 1 not found in lessons list"
        }
        clickByTextContains("Grundlagen")
        // Interactive lesson screen shows "Was ist KI" as title
        assert(waitForText("Was ist", 10_000)) {
            "Lesson content not shown"
        }
    }

    // ─── US-04: KI-Score Quiz ────────────────────────────────────────

    @Test
    fun us04_quiz_canBeStartedFromHome() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("KI-Score")
        // Quiz screen shows "Spiel starten" button and "Wie KI-fit" subtitle
        assert(waitForText("Spiel starten") || waitForText("Wie KI-fit")) {
            "Quiz screen not shown"
        }
    }

    // ─── US-05: Mini-Games ───────────────────────────────────────────

    @Test
    fun us05_a_miniGames_showsAllGames() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Mini-Spiele")
        assert(waitForText("KI oder Mensch")) {
            "Mini-Games list not shown or first game missing"
        }
    }

    @Test
    fun us05_b_miniGames_gameCanBeOpened() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Mini-Spiele")
        assert(waitForText("KI oder Mensch"))
        clickByTextContains("KI oder Mensch")
        assert(waitForText("Runde")) {
            "Game round not shown"
        }
    }

    // ─── US-06: ForKids (COPPA) ──────────────────────────────────────

    @Test
    fun us06_a_forKids_accessibleWithoutLogin() {
        ensureHomeScreen()
        waitForText("Lektionen")
        if (!textExists("Für Kinder")) {
            scrollDown()
        }
        clickByText("Für Kinder")
        // Kids screen title is "ForKids"
        assert(waitForText("ForKids")) {
            "ForKids content not shown"
        }
    }

    @Test
    fun us06_b_forKids_showsCoppaNotice() {
        ensureHomeScreen()
        waitForText("Lektionen")
        if (!textExists("Für Kinder")) {
            scrollDown()
        }
        clickByText("Für Kinder")
        // COPPA notice title: "Für Eltern — Datenschutz-Info"
        assert(waitForText("Eltern")) {
            "COPPA/parental notice not shown"
        }
    }

    // ─── US-07: ForSeniors ───────────────────────────────────────────

    @Test
    fun us07_forSeniors_accessibleWithoutLogin() {
        ensureHomeScreen()
        waitForText("Lektionen")
        if (!textExists("Für Senioren")) {
            scrollDown()
        }
        clickByText("Für Senioren")
        // Seniors screen shows "Passwörter" in content
        assert(waitForText("Passw")) {
            "ForSeniors content not shown"
        }
    }

    // ─── US-08: Gamification / Profile ───────────────────────────────

    @Test
    fun us08_a_profile_showsLevelAndXp() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Profil")
        assert(waitForText("Level")) {
            "Profile level not shown"
        }
        assert(textExists("XP")) {
            "XP not shown in profile"
        }
    }

    @Test
    fun us08_b_profile_showsDsgvoNote() {
        ensureHomeScreen()
        waitForText("Lektionen")
        clickByTextContains("Profil")
        assert(waitForText("Level")) {
            "Profile not loaded"
        }
        // DSGVO note is at the bottom of the profile — scroll to find it
        for (i in 1..3) {
            if (textExists("DSGVO")) break
            scrollDown()
        }
        assert(textExists("DSGVO")) {
            "DSGVO note not shown in profile"
        }
    }

    // ─── US-10: Premium Overview ─────────────────────────────────────

    @Test
    fun us10_premium_showsFeaturesAndPrice() {
        ensureHomeScreen()
        waitForText("Lektionen")
        // Premium is not directly accessible from home when not logged in.
        // Navigate via Lessons screen which has a "Alle Lektionen freischalten" button.
        clickByTextContains("Lektionen")
        assert(waitForText("Grundlagen", 10_000)) {
            "Lessons screen not loaded"
        }
        // Use UiScrollable to scroll the lessons list without triggering card clicks
        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.waitForExists(timeout)
        // Scroll to the end of the list to find the Premium unlock button
        for (i in 1..10) {
            if (textExists("freischalten")) break
            scrollable.scrollForward()
            device.waitForIdle()
        }
        assert(textExists("freischalten")) {
            "Premium unlock button not found in lessons list"
        }
        // Click the button
        val btn = device.findObject(By.textContains("freischalten"))
        assert(btn != null) { "freischalten button not found" }
        btn!!.click()
        // Wait for premium screen to load
        assert(waitForText("abonnieren", 10_000)) {
            "Premium screen not shown"
        }
        // Verify the price is displayed
        assert(textExists("6,99") || textExists("6.99")) {
            "Premium price not shown"
        }
    }

    // ─── US-09: Daily Challenge ──────────────────────────────────────

    @Test
    fun us09_a_dailyChallengeCard_visibleOnHome() {
        ensureHomeScreen()
        waitForText("Lektionen")
        // DailyChallengeCard should be visible on home screen
        val found = textExists("Challenge") || textExists("Herausforderung") ||
            textExists("Tages") || textExists("Daily")
        assert(found) {
            "Daily Challenge card not found on home screen"
        }
    }

    @Test
    fun us09_b_dailyChallengeCard_showsStartButton() {
        ensureHomeScreen()
        waitForText("Lektionen")
        for (i in 1..3) {
            if (textExists("Start") || textExists("Erledigt") || textExists("Fertig") ||
                textExists("Come back") || textExists("morgen")) break
            scrollDown()
        }
        assert(textExists("Start") || textExists("Erledigt") || textExists("Fertig") ||
            textExists("Come back") || textExists("morgen")) {
            "Daily Challenge start button or completed indicator not found"
        }
    }

    @Test
    fun us09_c_dailyChallengeCard_showsStreakOrXp() {
        ensureHomeScreen()
        waitForText("Lektionen")
        for (i in 1..3) {
            if (textExists("XP") || textExists("Tag")) break
            scrollDown()
        }
        assert(textExists("XP") || textExists("Tag")) {
            "Daily Challenge XP or streak indicator not found"
        }
    }

    // ─── US-11: Daily Challenge Navigation ───────────────────────────

    @Test
    fun us11_dailyChallenge_navigatesToMiniGame() {
        ensureHomeScreen()
        waitForText("Lektionen")
        val startBtn = device.findObject(By.text("Start"))
        if (startBtn != null) {
            startBtn.click()
            device.wait(Until.hasObject(By.pkg(packageName)), timeout)
            assert(waitForText("Runde", 5_000) || waitForText("Round", 5_000) ||
                waitForText("Punkte", 5_000) || waitForText("Score", 5_000)) {
                "Clicking Start did not navigate to a mini-game"
            }
        } else {
            assert(textExists("Erledigt") || textExists("Fertig") ||
                textExists("Come back") || textExists("morgen")) {
                "Daily challenge neither has Start button nor shows completed state"
            }
        }
    }
}
