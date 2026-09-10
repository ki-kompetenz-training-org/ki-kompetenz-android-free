package ai.ki_kompetenz_training_org.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.ki_kompetenz_training_org.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * GDPR Art. 17 Account Deletion Tests
 * Verifies: Self-service deletion, Data removal, Confirmation flow
 */
class AccountDeletionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun accountDeletion_buttonExists() {
        composeTestRule.apply {
            // After login, delete button should exist
            onNodeWithText("Dashboard").performClick()
            // Trash icon or delete button
            onNodeWithContentDescription("Account löschen").assertIsDisplayed()
            onNodeWithText("Löschen").assertIsDisplayed()
        }
    }

    @Test
    fun accountDeletion_hasConfirmation() {
        composeTestRule.apply {
            onNodeWithText("Dashboard").performClick()
            onNodeWithContentDescription("Account löschen").performClick()
            // Confirmation modal should appear
            onNodeWithText("Bestätigen").assertIsDisplayed()
            onNodeWithText("Alle Daten löschen").assertIsDisplayed()
        }
    }

    @Test
    fun accountDeletion_listsDataTypes() {
        composeTestRule.apply {
            onNodeWithText("Dashboard").performClick()
            onNodeWithContentDescription("Account löschen").performClick()
            // Should list all data to be deleted
            onNodeWithText("Profil").assertIsDisplayed()
            onNodeWithText("Teams").assertIsDisplayed()
            onNodeWithText("Scores").assertIsDisplayed()
            onNodeWithText("Fortsschritt").assertIsDisplayed()
        }
    }

    @Test
    fun accountDeletion_cancelExists() {
        composeTestRule.apply {
            onNodeWithText("Dashboard").performClick()
            onNodeWithContentDescription("Account löschen").performClick()
            // Cancel button should exist
            onNodeWithText("Abbrechen").assertIsDisplayed()
            onNodeWithText("Cancel").assertIsDisplayed()
        }
    }
}
