package ai.ki_kompetenz_training_org.data.daily

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD RED phase: Tests for DailyChallengeState.
 * These tests define the completion/streak logic contract BEFORE implementation.
 *
 * DailyChallengeState is a pure object with no side effects:
 * - isCompleted: checks if lastCompletedDate matches today
 * - calculateStreak: computes new streak based on lastCompletedDate and today
 * - formatDate: converts LocalDate to ISO string
 */
class DailyChallengeStateTest {

    @Test
    fun `isCompleted returns false when lastCompletedDate is null`() {
        assertFalse(DailyChallengeState.isCompleted(null, LocalDate.of(2026, 8, 25)))
    }

    @Test
    fun `isCompleted returns true when lastCompletedDate matches today`() {
        val today = LocalDate.of(2026, 8, 25)
        assertTrue(DailyChallengeState.isCompleted("2026-08-25", today))
    }

    @Test
    fun `isCompleted returns false when lastCompletedDate is different`() {
        val today = LocalDate.of(2026, 8, 25)
        assertFalse(DailyChallengeState.isCompleted("2026-08-24", today))
    }

    @Test
    fun `isCompleted returns false when lastCompletedDate is future`() {
        val today = LocalDate.of(2026, 8, 25)
        assertFalse(DailyChallengeState.isCompleted("2026-08-26", today))
    }

    @Test
    fun `calculateStreak returns 1 for first ever completion`() {
        // No previous completion → streak starts at 1
        val streak = DailyChallengeState.calculateStreak(null, LocalDate.of(2026, 8, 25))
        assertEquals(1, streak)
    }

    @Test
    fun `calculateStreak returns 2 for consecutive day`() {
        // Completed yesterday, completing today → streak = 2
        val streak = DailyChallengeState.calculateStreak("2026-08-24", LocalDate.of(2026, 8, 25))
        assertEquals(2, streak)
    }

    @Test
    fun `calculateStreak returns 1 after gap`() {
        // Completed 3 days ago, completing today → streak resets to 1
        val streak = DailyChallengeState.calculateStreak("2026-08-22", LocalDate.of(2026, 8, 25))
        assertEquals(1, streak)
    }

    @Test
    fun `calculateStreak returns 0 when already completed today`() {
        // Already completed today → 0 (no new streak)
        val streak = DailyChallengeState.calculateStreak("2026-08-25", LocalDate.of(2026, 8, 25))
        assertEquals(0, streak)
    }

    @Test
    fun `calculateStreak returns 3 for two-day streak`() {
        // Completed 2 days ago, then yesterday, now today → streak = 3
        // But we only store the LAST completion date, so: last = yesterday → streak = 2
        val streak = DailyChallengeState.calculateStreak("2026-08-24", LocalDate.of(2026, 8, 25))
        assertEquals(2, streak)
    }

    @Test
    fun `formatDate produces ISO format`() {
        val date = LocalDate.of(2026, 8, 25)
        assertEquals("2026-08-25", DailyChallengeState.formatDate(date))
    }

    @Test
    fun `formatDate handles single digit months and days`() {
        val date = LocalDate.of(2026, 1, 5)
        assertEquals("2026-01-05", DailyChallengeState.formatDate(date))
    }

    @Test
    fun `parseDate handles ISO format string`() {
        val date = DailyChallengeState.parseDate("2026-08-25")
        assertEquals(LocalDate.of(2026, 8, 25), date)
    }

    @Test
    fun `parseDate returns null for invalid string`() {
        val date = DailyChallengeState.parseDate("not-a-date")
        assertEquals(null, date)
    }

    @Test
    fun `parseDate returns null for empty string`() {
        val date = DailyChallengeState.parseDate("")
        assertEquals(null, date)
    }
}
