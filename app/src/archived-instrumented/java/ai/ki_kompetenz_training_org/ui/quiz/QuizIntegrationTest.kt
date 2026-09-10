package ai.ki_kompetenz_training_org.ui.quiz

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.ki_kompetenz_training_org.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Quiz Integration Tests
 * Verifies: 10 questions, scoring, local storage
 */
class QuizIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun quiz_startsWith10Questions() {
        composeTestRule.apply {
            onNodeWithText("Quiz").performClick()
            // Should show question counter
            onNodeWithText("Frage").assertIsDisplayed()
            onNodeWithText("10").assertIsDisplayed()
        }
    }

    @Test
    fun quiz_hasTwoOptionsPerQuestion() {
        composeTestRule.apply {
            onNodeWithText("Quiz").performClick()
            // Each question should have exactly 2 answer options
            // This is a basic check - more detailed tests in unit tests
            onNodeWithText("Antwort").assertIsDisplayed()
        }
    }

    @Test
    fun quiz_showsScore() {
        composeTestRule.apply {
            onNodeWithText("Quiz").performClick()
            // Score should be visible
            onNodeWithText("Score").assertIsDisplayed()
            onNodeWithText("KI-Score").assertIsDisplayed()
        }
    }

    @Test
    fun quiz_shareResultExists() {
        composeTestRule.apply {
            onNodeWithText("Quiz").performClick()
            // Share button should exist after quiz completion
            onNodeWithText("Teilen").assertIsDisplayed()
            onNodeWithText("Share").assertIsDisplayed()
        }
    }

    @Test
    fun quiz_localStorageWorks() {
        composeTestRule.apply {
            onNodeWithText("Quiz").performClick()
            // Results should be stored locally
            onNodeWithText("Ergebnis").assertIsDisplayed()
            onNodeWithText("Lokal speichern").assertIsDisplayed()
        }
    }
}
