package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.repo.Badges
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Property-Based Tests using JUnit 5 and manual test cases.
 * Tests invariants and edge cases without Kotest complexity.
 */
class PropertyBasedTests {

    private lateinit var premiumRepository: PremiumRepository

    @Before
    fun setup() {
        val apiService = mockk<ai.ki_kompetenz_training_org.data.api.ApiService>()
        premiumRepository = PremiumRepository(apiService)
    }

    // ==================== PremiumRepository Tests ====================

    @Test
    fun lessons_1_to_8_areFree() {
        (1..8).forEach { lessonNumber ->
            assertFalse("Lesson $lessonNumber should be free", premiumRepository.isPremiumLesson(lessonNumber))
        }
    }

    @Test
    fun lessons_9_andAbove_arePremium() {
        (9..20).forEach { lessonNumber ->
            assertTrue("Lesson $lessonNumber should be premium", premiumRepository.isPremiumLesson(lessonNumber))
        }
    }

    @Test
    fun negativeAndZeroLessonNumbers_areFree() {
        listOf(-5, -1, 0).forEach { lessonNumber ->
            assertFalse("Lesson $lessonNumber should be free", premiumRepository.isPremiumLesson(lessonNumber))
        }
    }

    @Test
    fun nullLessonNumber_isFree() {
        assertFalse("Null lesson should be free", premiumRepository.isPremiumLesson(null))
    }

    // ==================== GamificationRules Tests ====================

    @Test
    fun xpForLevel_producesNonNegativeValues() {
        (1..100).forEach { level ->
            val xp = GamificationRules.xpForLevel(level)
            assertNotNull("XP for level $level should not be null", xp)
            assertTrue("XP for level $level should be >= 0", xp >= 0)
        }
    }

    @Test
    fun xpForLevel_isMonotonicallyIncreasing() {
        var prevXp = GamificationRules.xpForLevel(1)
        for (level in 2..50) {
            val currentXp = GamificationRules.xpForLevel(level)
            assertTrue("XP should increase from level ${level-1} to $level", currentXp > prevXp)
            prevXp = currentXp
        }
    }

    @Test
    fun levelForXp_and_xpForLevel_areInverseFunctions() {
        (1..50).forEach { level ->
            val xp = GamificationRules.xpForLevel(level)
            val calculatedLevel = GamificationRules.levelForXp(xp)
            assertEquals("levelForXp should return original level", level, calculatedLevel)
        }
    }

    @Test
    fun levelForXp_returnsValidLevel() {
        (0..10000 step 100).forEach { xp ->
            val level = GamificationRules.levelForXp(xp)
            val xpForLevel = GamificationRules.xpForLevel(level)
            val xpForNextLevel = GamificationRules.xpForLevel(level + 1)
            
            assertTrue("XP should be >= xpForLevel", xp >= xpForLevel)
            if (level < 100) {
                assertTrue("XP should be < xpForNextLevel", xp < xpForNextLevel)
            }
        }
    }

    @Test
    fun xpIntoLevel_isAlwaysBetween0AndXpNeededForNextLevel() {
        (1..5000 step 500).forEach { xp ->
            val xpIntoCurrentLevel = GamificationRules.xpIntoLevel(xp)
            val xpNeededForNext = GamificationRules.xpNeededForNextLevel(xp)
            
            assertTrue("xpIntoLevel should be >= 0", xpIntoCurrentLevel >= 0)
            assertTrue("xpIntoLevel should be < xpNeededForNext", xpIntoCurrentLevel < xpNeededForNext)
        }
    }

    // ==================== Quiz Scoring Tests ====================

    @Test
    fun quizXp_isNonNegativeForValidInputs() {
        (0..10).forEach { correct ->
            (1..20).forEach { total ->
                if (correct <= total) {
                    val xp = GamificationRules.quizXp(correct, total)
                    assertTrue("Quiz XP should be >= 0 for $correct/$total", xp >= 0)
                }
            }
        }
    }

    @Test
    fun quizXp_isMaximizedForPerfectScore() {
        (1..20).forEach { total ->
            val perfectScore = GamificationRules.quizXp(total, total)
            val maxPossible = total * 10 + 50 // 10 points per question + 50 perfect bonus
            assertEquals("Perfect score should match max possible", maxPossible, perfectScore)
        }
    }

    @Test
    fun quizXp_with0CorrectAnswers_is0() {
        (1..20).forEach { total ->
            assertEquals("Quiz with 0 correct should give 0 XP", 0, GamificationRules.quizXp(0, total))
        }
    }

    // ==================== Streak Logic Tests ====================

    @Test
    fun streakXp_isNonDecreasing() {
        var prevXp = GamificationRules.checkInXp(1)
        for (streak in 2..30) {
            val currentXp = GamificationRules.checkInXp(streak)
            assertTrue("Streak XP should not decrease from $streak to ${streak+1}", currentXp >= prevXp)
            prevXp = currentXp
        }
    }

    @Test
    fun streakXp_capsAt30() {
        (30..100).forEach { streak ->
            val xp = GamificationRules.checkInXp(streak)
            assertTrue("Streak XP should cap at 30 for streak $streak", xp <= 30)
        }
    }

    @Test
    fun streakXp_isExactly30ForStreak30() {
        assertEquals("Streak 30 should give exactly 30 XP", 30, GamificationRules.checkInXp(30))
    }

    // ==================== Badge Tests ====================

    @Test
    fun allBadges_haveUniqueIds() {
        val badges = Badges.all()
        val ids = badges.map { it.id }
        assertEquals("All badge IDs should be unique", ids.size, ids.distinct().size)
    }

    @Test
    fun allBadges_haveNonEmptyFields() {
        val badges = Badges.all()
        badges.forEach { badge ->
            assertTrue("Badge ${badge.id} should have non-empty emoji", badge.emoji.isNotBlank())
            assertTrue("Badge ${badge.id} should have non-empty title", badge.title.isNotBlank())
            assertTrue("Badge ${badge.id} should have non-empty description", badge.description.isNotBlank())
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun veryHighXp_returnsProportionallyHighLevel() {
        val veryHighXp = 1000000
        val level = GamificationRules.levelForXp(veryHighXp)
        // Formula: 50 * n * (n-1) <= xp → n ~ sqrt(xp/50)
        // 50 * 141 * 140 = 987,000 → level ~141
        assertTrue("Very high XP ($veryHighXp) should return a high level", level > 100)
        assertTrue("Level should be reasonable", level < 200)
    }

    @Test
    fun quizXp_withEqualCorrectAndTotal_isPerfect() {
        (1..10).forEach { n ->
            val xp = GamificationRules.quizXp(n, n)
            val expected = n * 10 + 50
            assertEquals("Quiz with $n/$n correct should give $expected XP", expected, xp)
        }
    }

    @Test
    fun quizXp_withHalfCorrect_isBelowPassing() {
        // Formula: correct * 10 (no perfect bonus since not all correct)
        // 50% correct gives 50% of max XP, which is below 60% threshold
        (2..20 step 2).forEach { total ->
            val half = total / 2
            val xp = GamificationRules.quizXp(half, total)
            val maxPossible = total * 10
            val passingScore = maxPossible * 0.6
            // Half correct should always be below 60%
            assertTrue("Half correct ($half/$total) gives $xp XP, should be below passing ($passingScore)", xp < passingScore)
        }
    }
}
