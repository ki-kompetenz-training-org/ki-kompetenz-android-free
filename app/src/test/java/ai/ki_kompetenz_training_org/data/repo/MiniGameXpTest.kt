package ai.ki_kompetenz_training_org.data.repo

import org.junit.Assert.*
import org.junit.Test

class MiniGameXpTest {

    @Test
    fun `beginner XP -  80 percent correct gives expected base`() {
        val xp = GamificationRules.miniGameXp(correctCount = 8, totalQuestions = 10, difficulty = "BEGINNER")
        // base = 15 * 0.8 = 12, no perfect bonus
        assertEquals(12, xp)
    }

    @Test
    fun `beginner XP -  perfect score gives bonus`() {
        val xp = GamificationRules.miniGameXp(correctCount = 10, totalQuestions = 10, difficulty = "BEGINNER")
        // base = 15 * 1.0 = 15, + 25 perfect bonus = 40
        assertEquals(40, xp)
    }

    @Test
    fun `intermediate XP -  perfect score gives higher reward`() {
        val xp = GamificationRules.miniGameXp(correctCount = 10, totalQuestions = 10, difficulty = "INTERMEDIATE")
        // base = 20 * 1.0 = 20, + 25 perfect bonus = 45
        assertEquals(45, xp)
    }

    @Test
    fun `expert XP -  perfect score gives highest reward`() {
        val xp = GamificationRules.miniGameXp(correctCount = 10, totalQuestions = 10, difficulty = "EXPERT")
        // base = 25 * 1.0 = 25, + 25 perfect bonus = 50
        assertEquals(50, xp)
    }

    @Test
    fun `zero correct gives zero XP`() {
        val xp = GamificationRules.miniGameXp(correctCount = 0, totalQuestions = 10, difficulty = "BEGINNER")
        assertEquals(0, xp)
    }

    @Test
    fun `half correct intermediate gives expected XP`() {
        val xp = GamificationRules.miniGameXp(correctCount = 5, totalQuestions = 10, difficulty = "INTERMEDIATE")
        // base = 20 * 0.5 = 10
        assertEquals(10, xp)
    }

    @Test
    fun `expert gives more XP than beginner for same score`() {
        val beginnerXp = GamificationRules.miniGameXp(8, 10, "BEGINNER")
        val expertXp = GamificationRules.miniGameXp(8, 10, "EXPERT")
        assertTrue(expertXp > beginnerXp)
    }

    @Test
    fun `intermediate gives more XP than beginner for same score`() {
        val beginnerXp = GamificationRules.miniGameXp(7, 10, "BEGINNER")
        val intermediateXp = GamificationRules.miniGameXp(7, 10, "INTERMEDIATE")
        assertTrue(intermediateXp > beginnerXp)
    }

    @Test
    fun `unknown difficulty falls back to default XP`() {
        val xp = GamificationRules.miniGameXp(5, 10, "UNKNOWN")
        // Falls back to xpPerMiniGameWin = 10, 10 * 0.5 = 5
        assertEquals(5, xp)
    }

    @Test
    fun `single question correct beginner`() {
        val xp = GamificationRules.miniGameXp(1, 1, "BEGINNER")
        // base = 15 * 1.0 = 15, + 25 perfect bonus = 40
        assertEquals(40, xp)
    }

    @Test
    fun `single question wrong beginner`() {
        val xp = GamificationRules.miniGameXp(0, 1, "BEGINNER")
        assertEquals(0, xp)
    }

    @Test
    fun `XP scales linearly with correct ratio`() {
        val xp3 = GamificationRules.miniGameXp(3, 10, "BEGINNER")
        val xp6 = GamificationRules.miniGameXp(6, 10, "BEGINNER")
        assertTrue("6/10 should give more XP than 3/10", xp6 > xp3)
    }

    @Test
    fun `perfect bonus is constant across difficulties`() {
        val beginnerPerfect = GamificationRules.miniGameXp(10, 10, "BEGINNER")
        val intermediatePerfect = GamificationRules.miniGameXp(10, 10, "INTERMEDIATE")
        val expertPerfect = GamificationRules.miniGameXp(10, 10, "EXPERT")
        // The gap between levels should be consistent
        assertEquals(5, intermediatePerfect - beginnerPerfect) // 20-15=5 base diff
        assertEquals(5, expertPerfect - intermediatePerfect)    // 25-20=5 base diff
    }
}
