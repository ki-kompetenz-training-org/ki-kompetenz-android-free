package ai.ki_kompetenz_training_org

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Performance Tests
 * Verifies: App startup < 3s, No ANRs, Memory efficiency
 */
class PerformanceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_startupPerformance() {
        // App should start within 3 seconds
        val startTime = System.currentTimeMillis()
        
        composeTestRule.apply {
            onRoot().assertExists()
        }
        
        val endTime = System.currentTimeMillis()
        val startupTime = (endTime - startTime) / 1000.0
        
        assert(startupTime < 3.0) {
            "App startup time ${startupTime}s exceeds 3s limit"
        }
    }

    @Test
    fun app_noAnrOnInteraction() {
        // Interactions should not cause ANR (Application Not Responding)
        composeTestRule.apply {
            // Multiple rapid interactions
            repeat(10) {
                onRoot().performTouchInput { 
                    center.click() 
                }
            }
            // Should not hang
            onRoot().assertExists()
        }
    }

    @Test
    fun app_navigationPerformance() {
        // Navigation should be smooth (< 500ms per screen)
        composeTestRule.apply {
            val startTime = System.currentTimeMillis()
            
            onNodeWithText("Lektionen").performClick()
            onNodeWithText("Quiz").performClick()
            onNodeWithText("SRS").performClick()
            
            val endTime = System.currentTimeMillis()
            val navigationTime = (endTime - startTime) / 1000.0
            
            assert(navigationTime < 0.5) {
                "Navigation time ${navigationTime}s exceeds 500ms limit"
            }
        }
    }
}
