package ai.ki_kompetenz_training_org.ui.quiz

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for QuizConstants: combo multipliers, points calculation, max score.
 */
class QuizConstantsTest {

    @Test
    fun `combo multiplier is 1 for combo 0`() {
        assertEquals(1.0, QuizConstants.comboMultiplier(0), 0.001)
    }

    @Test
    fun `combo multiplier is 1 for combo 1`() {
        assertEquals(1.0, QuizConstants.comboMultiplier(1), 0.001)
    }

    @Test
    fun `combo multiplier is 1_5 for combo 2`() {
        assertEquals(1.5, QuizConstants.comboMultiplier(2), 0.001)
    }

    @Test
    fun `combo multiplier is 1_5 for combo 3`() {
        assertEquals(1.5, QuizConstants.comboMultiplier(3), 0.001)
    }

    @Test
    fun `combo multiplier is 2 for combo 4`() {
        assertEquals(2.0, QuizConstants.comboMultiplier(4), 0.001)
    }

    @Test
    fun `combo multiplier is 2 for combo 10`() {
        assertEquals(2.0, QuizConstants.comboMultiplier(10), 0.001)
    }

    @Test
    fun `points for correct answer with no combo and no time`() {
        assertEquals(100, QuizConstants.pointsForCorrectAnswer(0, 0))
    }

    @Test
    fun `points for correct answer with full time and no combo`() {
        assertEquals(300, QuizConstants.pointsForCorrectAnswer(20, 0))
    }

    @Test
    fun `points for correct answer with full time and combo 2`() {
        assertEquals(450, QuizConstants.pointsForCorrectAnswer(20, 2))
    }

    @Test
    fun `points for correct answer with full time and combo 4`() {
        assertEquals(600, QuizConstants.pointsForCorrectAnswer(20, 4))
    }

    @Test
    fun `max score is 6000`() {
        assertEquals(6000, QuizConstants.MAX_SCORE)
    }

    @Test
    fun `round seconds is 20`() {
        assertEquals(20, QuizConstants.ROUND_SECONDS)
    }

    @Test
    fun `max lives is 3`() {
        assertEquals(3, QuizConstants.MAX_LIVES)
    }

    @Test
    fun `timer tick is 1000ms`() {
        assertEquals(1000L, QuizConstants.TIMER_TICK_MS)
    }

    @Test
    fun `points increase with time left`() {
        val p0 = QuizConstants.pointsForCorrectAnswer(0, 0)
        val p10 = QuizConstants.pointsForCorrectAnswer(10, 0)
        val p20 = QuizConstants.pointsForCorrectAnswer(20, 0)
        assertTrue("More time should give more points", p20 > p10 && p10 > p0)
    }

    @Test
    fun `points increase with combo`() {
        val p0 = QuizConstants.pointsForCorrectAnswer(20, 0)
        val p2 = QuizConstants.pointsForCorrectAnswer(20, 2)
        val p4 = QuizConstants.pointsForCorrectAnswer(20, 4)
        assertTrue("Higher combo should give more points", p4 > p2 && p2 > p0)
    }
}
