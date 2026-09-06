package ai.ki_kompetenz_training_org.ui.daily

import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.ui.theme.KiKompetenzTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for DailyChallengeCard (openspec daily-challenge / task 5.1):
 * - displays today's challenge (title, description, streak, xp)
 * - completed state hides the start button
 * - start button triggers navigation callback
 *
 * Pure composable test: the card takes plain parameters, no app state needed.
 */
class DailyChallengeCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val startText = context.getString(R.string.daily_challenge_start)

    private val game: MiniGame = MiniGames.ALL.first { !it.premium }

    private fun setContent(isCompleted: Boolean = false, onStart: () -> Unit = {}) {
        composeTestRule.setContent {
            KiKompetenzTheme {
                DailyChallengeCard(
                    challenge = game,
                    isCompleted = isCompleted,
                    streak = 3,
                    xpPreview = 20,
                    onStart = onStart,
                )
            }
        }
    }

    @Test
    fun card_displays_challenge_title_description_streak_xp() {
        setContent()
        composeTestRule.onNodeWithText(game.title("de")).assertIsDisplayed()
        composeTestRule.onNodeWithText(game.description("de")).assertIsDisplayed()
        composeTestRule.onNodeWithText("\uD83D\uDD25 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("+20 XP").assertIsDisplayed()
        composeTestRule.onNodeWithText(startText).assertIsDisplayed()
    }

    @Test
    fun card_completed_hides_start_button_shows_checkmark() {
        setContent(isCompleted = true)
        composeTestRule.onNodeWithText(startText).assertDoesNotExist()
        composeTestRule.onNodeWithText("\u2713 \u2714").assertIsDisplayed()
    }

    @Test
    fun card_start_button_invokes_navigation_callback() {
        var clicked = false
        setContent(onStart = { clicked = true })
        composeTestRule.onNodeWithText(startText).performClick()
        assertTrue("onStart callback must fire on start button click", clicked)
    }
}
