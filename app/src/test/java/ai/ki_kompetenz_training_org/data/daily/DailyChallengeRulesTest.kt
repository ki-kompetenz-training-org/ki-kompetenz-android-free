package ai.ki_kompetenz_training_org.data.daily

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TDD RED phase: Tests for DailyChallengeRules.
 * These tests define the XP calculation contract BEFORE implementation.
 *
 * XP structure:
 * - Base: 20 XP for completing the daily challenge
 * - Perfect bonus: +15 XP if all rounds correct
 * - Streak bonus: +5 per consecutive day, capped at 30
 * - Total max: 65 XP (20 + 15 + 30)
 */
class DailyChallengeRulesTest {

    @Test
    fun `base XP for daily challenge completion is 20`() {
        assertEquals(20, DailyChallengeRules.baseXp)
    }

    @Test
    fun `perfect completion bonus is 15`() {
        assertEquals(15, DailyChallengeRules.perfectBonus)
    }

    @Test
    fun `streak bonus per day is 5`() {
        assertEquals(5, DailyChallengeRules.streakBonusPerDay)
    }

    @Test
    fun `max streak bonus is 30`() {
        assertEquals(30, DailyChallengeRules.maxStreakBonus)
    }

    @Test
    fun `calculate XP for first day non-perfect completion`() {
        // Day 1, not perfect: 20 base + 0 streak = 20
        val xp = DailyChallengeRules.calculateXp(streakDay = 1, perfect = false)
        assertEquals(20, xp)
    }

    @Test
    fun `calculate XP for first day perfect completion`() {
        // Day 1, perfect: 20 base + 15 perfect + 0 streak = 35
        val xp = DailyChallengeRules.calculateXp(streakDay = 1, perfect = true)
        assertEquals(35, xp)
    }

    @Test
    fun `calculate XP for 3-day streak non-perfect`() {
        // Day 3, not perfect: 20 base + 0 + 10 streak (3-1=2 days * 5 = 10) = 30
        // Streak bonus is for consecutive days AFTER the first day
        val xp = DailyChallengeRules.calculateXp(streakDay = 3, perfect = false)
        assertEquals(30, xp)
    }

    @Test
    fun `calculate XP for 3-day streak perfect`() {
        // Day 3, perfect: 20 base + 15 perfect + 10 streak = 45
        val xp = DailyChallengeRules.calculateXp(streakDay = 3, perfect = true)
        assertEquals(45, xp)
    }

    @Test
    fun `calculate XP for 7-day streak perfect`() {
        // Day 7, perfect: 20 base + 15 perfect + 30 streak (capped) = 65
        val xp = DailyChallengeRules.calculateXp(streakDay = 7, perfect = true)
        assertEquals(65, xp)
    }

    @Test
    fun `calculate XP for 10-day streak caps streak bonus`() {
        // Day 10, perfect: 20 base + 15 perfect + 30 (capped, not 45) = 65
        val xp = DailyChallengeRules.calculateXp(streakDay = 10, perfect = true)
        assertEquals(65, xp)
    }

    @Test
    fun `calculate XP for 10-day streak non-perfect caps streak bonus`() {
        // Day 10, not perfect: 20 base + 0 + 30 (capped) = 50
        val xp = DailyChallengeRules.calculateXp(streakDay = 10, perfect = false)
        assertEquals(50, xp)
    }

    @Test
    fun `streak bonus for day 1 is 0`() {
        // First day: no streak bonus (streak just started)
        val bonus = DailyChallengeRules.streakBonus(streakDay = 1)
        assertEquals(0, bonus)
    }

    @Test
    fun `streak bonus for day 2 is 5`() {
        val bonus = DailyChallengeRules.streakBonus(streakDay = 2)
        assertEquals(5, bonus)
    }

    @Test
    fun `streak bonus for day 7 is 30 (capped)`() {
        // 6 days * 5 = 30, capped at 30
        val bonus = DailyChallengeRules.streakBonus(streakDay = 7)
        assertEquals(30, bonus)
    }

    @Test
    fun `streak bonus for day 10 is 30 (capped)`() {
        // 9 days * 5 = 45, but capped at 30
        val bonus = DailyChallengeRules.streakBonus(streakDay = 10)
        assertEquals(30, bonus)
    }

    @Test
    fun `total max XP is 65`() {
        // 20 base + 15 perfect + 30 streak = 65
        val max = DailyChallengeRules.calculateXp(streakDay = 10, perfect = true)
        assertEquals(65, max)
    }
}
