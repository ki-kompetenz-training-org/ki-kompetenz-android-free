package ai.ki_kompetenz_training_org.data.forkids

import org.junit.Assert.*
import org.junit.Test

/**
 * Data integrity tests for COPPA-compliant ForKids lessons.
 * Verifies lesson structure, quiz validity, and COPPA compliance.
 */
class KidsLessonsTest {

    @Test
    fun `all 5 lessons are available`() {
        assertEquals(5, KidsLessons.all.size)
    }

    @Test
    fun `all lessons have unique IDs`() {
        val ids = KidsLessons.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all lessons have valid IDs`() {
        KidsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} should start with kids_", lesson.id.startsWith("kids_"))
        }
    }

    @Test
    fun `all lessons have non-empty titles`() {
        KidsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty title", lesson.title.isNotBlank())
        }
    }

    @Test
    fun `all lessons have non-empty descriptions`() {
        KidsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty description", lesson.description.isNotBlank())
        }
    }
    @Test
    fun `all lessons have at least 1 section`() {
        KidsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has no sections", lesson.sections.isNotEmpty())
        }
    }

    @Test
    fun `all lessons have valid emoji`() {
        KidsLessons.all.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty emoji", lesson.emoji.isNotBlank())
        }
    }

    @Test
    fun `all sections have non-empty titles`() {
        KidsLessons.all.flatMap { it.sections }.forEach { section ->
            assertTrue("Section title should be non-empty", section.title.isNotBlank())
        }
    }

    @Test
    fun `all quizzes have exactly 2 options`() {
        KidsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertEquals("Quiz should have exactly 2 options for kids, got ${quiz.options.size}",
                    2, quiz.options.size)
            }
    }

    @Test
    fun `all quiz correctIndex is valid`() {
        KidsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz correctIndex ${quiz.correctIndex} out of range [0, ${quiz.options.size})",
                    quiz.correctIndex >= 0 && quiz.correctIndex < quiz.options.size)
            }
    }

    @Test
    fun `all quizzes have non-empty explanations`() {
        KidsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz has empty explanation", quiz.explanation.isNotBlank())
            }
    }

    @Test
    fun `all quizzes have encouragement`() {
        KidsLessons.all.flatMap { it.sections }
            .mapNotNull { it.quiz }
            .forEach { quiz ->
                assertTrue("Quiz has empty encouragement", quiz.encouragement.isNotBlank())
            }
    }

    // ── COPPA compliance ──

    @Test
    fun `coppa notice has required fields`() {
        assertTrue("COPPA heading should be non-empty", CoppaNotice.HEADING.isNotBlank())
        assertTrue("COPPA body should be non-empty", CoppaNotice.BODY.isNotBlank())
        assertTrue("COPPA deletion hint should be non-empty", CoppaNotice.DELETION_HINT.isNotBlank())
        assertEquals("COPPA parent gate PIN should be 4 digits", 4, CoppaNotice.PARENT_GATE_PIN.length)
    }

    @Test
    fun `coppa notice mentions no data collection`() {
        val body = CoppaNotice.BODY.lowercase()
        assertTrue("COPPA notice should mention no data collection", body.contains("keine daten") || body.contains("kein tracking"))
        assertTrue("COPPA notice should mention no server communication", body.contains("server"))
    }

    @Test
    fun `no lesson contains server URLs`() {
        val allText = KidsLessons.all.joinToString(" ") { it.title + it.description +
            it.sections.joinToString(" ") { s -> s.content + s.funFact + (s.quiz?.question ?: "") } }
        assertFalse("ForKids content should not contain server URLs",
            allText.contains("http://") || allText.contains("https://"))
    }
}
