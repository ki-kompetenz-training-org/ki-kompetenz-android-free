package ai.ki_kompetenz_training_org.data.daily

import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Integration tests for Daily Challenge: Selector + State + Rules working together.
 * Tests the end-to-end flow without Android dependencies.
 */
class DailyChallengeIntegrationTest {

    private val games = MiniGames.ALL

    // ── End-to-end: select -> complete -> verify ──

    @Test
    fun `select then complete then verify state for first time`() {
        val today = LocalDate.of(2026, 1, 15)
        val game = DailyChallengeSelector.select(today, games)
        assertNotNull("Should select a game", game)

        // First time: no last date -> streak = 1, XP = 20 (no perfect, no streak bonus)
        val streak = DailyChallengeState.calculateStreak(null, today)
        assertEquals(1, streak)
        val xp = DailyChallengeRules.calculateXp(streakDay = 1, perfect = false)
        assertEquals(20, xp)
    }

    @Test
    fun `select then complete with perfect on first day`() {
        val today = LocalDate.of(2026, 1, 15)
        val game = DailyChallengeSelector.select(today, games)
        assertNotNull(game)

        val streak = DailyChallengeState.calculateStreak(null, today)
        val xp = DailyChallengeRules.calculateXp(streakDay = streak, perfect = true)
        assertEquals("First day perfect: 20 + 15 + 0 = 35", 35, xp)
    }

    @Test
    fun `select then complete on consecutive day 2`() {
        val yesterday = LocalDate.of(2026, 1, 14)
        val today = LocalDate.of(2026, 1, 15)

        val streak = DailyChallengeState.calculateStreak(yesterday.toString(), today)
        assertEquals(2, streak)
        val xp = DailyChallengeRules.calculateXp(streakDay = 2, perfect = false)
        assertEquals("Day 2: 20 + 0 + 5 = 25", 25, xp)
    }

    @Test
    fun `select then complete on consecutive day 3 with perfect`() {
        val twoDaysAgo = LocalDate.of(2026, 1, 13)
        val today = LocalDate.of(2026, 1, 15)

        val streak = DailyChallengeState.calculateStreak(twoDaysAgo.toString(), today)
        // Gap detected (not yesterday), so streak resets to 1
        assertEquals(1, streak)
    }

    @Test
    fun `same date selection is deterministic across calls`() {
        val today = LocalDate.of(2026, 3, 20)
        val game1 = DailyChallengeSelector.select(today, games)
        val game2 = DailyChallengeSelector.select(today, games)
        assertEquals("Same date should produce same game", game1, game2)
    }

    @Test
    fun `8 consecutive days cycle through all 8 games`() {
        // 8 games -> 8-day rotation cycle
        val baseDate = LocalDate.of(2026, 1, 1)
        val selectedGames = (0 until 8).map { day ->
            DailyChallengeSelector.select(baseDate.plusDays(day.toLong()), games)
        }
        val gameIds = selectedGames.map { it!!.id }
        assertEquals("Should cycle through all 8 games", 8, gameIds.toSet().size)
    }

    @Test
    fun `format date and parse date are inverse operations`() {
        val date = LocalDate.of(2026, 7, 15)
        val formatted = DailyChallengeState.formatDate(date)
        val parsed = DailyChallengeState.parseDate(formatted)
        assertEquals("Format then parse should return original date", date, parsed)
    }

    @Test
    fun `isCompleted returns false for null last date`() {
        val today = LocalDate.of(2026, 1, 15)
        assertFalse(DailyChallengeState.isCompleted(null, today))
    }

    @Test
    fun `isCompleted returns true when last date matches today`() {
        val today = LocalDate.of(2026, 1, 15)
        assertTrue(DailyChallengeState.isCompleted(today.toString(), today))
    }

    @Test
    fun `isCompleted returns false when last date is yesterday`() {
        val yesterday = LocalDate.of(2026, 1, 14)
        val today = LocalDate.of(2026, 1, 15)
        assertFalse(DailyChallengeState.isCompleted(yesterday.toString(), today))
    }

    @Test
    fun `parseDate returns null for invalid input`() {
        assertNull(DailyChallengeState.parseDate("not-a-date"))
        assertNull(DailyChallengeState.parseDate(""))
        assertNull(DailyChallengeState.parseDate("2026-13-45"))
    }

    @Test
    fun `streak bonus caps at 30`() {
        // Day 7: (7-1)*5 = 30 -> capped at 30
        assertEquals(30, DailyChallengeRules.streakBonus(7))
        // Day 10: (10-1)*5 = 45 -> capped at 30
        assertEquals(30, DailyChallengeRules.streakBonus(10))
        // Day 100: still capped
        assertEquals(30, DailyChallengeRules.streakBonus(100))
    }

    @Test
    fun `maximum daily challenge XP is 65`() {
        // Base 20 + Perfect 15 + Max streak 30 = 65
        val maxStreakDay = 100 // (100-1)*5 = 495 -> capped at 30
        val maxXP = DailyChallengeRules.calculateXp(streakDay = maxStreakDay, perfect = true)
        assertEquals(65, maxXP)
    }

    @Test
    fun `minimum daily challenge XP is 20`() {
        // Base 20 + no perfect + no streak = 20
        val minXP = DailyChallengeRules.calculateXp(streakDay = 1, perfect = false)
        assertEquals(20, minXP)
    }

    @Test
    fun `selector returns null for empty game list`() {
        val result = DailyChallengeSelector.select(LocalDate.of(2026, 1, 1), emptyList())
        assertNull("Empty game list should return null", result)
    }
}
