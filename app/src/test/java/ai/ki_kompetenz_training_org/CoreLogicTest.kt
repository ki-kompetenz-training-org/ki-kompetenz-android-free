package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.api.KiScoreQuestionDto
import ai.ki_kompetenz_training_org.data.api.KiScoreTierDto
import ai.ki_kompetenz_training_org.data.repo.QuizScoring
import ai.ki_kompetenz_training_org.ui.lessons.renderMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScoringTest {

    private val tiers = listOf(
        KiScoreTierDto(min = 0, max = 20, title = "KI-Laie", emoji = "🌱", description = ""),
        KiScoreTierDto(min = 21, max = 40, title = "KI-Entdecker", emoji = "🔍", description = ""),
        KiScoreTierDto(min = 41, max = 60, title = "KI-Praktiker", emoji = "⚙️", description = ""),
        KiScoreTierDto(min = 61, max = 80, title = "KI-Profi", emoji = "💡", description = ""),
        KiScoreTierDto(min = 81, max = 100, title = "KI-Visionär", emoji = "🚀", description = ""),
    )

    @Test
    fun `score 0 for no answers`() {
        assertEquals(0, QuizScoring.scoreFor(emptyList()))
    }

    @Test
    fun `score rounds correctly for 10 questions`() {
        // 7 of 10 correct → 70
        assertEquals(70, QuizScoring.scoreFor(List(7) { true } + List(3) { false }))
        // 10 of 10 → 100
        assertEquals(100, QuizScoring.scoreFor(List(10) { true }))
        // 0 of 10 → 0
        assertEquals(0, QuizScoring.scoreFor(List(10) { false }))
    }

    @Test
    fun `tier boundaries map correctly`() {
        assertEquals("KI-Laie", QuizScoring.tierFor(0, tiers)?.title)
        assertEquals("KI-Laie", QuizScoring.tierFor(20, tiers)?.title)
        assertEquals("KI-Entdecker", QuizScoring.tierFor(21, tiers)?.title)
        assertEquals("KI-Praktiker", QuizScoring.tierFor(60, tiers)?.title)
        assertEquals("KI-Profi", QuizScoring.tierFor(80, tiers)?.title)
        assertEquals("KI-Visionär", QuizScoring.tierFor(100, tiers)?.title)
        assertNull(QuizScoring.tierFor(101, tiers))
    }

    @Test
    fun `shuffle keeps all options`() {
        val q = KiScoreQuestionDto(
            id = 1,
            text = "Frage",
            options = listOf("A", "B", "C", "D"),
            correct = 1,
            explanation = "",
            emoji = "🤖",
        )
        val shuffled = QuizScoring.shuffledOptions(q)
        assertEquals(q.options.toSet(), shuffled.toSet())
        assertEquals(q.options.size, shuffled.size)
    }
}

class PremiumGatingTest {

    @Test
    fun `first three lessons are free preview`() {
        val gate = ai.ki_kompetenz_training_org.data.repo.PremiumRepository(
            api = object : ai.ki_kompetenz_training_org.data.api.ApiService {
                override suspend fun getLessons(lang: String?) = ai.ki_kompetenz_training_org.data.api.LessonsResponseDto()
                override suspend fun getLesson(slug: String, lang: String?) = ai.ki_kompetenz_training_org.data.api.LessonDetailDto(slug = slug)
                override suspend fun getKiScoreData() = ai.ki_kompetenz_training_org.data.api.KiScoreDataDto()
                override suspend fun getMe() = ai.ki_kompetenz_training_org.data.api.MeResponseDto()
                override suspend fun getMyTeam() = ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto()
                override suspend fun getTeamStats(id: String) = ai.ki_kompetenz_training_org.data.api.TeamStatsResponseDto()
                override suspend fun getSubscriptionStatus() = ai.ki_kompetenz_training_org.data.api.SubscriptionStatusDto()
                override suspend fun getDueCards() = ai.ki_kompetenz_training_org.data.api.SrsDueResponseDto()
                override suspend fun postReview(body: ai.ki_kompetenz_training_org.data.api.SrsReviewRequestDto) = ai.ki_kompetenz_training_org.data.api.SrsReviewResponseDto(success = true)
            },
        )
        // Lessons 1-8 are free (introductory content)
        assertTrue(!gate.isPremiumLesson(1))
        assertTrue(!gate.isPremiumLesson(8))
        // Lesson 9 is premium
        assertTrue(gate.isPremiumLesson(9))
        // Lesson 12 is premium
        assertTrue(gate.isPremiumLesson(12))
        assertTrue(!gate.isPremiumLesson(null))
    }
}

class MarkdownRendererTest {

    @Test
    fun `renders headers bullets and bold`() {
        val md = """
            ## Lernziele

            Nach dieser Lektion kannst du:

            - Künstliche Intelligenz einordnen
            - **Risiken** erkennen

            ### Praxis
            Text mit `Code` und [Link](https://example.org).
        """.trimIndent()

        val out = renderMarkdown(md)
        assertTrue(out.contains("Lernziele"))
        assertTrue(out.contains("• Künstliche Intelligenz einordnen"))
        assertTrue(out.contains("Risiken erkennen")) // bold stripped
        assertTrue(out.contains("Praxis"))
        assertTrue(out.contains("Text mit Code und Link")) // code + link stripped
    }
}