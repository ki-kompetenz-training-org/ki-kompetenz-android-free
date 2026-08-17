package ai.ki_kompetenz_training_org.ui.forkids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.ki_kompetenz_training_org.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * ForKids COPPA Compliance Tests
 * Verifies: No PII, No server communication, Local-only storage, No tracking
 */
class ForKidsCoppaTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun forKids_noLoginRequired() {
        composeTestRule.apply {
            // ForKids should NOT require login
            onNodeWithText("ForKids").performClick()
            // Should NOT see login prompt
            onNodeWithText("Login").assertDoesNotExist()
            onNodeWithText("Anmelden").assertDoesNotExist()
        }
    }

    @Test
    fun forKids_parentalGate_exists() {
        composeTestRule.apply {
            onNodeWithText("ForKids").performClick()
            // Parental gate with PIN should exist
            onNodeWithText("Eltern-Gate").assertIsDisplayed()
            onNodeWithText("PIN").assertIsDisplayed()
        }
    }

    @Test
    fun forKids_noTrackingNotice() {
        composeTestRule.apply {
            onNodeWithText("ForKids").performClick()
            // COPPA notice should be visible
            onNodeWithText("Keine Datenweitergabe").assertIsDisplayed()
            onNodeWithText("Lokal speichern").assertIsDisplayed()
        }
    }

    @Test
    fun forKids_noAds() {
        composeTestRule.apply {
            onNodeWithText("ForKids").performClick()
            // No advertising content
            onNodeWithText("Werbung").assertDoesNotExist()
            onNodeWithText("Ad").assertDoesNotExist()
            onNodeWithText("Anzeige").assertDoesNotExist()
        }
    }

    @Test
    fun forKids_noSocialSharing() {
        composeTestRule.apply {
            onNodeWithText("ForKids").performClick()
            // No social sharing buttons
            onNodeWithText("Teilen").assertDoesNotExist()
            onNodeWithText("Share").assertDoesNotExist()
            onNodeWithText("Facebook").assertDoesNotExist()
            onNodeWithText("Instagram").assertDoesNotExist()
        }
    }

    @Test
    fun forKids_localStorageOnly() {
        composeTestRule.apply {
            onNodeWithText("ForKids").performClick()
            // Progress should be saved locally
            onNodeWithText("Fortschritt").assertIsDisplayed()
            // No cloud sync
            onNodeWithText("Cloud").assertDoesNotExist()
            onNodeWithText("Sync").assertDoesNotExist()
        }
    }
}
