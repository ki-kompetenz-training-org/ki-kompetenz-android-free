package ai.ki_kompetenz_training_org.ui.srs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.ki_kompetenz_training_org.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * SRS (Spaced Repetition System) Integration Tests
 * Verifies: Card reviews, scheduling, progress tracking
 */
class SrsIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun srs_displaysCards() {
        composeTestRule.apply {
            onNodeWithText("SRS").performClick()
            // Should display SRS cards
            onNodeWithText("Karte").assertIsDisplayed()
        }
    }

    @Test
    fun srs_hasReviewButtons() {
        composeTestRule.apply {
            onNodeWithText("SRS").performClick()
            // Review buttons should exist
            onNodeWithText("Wiederholen").assertIsDisplayed()
            onNodeWithText("Review").assertIsDisplayed()
        }
    }

    @Test
    fun srs_showsProgress() {
        composeTestRule.apply {
            onNodeWithText("SRS").performClick()
            // Progress indicator should be visible
            onNodeWithText("Fortschritt").assertIsDisplayed()
        }
    }

    @Test
    fun srs_cardsAreScheduled() {
        composeTestRule.apply {
            onNodeWithText("SRS").performClick()
            // Cards should show scheduling info
            onNodeWithText("Nächste").assertIsDisplayed()
            onNodeWithText("Wiederholung").assertIsDisplayed()
        }
    }
}
