package ai.ki_kompetenz_training_org.data.lessons

import org.junit.Assert.*
import org.junit.Test

/**
 * Data integrity tests for the 14 interactive lessons.
 * Verifies lesson structure, bilingual content, and block validity.
 */
class InteractiveLessonsTest {

    private val allLessons = listOf(
        Lesson1.lesson, Lesson2.lesson, Lesson3.lesson, Lesson4.lesson,
        Lesson5.lesson, Lesson6.lesson, Lesson7.lesson, Lesson8.lesson,
        Lesson9.lesson, Lesson10.lesson, Lesson11.lesson, Lesson12.lesson,
        Lesson13.lesson, Lesson14.lesson,
    )

    @Test
    fun `all 14 lessons exist`() {
        assertEquals(14, allLessons.size)
    }

    @Test
    fun `all lessons have unique IDs`() {
        val ids = allLessons.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all lesson IDs match expected pattern`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ID ${lesson.id} should start with lesson-", lesson.id.startsWith("lesson-"))
        }
    }

    @Test
    fun `all lessons have valid lesson numbers 1 to 14`() {
        val numbers = allLessons.map { it.lessonNumber }.sorted()
        assertEquals("Should have lessons 1-14", (1..14).toList(), numbers)
    }

    @Test
    fun `all lessons have non-empty German titles`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty German title", lesson.titleDe.isNotBlank())
        }
    }

    @Test
    fun `all lessons have non-empty English titles`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty English title", lesson.titleEn.isNotBlank())
        }
    }
    @Test
    fun `all lessons have non-empty German descriptions`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty German description", lesson.descriptionDe.isNotBlank())
        }
    }

    @Test
    fun `all lessons have non-empty English descriptions`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has empty English description", lesson.descriptionEn.isNotBlank())
        }
    }

    @Test
    fun `all lessons have at least 1 section`() {
        allLessons.forEach { lesson ->
            assertTrue("Lesson ${lesson.id} has no sections", lesson.sections.isNotEmpty())
        }
    }

    @Test
    fun `all sections have non-empty bilingual titles`() {
        allLessons.flatMap { it.sections }.forEach { section ->
            assertTrue("Section has empty German title", section.titleDe.isNotBlank())
            assertTrue("Section has empty English title", section.titleEn.isNotBlank())
        }
    }

    @Test
    fun `all sections have at least 1 block`() {
        allLessons.flatMap { it.sections }.forEach { section ->
            assertTrue("Section has no blocks", section.blocks.isNotEmpty())
        }
    }

    @Test
    fun `total blocks across all lessons is significant`() {
        val totalBlocks = allLessons.flatMap { it.sections }.flatMap { it.blocks }.size
        assertTrue("Should have at least 30 blocks total, got $totalBlocks", totalBlocks >= 30)
    }

    @Test
    fun `all text blocks have non-empty bilingual content`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Text>()
            .forEach { block ->
                assertTrue("Text block has empty German text", block.textDe.isNotBlank())
                assertTrue("Text block has empty English text", block.textEn.isNotBlank())
            }
    }

    @Test
    fun `all callout blocks have valid type`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Callout>()
            .forEach { block ->
                assertNotNull("Callout type should not be null", block.type)
            }
    }

    @Test
    fun `all callout blocks have non-empty bilingual text`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Callout>()
            .forEach { block ->
                assertTrue("Callout has empty German text", block.textDe.isNotBlank())
                assertTrue("Callout has empty English text", block.textEn.isNotBlank())
            }
    }
    @Test
    fun `all quiz blocks have at least 2 options`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Quiz>()
            .forEach { block ->
                assertTrue("Quiz block should have at least 2 options, got ${block.options.size}",
                    block.options.size >= 2)
            }
    }

    @Test
    fun `all quiz blocks have exactly one correct option`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Quiz>()
            .forEach { block ->
                val correctCount = block.options.count { it.isCorrect }
                assertEquals("Quiz should have exactly 1 correct option, got $correctCount",
                    1, correctCount)
            }
    }

    @Test
    fun `all quiz blocks have non-empty bilingual explanations`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Quiz>()
            .forEach { block ->
                assertTrue("Quiz has empty German explanation", block.explanationDe.isNotBlank())
                assertTrue("Quiz has empty English explanation", block.explanationEn.isNotBlank())
            }
    }

    @Test
    fun `all knowledge check blocks have non-empty bilingual content`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.KnowledgeCheck>()
            .forEach { block ->
                assertTrue("KnowledgeCheck has empty German question", block.questionDe.isNotBlank())
                assertTrue("KnowledgeCheck has empty English question", block.questionEn.isNotBlank())
                assertTrue("KnowledgeCheck has empty German answer", block.answerDe.isNotBlank())
                assertTrue("KnowledgeCheck has empty English answer", block.answerEn.isNotBlank())
            }
    }

    @Test
    fun `all fill blank blocks have valid correct index`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.FillBlank>()
            .forEach { block ->
                assertTrue("FillBlank correctIndex ${block.correctIndex} out of range",
                    block.correctIndex >= 0 && block.correctIndex < block.choices.size)
                assertTrue("FillBlank should have at least 2 choices", block.choices.size >= 2)
            }
    }

    @Test
    fun `all true false blocks have non-empty bilingual explanations`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.TrueFalse>()
            .forEach { block ->
                assertTrue("TrueFalse has empty German explanation", block.explanationDe.isNotBlank())
                assertTrue("TrueFalse has empty English explanation", block.explanationEn.isNotBlank())
            }
    }

    @Test
    fun `all classification blocks have categories with items`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.Classification>()
            .forEach { block ->
                assertTrue("Classification should have at least 2 categories", block.categories.size >= 2)
                block.categories.forEach { cat ->
                    assertTrue("Category has empty German name", cat.nameDe.isNotBlank())
                    assertTrue("Category has empty English name", cat.nameEn.isNotBlank())
                    assertTrue("Category should have at least 1 item", cat.items.isNotEmpty())
                }
            }
    }

    @Test
    fun `all risk thermometer blocks have non-empty titles`() {
        allLessons.flatMap { it.sections }.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.RiskThermometer>()
            .forEach { block ->
                assertTrue("RiskThermometer has empty German title", block.titleDe.isNotBlank())
                assertTrue("RiskThermometer has empty English title", block.titleEn.isNotBlank())
            }
    }
}
