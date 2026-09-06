/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.lessons.Lesson1
import ai.ki_kompetenz_training_org.data.lessons.Lesson10
import ai.ki_kompetenz_training_org.data.lessons.Lesson11
import ai.ki_kompetenz_training_org.data.lessons.Lesson12
import ai.ki_kompetenz_training_org.data.lessons.Lesson13
import ai.ki_kompetenz_training_org.data.lessons.Lesson14
import ai.ki_kompetenz_training_org.data.lessons.Lesson2
import ai.ki_kompetenz_training_org.data.lessons.Lesson3
import ai.ki_kompetenz_training_org.data.lessons.Lesson4
import ai.ki_kompetenz_training_org.data.lessons.Lesson5
import ai.ki_kompetenz_training_org.data.lessons.Lesson6
import ai.ki_kompetenz_training_org.data.lessons.Lesson7
import ai.ki_kompetenz_training_org.data.lessons.Lesson8
import ai.ki_kompetenz_training_org.data.lessons.Lesson9
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Daten-Vertrag zwischen den 14 gebundenen Lektionen und der
 * InteractiveLessonScreen-Logik ([InteractiveLessonLogic]).
 *
 * Der BUG-Report 2026-09-05 zeigte: Der "Quizzes bestehen"-Button schützt
 * die Lektion. Das funktioniert NUR, wenn jede Lektion wirklich Quiz-Blöcke
 * enthält — sonst ist isLessonPassed sofort true und die Lektion gilt ohne
 * Prüfung als abgeschlossen. Dieser Test verriegelt diesen Vertrag.
 */
class InteractiveLessonQuizCoverageTest {

    private val allLessons = listOf(
        Lesson1.lesson, Lesson2.lesson, Lesson3.lesson, Lesson4.lesson,
        Lesson5.lesson, Lesson6.lesson, Lesson7.lesson, Lesson8.lesson,
        Lesson9.lesson, Lesson10.lesson, Lesson11.lesson, Lesson12.lesson,
        Lesson13.lesson, Lesson14.lesson,
    )

    @Test
    fun `JEDE Lektion enthaelt mindestens einen Quiz-Block (sonst Auto-Pass ohne Prüfung)`() {
        val withoutQuiz = allLessons.filter { lesson ->
            lesson.sections.none { section ->
                section.blocks.any { it is ai.ki_kompetenz_training_org.data.lessons.ContentBlock.Quiz }
            }
        }
        assertThat(withoutQuiz.map { it.id }).isEmpty()
    }

    @Test
    fun `keine Lektion ist beim ersten Aufruf bereits bestanden (isLessonPassed=false)`() {
        allLessons.forEach { lesson ->
            assertThat(InteractiveLessonLogic.isLessonPassed(lesson, emptyMap()))
                .isFalse()
        }
    }

    @Test
    fun `Quiz-Blöcke sind spielbar - mindestens 2 Optionen und GENAU eine richtige`() {
        allLessons.forEach { lesson ->
            lesson.sections.forEach { section ->
                section.blocks.forEach { block ->
                    if (block is ai.ki_kompetenz_training_org.data.lessons.ContentBlock.Quiz) {
                        assertThat(block.options.size).isAtLeast(2)
                        assertThat(block.options.count { it.isCorrect }).isEqualTo(1)
                    }
                }
            }
        }
    }

    @Test
    fun `Quiz-Blöcke sind vollständig zweisprachig - Frage, Optionen und Erklärung`() {
        allLessons.forEach { lesson ->
            lesson.sections.forEach { section ->
                section.blocks.forEach { block ->
                    if (block is ai.ki_kompetenz_training_org.data.lessons.ContentBlock.Quiz) {
                        assertThat(block.questionDe).isNotEmpty()
                        assertThat(block.questionEn).isNotEmpty()
                        assertThat(block.explanationDe).isNotEmpty()
                        assertThat(block.explanationEn).isNotEmpty()
                        block.options.forEach { option ->
                            assertThat(option.textDe).isNotEmpty()
                            assertThat(option.textEn).isNotEmpty()
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `firstOpenQuizSection findet in jeder Lektion ein Quiz (frischer Start)`() {
        allLessons.forEach { lesson ->
            assertThat(InteractiveLessonLogic.firstOpenQuizSection(lesson, emptyMap()))
                .isNotNull()
        }
    }
}
