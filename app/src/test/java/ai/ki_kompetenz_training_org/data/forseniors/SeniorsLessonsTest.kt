package ai.ki_kompetenz_training_org.data.forseniors

import org.junit.Assert.*
import org.junit.Test

/**
 * Data integrity tests for ForSeniors lessons.
 * Verifies lesson structure, quiz validity, and content coverage.
 */
class SeniorsLessonsTest {

    @Test
    fun `all 6 lessons are available`() {
        assertEquals(6, SeniorsLessons.all.size)
    }

    @Test
    fun `all lessons have unique IDs`() {
        val ids = SeniorsLessons.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all lessons have valid IDs`() {
        SeniorsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} should start with seniors_", lesson.id.startsWith("seniors_"))
        }
    }

    @Test
    fun `all lessons have non-empty titles`() {
        SeniorsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty title", lesson.title.isNotBlank())
        }
    }

    @Test
    fun `all lessons have non-empty descriptions`() {
        SeniorsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty description", lesson.description.isNotBlank())
        }
    }
    @Test
    fun `all lessons have at least 1 section`() {
        SeniorsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has no sections", lesson.sections.isNotEmpty())
        }
    }

    @Test
    fun `all lessons have valid emoji`() {
        SeniorsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty emoji", lesson.emoji.isNotBlank())
        }
    }

    @Test
    fun `all sections have non-empty titles`() {
        SeniorsLessons.all.flatMap { it.sections }.forEach { section ->
            assertTrue("Section title should be non-empty", section.title.isNotBlank())
        }
    }

    @Test
    fun `all sections have non-empty content`() {
        SeniorsLessons.all.flatMap { it.sections }.forEach { section ->
            assertTrue("Section ${section.title} has empty content", section.content.isNotBlank())
        }
    }

    @Test
    fun `all sections have key takeaways`() {
        SeniorsLessons.all.flatMap { it.sections }.forEach { section ->
            assertTrue("Section ${section.title} has empty key takeaway", section.keyTakeaway.isNotBlank())
        }
    }

    @Test
    fun `all quizzes have at least 2 options`() {
        SeniorsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz should have at least 2 options, got ${quiz.options.size}",
                    quiz.options.size >= 2)
            }
    }

    @Test
    fun `all quiz correctIndex is valid`() {
        SeniorsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz correctIndex ${quiz.correctIndex} out of range [0, ${quiz.options.size})",
                    quiz.correctIndex >= 0 && quiz.correctIndex < quiz.options.size)
            }
    }

    @Test
    fun `all quizzes have non-empty explanations`() {
        SeniorsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz has empty explanation", quiz.explanation.isNotBlank())
            }
    }

    // ── Content coverage ──

    @Test
    fun `covers password security topic`() {
        val allText = SeniorsLessons.all.joinToString(" ") { it.title + it.description }
        assertTrue("Should cover password security", allText.lowercase().contains("passwort") || allText.lowercase().contains("pass"))
    }

    @Test
    fun `covers phishing topic`() {
        val allText = SeniorsLessons.all.joinToString(" ") {
            it.title + " " + it.description + " " +
            it.sections.joinToString(" ") { s -> s.title + " " + s.content }
        }
        assertTrue("Should cover phishing", allText.lowercase().contains("phishing"))
    }

    @Test
    fun `no lesson contains external server URLs`() {
        // Note: lessons may mention https:// as part of security education
        // (e.g., "look for https:// in the URL bar"). This is educational content,
        // not a server call. We only check for actual URLs (http://example.com).
        val allText = SeniorsLessons.all.joinToString(" ") {
            it.title + it.description +
            it.sections.joinToString(" ") { s -> s.content + s.keyTakeaway + (s.quiz?.question ?: "") }
        }
        // Check for actual clickable URLs (domain.tld pattern), not https:// mentions
        assertFalse("ForSeniors content should not contain clickable server URLs",
            allText.contains(".com/") || allText.contains(".org/") || allText.contains(".de/"))
    }

    // ── AI literacy coverage (GAP-8) ──

    @Test
    fun `all 6 lessons have at least one AI-related section`() {
        SeniorsLessons.all.forEach { lesson ->
            val hasAi = lesson.sections.any { s ->
                listOf(s.title, s.content).any { it.contains("KI") || it.contains("AI") || it.contains("künstliche Intelligenz") }
            }
            assertTrue("Lesson ${lesson.id} should have at least one AI-related section", hasAi)
        }
    }

    @Test
    fun `AI sections in lessons 1-3 have a quiz with 4 options`() {
        listOf("seniors_01", "seniors_02", "seniors_03").forEach { id ->
            val lesson = SeniorsLessons.all.first { it.id == id }
            val aiSection = lesson.sections.first { s ->
                listOf(s.title, s.content).any { it.contains("KI") || it.contains("AI") || it.contains("künstliche Intelligenz") }
            }
            val quiz = aiSection.quiz
            assertNotNull("AI section in $id should have a quiz", quiz)
            assertEquals("AI section quiz in $id should have 4 options", 4, quiz!!.options.size)
        }
    }

    @Test
    fun `AI sections in lessons 1-3 have non-empty content and keyTakeaway`() {
        listOf("seniors_01", "seniors_02", "seniors_03").forEach { id ->
            val lesson = SeniorsLessons.all.first { it.id == id }
            val aiSection = lesson.sections.first { s ->
                listOf(s.title, s.content).any { it.contains("KI") || it.contains("AI") || it.contains("künstliche Intelligenz") }
            }
            assertTrue("AI section in $id has empty content", aiSection.content.isNotBlank())
            assertTrue("AI section in $id has empty keyTakeaway", aiSection.keyTakeaway.isNotBlank())
        }
    }

    @Test
    fun `original sections in lessons 1-3 remain intact after adding AI sections`() {
        val originals = mapOf(
            "seniors_01" to listOf("Ein gutes Passwort ist wie ein gutes Türschloss", "Zwei-Faktor-Authentifizierung (2FA)"),
            "seniors_02" to listOf("Was ist Phishing?"),
            "seniors_03" to listOf("Sicher einkaufen im Internet"),
        )
        originals.forEach { (id, titles) ->
            val lesson = SeniorsLessons.all.first { it.id == id }
            val titles1 = lesson.sections.map { it.title }
            titles.forEach { t ->
                assertTrue("Original section '$t' missing in $id", titles1.contains(t))
            }
            assertEquals("AI section should be appended after originals in $id",
                titles.size, titles1.indexOfFirst { it !in titles })
        }
    }
}
