/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.lessons.ContentBlock
import ai.ki_kompetenz_training_org.data.lessons.InteractiveLesson
import ai.ki_kompetenz_training_org.data.lessons.LessonSection

/**
 * Pure Logik der InteractiveLesson — aus [InteractiveLessonScreen] extrahiert,
 * damit sie unit-testbar ist (BUG-Report 2026-09-05: der disabled
 * "Quizzes bestehen, um abzuschließen"-Button war eine Sackgasse).
 */
object InteractiveLessonLogic {

    /** Mindestpunktzahl (0-100), um ein Lektions-Quiz als bestanden zu zählen. */
    const val QUIZ_PASS_SCORE = 60

    /** Hat die Section einen Multiple-Choice-Quiz-Block? */
    fun sectionHasQuiz(section: LessonSection): Boolean =
        section.blocks.any { it is ContentBlock.Quiz }

    /**
     * Ist die ganze Lektion abschließbar? true, wenn jede Section entweder
     * kein Quiz hat oder deren Score >= [QUIZ_PASS_SCORE] ist.
     */
    fun isLessonPassed(lesson: InteractiveLesson, scores: Map<Int, Int>): Boolean =
        lesson.sections.indices.all { i ->
            val sec = lesson.sections[i]
            !sectionHasQuiz(sec) || scores.getOrDefault(i, 0) >= QUIZ_PASS_SCORE
        }

    /**
     * Erster Section-Index mit offenem (nicht bestandenem) Quiz,
     * oder null, wenn alle Quizzes bestanden sind bzw. keins existiert.
     */
    fun firstOpenQuizSection(lesson: InteractiveLesson, scores: Map<Int, Int>): Int? =
        lesson.sections.indices.firstOrNull { i ->
            sectionHasQuiz(lesson.sections[i]) && scores.getOrDefault(i, 0) < QUIZ_PASS_SCORE
        }

    /**
     * Scroll-Ziel (Pixel) für den "Quizzes bestehen"-Klick: springt
     * fraction-basiert zum ersten offenen Quiz. null → kein offenes Quiz
     * (Button schließt dann die Lektion ab). maxValue <= 0 (nicht gelayoutet)
     * → 0 (Seitenanfang).
     */
    fun scrollTargetForOpenQuiz(
        lesson: InteractiveLesson,
        scores: Map<Int, Int>,
        maxValue: Int,
    ): Int? =
        firstOpenQuizSection(lesson, scores)?.let { idx ->
            if (maxValue <= 0) 0
            else (idx.toDouble() / lesson.sections.size * maxValue).toInt()
        }
}
