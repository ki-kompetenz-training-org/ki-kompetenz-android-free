/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Data-integrity tests for every [ContentBlock.Classification] in all 14
 * bundled lessons ([BundledLessons.all]).
 *
 * A classification exercise is a drag/tap-to-assign puzzle: the learner sees
 * one pool of items and must place each into exactly one category. That only
 * works if the data guarantees an *unambiguous* solution and distinguishable
 * targets:
 *
 *  - An item text that appears under two different categories has no single
 *    correct answer (violates the puzzle's one-solution contract).
 *  - A duplicate text within one category shows up as visually identical
 *    entries in the pool (confusing, looks like a data error).
 *  - Two categories with the same emoji are hard to tell apart in the UI,
 *    which renders "`emoji name`" as the category label.
 *
 * Like [LessonBlocksIntegrityTest], every test walks the whole catalog,
 * collects violations in a [StringBuilder] (locator: lesson id + section
 * index/title + block index) and asserts at the end, so one run reports all
 * violations instead of only the first.
 */
class ClassificationDataIntegrityTest {

    private val allLessons: List<InteractiveLesson> = BundledLessons.all

    /** Reference to one classification block within the catalog. */
    private data class ClassificationRef(
        val lessonId: String,
        val sectionIndex: Int,
        val sectionTitle: String,
        val blockIndex: Int,
        val block: ContentBlock.Classification,
    )

    private fun ClassificationRef.describe(): String =
        "$lessonId / section[$sectionIndex] '$sectionTitle' / block[$blockIndex] Classification"

    /** Flattens all 14 lessons into one list of classification-block refs. */
    private fun allClassifications(): List<ClassificationRef> =
        allLessons.flatMap { lesson ->
            lesson.sections.flatMapIndexed { sectionIndex, section ->
                section.blocks.mapIndexedNotNull { blockIndex, block ->
                    (block as? ContentBlock.Classification)?.let {
                        ClassificationRef(
                            lessonId = lesson.id,
                            sectionIndex = sectionIndex,
                            sectionTitle = section.titleDe,
                            blockIndex = blockIndex,
                            block = it,
                        )
                    }
                }
            }
        }

    /**
     * Maps each [textDe] to the indices of the categories containing it;
     * entries whose value set has more than one element are ambiguous items.
     */
    private fun Map<String, MutableSet<Int>>.ambiguousEntries(): Map<String, Set<Int>> =
        filterValues { it.size > 1 }.mapValues { it.value.toSet() }

    /** Builds "text → containing category indices" for one block. */
    private fun ContentBlock.Classification.textToCategoryIndices(text: (ClassificationItem) -> String): Map<String, MutableSet<Int>> {
        val map = mutableMapOf<String, MutableSet<Int>>()
        categories.forEachIndexed { catIndex, category ->
            category.items.forEach { item ->
                map.getOrPut(text(item)) { mutableSetOf() }.add(catIndex)
            }
        }
        return map
    }

    // ── Catalog precondition ───────────────────────────────────────────────

    @Test
    fun `catalog bundles all fourteen lessons with expected ids`() {
        val expectedIds = (1..14).map { "lesson-$it" }
        assertWithMessage("BundledLessons.all must contain the 14 bundled lessons in order")
            .that(allLessons.map { it.id })
            .containsExactlyElementsIn(expectedIds)
            .inOrder()
    }

    // ── (1) unambiguous solution, German ──────────────────────────────────

    @Test
    fun `no german item text appears in more than one category of the same block`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            ref.block.textToCategoryIndices { it.textDe }
                .ambiguousEntries()
                .forEach { (text, catIndices) ->
                    val catNames = catIndices.sorted().joinToString(", ") { idx -> "'${ref.block.categories[idx].nameDe}'" }
                    violations.append("\n  $where: textDe \"$text\" appears in categories $catNames — assignment is ambiguous")
                }
        }

        assertWithMessage("catalog must contain classification blocks at all").that(checked).isGreaterThan(0)
        assertWithMessage("checked $checked Classification blocks; ambiguous German items:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (2) unambiguous solution, English ─────────────────────────────────

    @Test
    fun `no english item text appears in more than one category of the same block`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            ref.block.textToCategoryIndices { it.textEn }
                .ambiguousEntries()
                .forEach { (text, catIndices) ->
                    val catNames = catIndices.sorted().joinToString(", ") { idx -> "'${ref.block.categories[idx].nameEn}'" }
                    violations.append("\n  $where: textEn \"$text\" appears in categories $catNames — assignment is ambiguous")
                }
        }

        assertWithMessage("checked $checked Classification blocks; ambiguous English items:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (3) no duplicate items within a category ──────────────────────────

    @Test
    fun `no item text is duplicated within the same category`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            ref.block.categories.forEachIndexed { catIndex, category ->
                val cat = "category[$catIndex] '${category.nameDe}'"
                listOf(
                    "textDe" to category.items.map { it.textDe },
                    "textEn" to category.items.map { it.textEn },
                ).forEach { (field, texts) ->
                    texts.groupingBy { it }.eachCount()
                        .filterValues { it > 1 }
                        .forEach { (text, count) ->
                            violations.append("\n  $where: $cat lists $field \"$text\" $count times")
                        }
                }
            }
        }

        assertWithMessage("checked $checked Classification blocks; duplicate items:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (4) visually distinguishable categories ───────────────────────────

    @Test
    fun `category emojis are unique within each block`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            ref.block.categories.groupingBy { it.emoji }.eachCount()
                .filterValues { it > 1 }
                .forEach { (emoji, count) ->
                    val catNames = ref.block.categories.filter { it.emoji == emoji }.joinToString(", ") { "'${it.nameDe}'" }
                    violations.append("\n  $where: $count categories share emoji \"$emoji\": $catNames — targets are indistinguishable")
                }
        }

        assertWithMessage("checked $checked Classification blocks; emoji collisions:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (5) every category is a valid drop target ─────────────────────────

    @Test
    fun `every category contains at least one item`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            ref.block.categories.forEachIndexed { catIndex, category ->
                if (category.items.isEmpty()) {
                    violations.append("\n  $where: category[$catIndex] '${category.nameDe}' has no items, need >= 1")
                }
            }
        }

        assertWithMessage("checked $checked Classification blocks; empty categories:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (6) the exercise is a real exercise ───────────────────────────────

    @Test
    fun `each block offers at least two items across all categories`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val total = ref.block.categories.sumOf { it.items.size }
            if (total < 2) {
                violations.append("\n  ${ref.describe()}: only $total item(s) across all categories, need >= 2 for a meaningful exercise")
            }
        }

        assertWithMessage("checked $checked Classification blocks; too few items:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }

    // ── (7) bilingual instructions ────────────────────────────────────────

    @Test
    fun `german and english instructions are non blank`() {
        val violations = StringBuilder()
        var checked = 0

        allClassifications().forEach { ref ->
            checked++
            val where = ref.describe()
            if (ref.block.instructionDe.isBlank()) violations.append("\n  $where: instructionDe is blank")
            if (ref.block.instructionEn.isBlank()) violations.append("\n  $where: instructionEn is blank")
        }

        assertWithMessage("checked $checked Classification blocks; blank instructions:$violations")
            .that(violations.isEmpty())
            .isTrue()
    }
}
