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
}
