package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import org.junit.Assert.*
import org.junit.Test

class GamificationRulesExtendedTest {

    @Test
    fun `xpPerCorrectQuizAnswer is 10`() {
        assertEquals(10, GamificationRules.xpPerCorrectQuizAnswer)
    }

    @Test
    fun `perfectQuizBonus is 50`() {
        assertEquals(50, GamificationRules.perfectQuizBonus)
    }

    @Test
    fun `xpPerPremiumCorrectAnswer is 15`() {
        assertEquals(15, GamificationRules.xpPerPremiumCorrectAnswer)
    }

    @Test
    fun `premiumPerfectBonus is 75`() {
        assertEquals(75, GamificationRules.premiumPerfectBonus)
    }

    @Test
    fun `xpPerCompletedLesson is 25`() {
        assertEquals(25, GamificationRules.xpPerCompletedLesson)
    }

    @Test
    fun `xpPerTeamJoin is 20`() {
        assertEquals(20, GamificationRules.xpPerTeamJoin)
    }

    @Test
    fun `xpPerMiniGameWin is 10`() {
        assertEquals(10, GamificationRules.xpPerMiniGameWin)
    }

    @Test
    fun `xpPerSrsReview is 5`() {
        assertEquals(5, GamificationRules.xpPerSrsReview)
    }

    @Test
    fun `srsSessionBonus is 20`() {
        assertEquals(20, GamificationRules.srsSessionBonus)
    }

    @Test
    fun `quizXp with 0 correct returns 0`() {
        assertEquals(0, GamificationRules.quizXp(0, 10))
    }

    @Test
    fun `quizXp with all correct and premium gives 225`() {
        // 10 * 15 (premium per correct) + 75 (premium perfect bonus) = 225
        assertEquals(225, GamificationRules.quizXp(10, 10, premium = true))
    }

    @Test
    fun `quizXp premium gives more than non-premium`() {
        val normal = GamificationRules.quizXp(5, 10)
        val premium = GamificationRules.quizXp(5, 10, premium = true)
        assertTrue("Premium should give more XP", premium > normal)
    }

    @Test
    fun `miniGameXp beginner perfect gives 40`() {
        assertEquals(40, GamificationRules.miniGameXp(10, 10, "BEGINNER"))
    }

    @Test
    fun `miniGameXp intermediate perfect gives 45`() {
        assertEquals(45, GamificationRules.miniGameXp(10, 10, "INTERMEDIATE"))
    }

    @Test
    fun `miniGameXp expert perfect gives 50`() {
        assertEquals(50, GamificationRules.miniGameXp(10, 10, "EXPERT"))
    }
    @Test
    fun `miniGameXp unknown difficulty uses default 10`() {
        val xp = GamificationRules.miniGameXp(5, 10, "UNKNOWN")
        assertEquals(5, xp)
    }

    @Test
    fun `miniGameXp empty difficulty uses default`() {
        val xp = GamificationRules.miniGameXp(5, 10, "")
        assertEquals(5, xp)
    }

    @Test
    fun `miniGameXp with 0 total does not divide by zero`() {
        // 0/1 = 0 ratio, base = 0, but 0 == 0 so perfect bonus = 25
        val xp = GamificationRules.miniGameXp(0, 0, "BEGINNER")
        assertEquals(25, xp)
    }

    @Test
    fun `nextStreak returns 1 for null lastDay`() {
        assertEquals(1, GamificationRules.nextStreak(null))
    }

    @Test
    fun `nextStreak returns 0 for yesterday`() {
        val today = java.time.LocalDate.of(2026, 1, 15)
        val yesterday = today.minusDays(1).toString()
        assertEquals(0, GamificationRules.nextStreak(yesterday, today))
    }

    @Test
    fun `nextStreak returns -1 for today`() {
        val today = java.time.LocalDate.of(2026, 1, 15)
        assertEquals(-1, GamificationRules.nextStreak(today.toString(), today))
    }

    @Test
    fun `nextStreak returns 1 for old date`() {
        val today = java.time.LocalDate.of(2026, 1, 15)
        val oldDate = today.minusDays(10).toString()
        assertEquals(1, GamificationRules.nextStreak(oldDate, today))
    }

    @Test
    fun `checkInXp returns 5 for streak 1`() {
        assertEquals(5, GamificationRules.checkInXp(1))
    }

    @Test
    fun `checkInXp returns 25 for streak 5`() {
        assertEquals(25, GamificationRules.checkInXp(5))
    }

    @Test
    fun `checkInXp returns 30 for streak 6 and above`() {
        assertEquals(30, GamificationRules.checkInXp(6))
        assertEquals(30, GamificationRules.checkInXp(10))
        assertEquals(30, GamificationRules.checkInXp(100))
    }

    @Test
    fun `xpForLevel is 0 for level 1`() {
        assertEquals(0, GamificationRules.xpForLevel(1))
    }

    @Test
    fun `xpForLevel formula is 50 times n times n minus 1`() {
        assertEquals(0, GamificationRules.xpForLevel(1))
        assertEquals(100, GamificationRules.xpForLevel(2))
        assertEquals(300, GamificationRules.xpForLevel(3))
        assertEquals(600, GamificationRules.xpForLevel(4))
        assertEquals(1000, GamificationRules.xpForLevel(5))
        assertEquals(1500, GamificationRules.xpForLevel(6))
    }
}
