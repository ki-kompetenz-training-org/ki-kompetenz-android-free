package ai.ki_kompetenz_training_org.data.daily

import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TDD RED phase: Tests for DailyChallengeSelector.
 * These tests define the contract BEFORE implementation.
 *
 * The selector MUST be:
 * - Deterministic: same date + same games = same result
 * - Rotating: 8 consecutive days cycle through all 8 games
 * - Null-safe: empty game list returns null
 */
class DailyChallengeSelectorTest {

    @Test
    fun `same date produces same challenge`() {
        val date = LocalDate.of(2026, 8, 25)
        val games = MiniGames.ALL

        val result1 = DailyChallengeSelector.select(date, games)
        val result2 = DailyChallengeSelector.select(date, games)

        assertEquals("Same date must produce same game", result1, result2)
    }

    @Test
    fun `different dates produce different challenges`() {
        val date1 = LocalDate.of(2026, 8, 25)
        val date2 = LocalDate.of(2026, 8, 26)
        val games = MiniGames.ALL

        val result1 = DailyChallengeSelector.select(date1, games)
        val result2 = DailyChallengeSelector.select(date2, games)

        assertNotNull(result1)
        assertNotNull(result2)
        assertTrue(
            "Consecutive days should usually produce different games (got ${result1!!.id} and ${result2!!.id})",
            result1.id != result2.id
        )
    }

    @Test
    fun `all games are cycled through over 8 days`() {
        val games = MiniGames.ALL
        val startDate = LocalDate.of(2026, 8, 25)
        val selectedIds = (0 until games.size).map { dayOffset ->
            DailyChallengeSelector.select(startDate.plusDays(dayOffset.toLong()), games)!!.id
        }.toSet()

        assertEquals(
            "Over ${games.size} days, all ${games.size} games should be selected",
            games.size,
            selectedIds.size
        )
    }

    @Test
    fun `empty game list returns null`() {
        val date = LocalDate.of(2026, 8, 25)
        val result = DailyChallengeSelector.select(date, emptyList())
        assertNull("Empty game list should return null", result)
    }

    @Test
    fun `single game always selected`() {
        val date = LocalDate.of(2026, 8, 25)
        val singleGame = listOf(MiniGames.ALL.first())
        val result = DailyChallengeSelector.select(date, singleGame)
        assertNotNull(result)
        assertEquals(singleGame.first().id, result!!.id)
    }

    @Test
    fun `selection is deterministic across calls`() {
        val date = LocalDate.of(2026, 1, 1)
        val games = MiniGames.ALL

        // Call 100 times — must always return the same result
        val first = DailyChallengeSelector.select(date, games)
        repeat(100) {
            assertEquals(first, DailyChallengeSelector.select(date, games))
        }
    }

    @Test
    fun `date wraps around game list size`() {
        val games = MiniGames.ALL
        val day1 = LocalDate.of(2026, 8, 25)
        val day9 = day1.plusDays(games.size.toLong()) // 8 days later = same game

        val result1 = DailyChallengeSelector.select(day1, games)
        val result9 = DailyChallengeSelector.select(day9, games)

        assertEquals(
            "Day ${games.size} later should wrap to same game",
            result1,
            result9
        )
    }
}
