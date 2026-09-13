/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import ai.ki_kompetenz_training_org.ui.lessons.InteractiveLessonLogic
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * GAP-6: PromptExercise ist ein Übungsblock (kein Quiz):
 * keine Wertung, kein Kompetenz-Tracking, reines Üben.
 */
class PromptExerciseTest {

    @Test
    fun `Lektion 10 enthaelt mindestens einen PromptExercise-Block`() {
        val blocks = Lesson10.lesson.sections.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.PromptExercise>()

        assertThat(blocks).isNotEmpty()
    }

    @Test
    fun `PromptExercise-Block hat vollstaendige bilinguale Inhalte`() {
        val blocks = Lesson10.lesson.sections.flatMap { it.blocks }
            .filterIsInstance<ContentBlock.PromptExercise>()

        blocks.forEach { b ->
            assertThat(b.promptDe).isNotEmpty()
            assertThat(b.promptEn).isNotEmpty()
            assertThat(b.modelAnswerDe).isNotEmpty()
            assertThat(b.modelAnswerEn).isNotEmpty()
        }
    }

    @Test
    fun `Section nur mit PromptExercise gilt nicht als Quiz-Section`() {
        val section = LessonSection(
            titleDe = "Übung",
            titleEn = "Practice",
            blocks = listOf(
                ContentBlock.PromptExercise(
                    promptDe = "Schreibe einen Prompt.",
                    promptEn = "Write a prompt.",
                    modelAnswerDe = "Modelllösung.",
                    modelAnswerEn = "Model answer.",
                ),
            ),
        )

        assertThat(InteractiveLessonLogic.sectionHasQuiz(section)).isFalse()
    }
}
