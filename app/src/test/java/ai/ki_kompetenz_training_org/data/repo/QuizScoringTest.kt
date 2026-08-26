package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.KiScoreQuestionDto
import ai.ki_kompetenz_training_org.data.api.KiScoreTierDto
import org.junit.Assert.*
import org.junit.Test

class QuizScoringTest {

    @Test
    fun `scoreFor empty answers returns 0`() {
        assertEquals(0, QuizScoring.scoreFor(emptyList()))
    }

    @Test
    fun `scoreFor all correct returns 100`() {
        assertEquals(100, QuizScoring.scoreFor(listOf(true, true, true, true, true, true, true, true, true, true)))
    }

    @Test
    fun `scoreFor half correct returns 50`() {
        val answers = listOf(true, true, true, true, true, false, false, false, false, false)
        assertEquals(50, QuizScoring.scoreFor(answers))
    }

    @Test
    fun `scoreFor all wrong returns 0`() {
        assertEquals(0, QuizScoring.scoreFor(listOf(false, false, false, false, false)))
    }

    @Test
    fun `scoreFor single correct returns 100`() {
        assertEquals(100, QuizScoring.scoreFor(listOf(true)))
    }

    @Test
    fun `scoreFor single wrong returns 0`() {
        assertEquals(0, QuizScoring.scoreFor(listOf(false)))
    }

    @Test
    fun `scoreFor rounds correctly for 7 of 10`() {
        assertEquals(70, QuizScoring.scoreFor(listOf(true, true, true, true, true, true, true, false, false, false)))
    }

    private val tiers = listOf(
        KiScoreTierDto(min = 0, max = 30, title = "Anfaenger", emoji = "world", description = "test"),
        KiScoreTierDto(min = 31, max = 60, title = "Fortgeschritten", emoji = "muscle", description = "test"),
        KiScoreTierDto(min = 61, max = 100, title = "Experte", emoji = "trophy", description = "test"),
    )

    @Test
    fun `tierFor returns correct tier for low score`() {
        val tier = QuizScoring.tierFor(15, tiers)
        assertEquals("Anfaenger", tier?.title)
    }

    @Test
    fun `tierFor returns correct tier for mid score`() {
        val tier = QuizScoring.tierFor(50, tiers)
        assertEquals("Fortgeschritten", tier?.title)
    }
    @Test
    fun `tierFor returns correct tier for high score`() {
        val tier = QuizScoring.tierFor(90, tiers)
        assertEquals("Experte", tier?.title)
    }

    @Test
    fun `tierFor returns null for empty tiers`() {
        assertNull(QuizScoring.tierFor(50, emptyList()))
    }

    @Test
    fun `tierFor returns null if no tier matches`() {
        assertNull(QuizScoring.tierFor(200, tiers))
    }

    @Test
    fun `tierFor handles boundary min`() {
        val tier = QuizScoring.tierFor(31, tiers)
        assertEquals("Fortgeschritten", tier?.title)
    }

    @Test
    fun `tierFor handles boundary max`() {
        val tier = QuizScoring.tierFor(60, tiers)
        assertEquals("Fortgeschritten", tier?.title)
    }

    @Test
    fun `shuffledOptions returns correct number of options`() {
        val q = KiScoreQuestionDto(id = 1, text = "Q", options = listOf("A", "B", "C", "D"), correct = 0)
        val shuffled = QuizScoring.shuffledOptions(q)
        assertEquals(4, shuffled.size)
    }

    @Test
    fun `shuffledOptions contains all original options`() {
        val q = KiScoreQuestionDto(id = 1, text = "Q", options = listOf("A", "B", "C", "D"), correct = 0)
        val shuffled = QuizScoring.shuffledOptions(q)
        assertEquals(setOf("A", "B", "C", "D"), shuffled.toSet())
    }

    @Test
    fun `shuffledOptions works with 2 options`() {
        val q = KiScoreQuestionDto(id = 1, text = "Q", options = listOf("X", "Y"), correct = 0)
        val shuffled = QuizScoring.shuffledOptions(q)
        assertEquals(2, shuffled.size)
    }
}
