package ai.ki_kompetenz_training_org.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ai.ki_kompetenz_training_org.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Home Screen UI Tests
 * Verifies navigation, ForKids, ForSeniors, and core features
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_displaysAllNavigationOptions() {
        composeTestRule.apply {
            // Check main navigation items exist
            onNodeWithText("Lektionen").assertIsDisplayed()
            onNodeWithText("Quiz").assertIsDisplayed()
            onNodeWithText("SRS").assertIsDisplayed()
            onNodeWithText("Team").assertIsDisplayed()
            onNodeWithText("Gamification").assertIsDisplayed()
            onNodeWithText("Premium").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_forKidsSection_exists() {
        composeTestRule.apply {
            onNodeWithText("ForKids").assertIsDisplayed()
            onNodeWithText("Für Kinder").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_forSeniorsSection_exists() {
        composeTestRule.apply {
            onNodeWithText("ForSeniors").assertIsDisplayed()
            onNodeWithText("Für Senioren").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_navigatesToLessons() {
        composeTestRule.apply {
            onNodeWithText("Lektionen").performClick()
            // Verify navigation occurred
            onNodeWithText("Lektionen").assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_showsLoginButton() {
        composeTestRule.apply {
            onNodeWithText("Login").assertIsDisplayed()
        }
    }
}
