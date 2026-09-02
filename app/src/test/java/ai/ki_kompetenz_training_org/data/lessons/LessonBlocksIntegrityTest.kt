/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Integrity tests for the interactive content blocks of all 14 bundled
 * lessons ([BundledLessons.all]).
 *
 * Each test walks every lesson → section → block, collects violations in a
 * [StringBuilder] (locator: lesson id + section index/title + block index)
 * and asserts at the end so one run reports *all* violations, not just the
 * first. Interactive block kinds: Quiz, FillBlank, TrueFalse, Classification,
 * KnowledgeCheck (RiskThermometer and the passive Text/Callout blocks are
 * presentational).
 */
class LessonBlocksIntegrityTest {

    private val allLessons: List<InteractiveLesson> = BundledLessons.all

    /** Reference to one block within the catalog, for precise error messages. */
    private data class BlockRef(
        val lessonId: String,
        val sectionIndex: Int,
        val sectionTitle: String,
        val blockIndex: Int,
        val block: ContentBlock,
    )

    private fun BlockRef.describe(): String =
        "$lessonId / section[$sectionIndex] '${sectionTitle}' / block[$blockIndex] ${block::class.simpleName}"

    /** Flattens the whole catalog (14 lessons × sections × blocks) into refs. */
    private fun allBlocks(): List<BlockRef> =
        allLessons.flatMap { lesson ->
            lesson.sections.flatMapIndexed { sectionIndex, section ->
                section.blocks.mapIndexed { blockIndex, block ->
                    BlockRef(
                        lessonId = lesson.id,
                        sectionIndex = sectionIndex,
                        sectionTitle = section.titleDe,
                        blockIndex = blockIndex,
                        block = block,
                    )
                }
            }
        }

    /** Interactive block kinds — the ones requiring a user decision. */
    private fun ContentBlock.isInteractive(): Boolean =
        this is ContentBlock.Quiz ||
            this is ContentBlock.FillBlank ||
            this is ContentBlock.TrueFalse ||
            this is ContentBlock.Classification ||
            this is ContentBlock.KnowledgeCheck

    // ── Catalog sanity (precondition for everything below) ─────────────────

    @Test
    fun `catalog bundles all fourteen lessons with expected ids`() {
        val expectedIds = (1..14).map { "lesson-$it" }
        assertWithMessage("BundledLessons.all must contain the 14 bundled lessons in order")
            .that(allLessons.map { it.id })
            .containsExactlyElementsIn(expectedIds)
            .inOrder()
    }

    // ── Quiz blocks ────────────────────────────────────────────────────────

    @Test
    fun `every quiz block has at least two options, exactly one correct, and complete bilingual texts`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val quiz = ref.block as? ContentBlock.Quiz ?: return@forEach
            checked++
            val where = ref.describe()

            if (quiz.options.size < 2) {
                violations.append("\n  $where: only ${quiz.options.size} option(s), need >= 2")
            }
            val correctCount = quiz.options.count { it.isCorrect }
            if (correctCount != 1) {
                violations.append("\n  $where: $correctCount option(s) flagged isCorrect=true, need exactly 1")
            }
            if (quiz.questionDe.isBlank()) violations.append("\n  $where: questionDe is blank")
            if (quiz.questionEn.isBlank()) violations.append("\n  $where: questionEn is blank")
            if (quiz.explanationDe.isBlank()) violations.append("\n  $where: explanationDe is blank")
            if (quiz.explanationEn.isBlank()) violations.append("\n  $where: explanationEn is blank")
            quiz.options.forEachIndexed { optionIndex, option ->
                if (option.textDe.isBlank()) violations.append("\n  $where: option[$optionIndex].textDe is blank")
                if (option.textEn.isBlank()) violations.append("\n  $where: option[$optionIndex].textEn is blank")
            }
        }

        assertWithMessage("catalog must contain quiz blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked Quiz blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── FillBlank blocks ───────────────────────────────────────────────────

    @Test
    fun `every fill blank block has at least two choices, a valid correct index, and complete bilingual texts`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val fill = ref.block as? ContentBlock.FillBlank ?: return@forEach
            checked++
            val where = ref.describe()

            if (fill.choices.size < 2) {
                violations.append("\n  $where: only ${fill.choices.size} choice(s), need >= 2")
            }
            if (fill.correctIndex !in fill.choices.indices) {
                violations.append("\n  $where: correctIndex=${fill.correctIndex} outside choices.indices=${fill.choices.indices}")
            }
            if (fill.choices.any { it.isBlank() }) {
                violations.append("\n  $where: contains blank choice text(s)")
            }
            if (fill.blankKey.isBlank()) violations.append("\n  $where: blankKey is blank")
            if (fill.sentenceDe.isBlank()) violations.append("\n  $where: sentenceDe is blank")
            if (fill.sentenceEn.isBlank()) violations.append("\n  $where: sentenceEn is blank")
            if (fill.explanationDe.isBlank()) violations.append("\n  $where: explanationDe is blank")
            if (fill.explanationEn.isBlank()) violations.append("\n  $where: explanationEn is blank")
        }

        assertWithMessage("catalog must contain fill-blank blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked FillBlank blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── Classification blocks ──────────────────────────────────────────────

    @Test
    fun `every classification block has non-empty categories with items and complete bilingual labels`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val cls = ref.block as? ContentBlock.Classification ?: return@forEach
            checked++
            val where = ref.describe()

            if (cls.categories.isEmpty()) {
                violations.append("\n  $where: categories list is empty")
            }
            if (cls.instructionDe.isBlank()) violations.append("\n  $where: instructionDe is blank")
            if (cls.instructionEn.isBlank()) violations.append("\n  $where: instructionEn is blank")
            cls.categories.forEachIndexed { categoryIndex, category ->
                val cat = "category[$categoryIndex] '${category.nameDe}'"
                if (category.items.isEmpty()) {
                    violations.append("\n  $where: $cat has no items, need >= 1")
                }
                if (category.nameDe.isBlank()) violations.append("\n  $where: $cat nameDe is blank")
                if (category.nameEn.isBlank()) violations.append("\n  $where: $cat nameEn is blank")
                if (category.emoji.isBlank()) violations.append("\n  $where: $cat emoji is blank")
                category.items.forEachIndexed { itemIndex, item ->
                    if (item.textDe.isBlank()) {
                        violations.append("\n  $where: $cat item[$itemIndex].textDe is blank")
                    }
                    if (item.textEn.isBlank()) {
                        violations.append("\n  $where: $cat item[$itemIndex].textEn is blank")
                    }
                }
            }
        }

        assertWithMessage("catalog must contain classification blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked Classification blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── TrueFalse blocks ───────────────────────────────────────────────────

    @Test
    fun `every true false block has bilingual statements and explanations`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val tf = ref.block as? ContentBlock.TrueFalse ?: return@forEach
            checked++
            val where = ref.describe()

            if (tf.statementDe.isBlank()) violations.append("\n  $where: statementDe is blank")
            if (tf.statementEn.isBlank()) violations.append("\n  $where: statementEn is blank")
            if (tf.explanationDe.isBlank()) violations.append("\n  $where: explanationDe is blank")
            if (tf.explanationEn.isBlank()) violations.append("\n  $where: explanationEn is blank")
        }

        assertWithMessage("catalog must contain true/false blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked TrueFalse blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── KnowledgeCheck blocks ──────────────────────────────────────────────

    @Test
    fun `every knowledge check block has all four bilingual text fields`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val kc = ref.block as? ContentBlock.KnowledgeCheck ?: return@forEach
            checked++
            val where = ref.describe()

            if (kc.questionDe.isBlank()) violations.append("\n  $where: questionDe is blank")
            if (kc.questionEn.isBlank()) violations.append("\n  $where: questionEn is blank")
            if (kc.answerDe.isBlank()) violations.append("\n  $where: answerDe is blank")
            if (kc.answerEn.isBlank()) violations.append("\n  $where: answerEn is blank")
        }

        assertWithMessage("catalog must contain knowledge-check blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked KnowledgeCheck blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── Callout blocks ─────────────────────────────────────────────────────

    @Test
    fun `every callout block carries bilingual text`() {
        val violations = StringBuilder()
        var checked = 0

        allBlocks().forEach { ref ->
            val callout = ref.block as? ContentBlock.Callout ?: return@forEach
            checked++
            val where = ref.describe() // CalloutType itself is an enum → always valid

            if (callout.textDe.isBlank()) violations.append("\n  $where (type=${callout.type}): textDe is blank")
            if (callout.textEn.isBlank()) violations.append("\n  $where (type=${callout.type}): textEn is blank")
        }

        assertWithMessage("catalog must contain callout blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked Callout blocks; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── Lesson-level interactivity ─────────────────────────────────────────

    @Test
    fun `every lesson contains at least one interactive block`() {
        val violations = StringBuilder()
        var checked = 0

        allLessons.forEach { lesson ->
            checked++
            val blockTypes = lesson.sections.flatMap { section -> section.blocks.map { it::class.simpleName } }
            val interactiveCount = lesson.sections
                .flatMap { it.blocks }
                .count { it.isInteractive() }
            if (interactiveCount == 0) {
                violations.append(
                    "\n  ${lesson.id} ('${lesson.titleDe}'): no interactive block " +
                        "(Quiz/FillBlank/TrueFalse/Classification/KnowledgeCheck); " +
                        "only ${blockTypes.joinToString(", ")}",
                )
            }
        }

        assertWithMessage("checked $checked lessons; violations:$violations")
            .that(violations.isEmpty())
            .isTrue()
        assertThat(checked).isEqualTo(14)
    }
}
