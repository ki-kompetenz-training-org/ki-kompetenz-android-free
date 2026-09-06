/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.lessons.CalloutType
import ai.ki_kompetenz_training_org.data.lessons.ContentBlock
import ai.ki_kompetenz_training_org.data.lessons.InteractiveLesson
import ai.ki_kompetenz_training_org.data.lessons.LessonSection
import ai.ki_kompetenz_training_org.data.lessons.QuizOption
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für die pure Lektions-Logik (extrahiert aus
 * InteractiveLessonScreen im Zuge des BUG-Reports 2026-09-05:
 * "Quizzes bestehen, um abzuschließen" war nicht klickbar).
 */
class InteractiveLessonLogicTest {

    // ── Fixtures ─────────────────────────────────────────────────────────

    private fun quizBlock() = ContentBlock.Quiz(
        questionDe = "Frage?",
        questionEn = "Question?",
        options = listOf(
            QuizOption("A", "A", isCorrect = true),
            QuizOption("B", "B", isCorrect = false),
        ),
        explanationDe = "Erklärung",
        explanationEn = "Explanation",
    )

    private fun textBlock() = ContentBlock.Text(textDe = "Text", textEn = "Text")

    private fun section(vararg blocks: ContentBlock) =
        LessonSection(titleDe = "S", titleEn = "S", blocks = blocks.toList())

    private fun lesson(vararg sections: LessonSection) = InteractiveLesson(
        id = "lesson-1",
        lessonNumber = 1,
        titleDe = "Lektion",
        titleEn = "Lesson",
        descriptionDe = "Beschreibung",
        descriptionEn = "Description",
        durationMinutes = 20,
        objectivesDe = listOf("Ziel"),
        objectivesEn = listOf("Objective"),
        sections = sections.toList(),
    )

    // ── sectionHasQuiz ───────────────────────────────────────────────────

    @Test
    fun `sectionHasQuiz - Quiz-Block wird erkannt`() {
        assertThat(InteractiveLessonLogic.sectionHasQuiz(section(quizBlock()))).isTrue()
    }

    @Test
    fun `sectionHasQuiz - Text-und-Callout-Section hat kein Quiz`() {
        val sec = section(textBlock(), ContentBlock.Callout(CalloutType.TIP, "Tipp", "Tip"))
        assertThat(InteractiveLessonLogic.sectionHasQuiz(sec)).isFalse()
    }

    // ── isLessonPassed ───────────────────────────────────────────────────

    @Test
    fun `isLessonPassed - ohne Quizzes immer true`() {
        val l = lesson(section(textBlock()), section(textBlock()))
        assertThat(InteractiveLessonLogic.isLessonPassed(l, emptyMap())).isTrue()
    }

    @Test
    fun `isLessonPassed - 59 Punkte nicht bestanden, 60 bestanden (Grenze)`() {
        val l = lesson(section(quizBlock()))
        assertThat(InteractiveLessonLogic.isLessonPassed(l, mapOf(0 to 59))).isFalse()
        assertThat(InteractiveLessonLogic.isLessonPassed(l, mapOf(0 to 60))).isTrue()
    }

    @Test
    fun `isLessonPassed - fehlender Score zaehlt als 0 (nicht bestanden)`() {
        val l = lesson(section(quizBlock()), section(textBlock()))
        assertThat(InteractiveLessonLogic.isLessonPassed(l, emptyMap())).isFalse()
    }

    @Test
    fun `isLessonPassed - ein bestandenes und ein offenes Quiz = nicht bestanden`() {
        val l = lesson(section(quizBlock()), section(quizBlock()))
        assertThat(InteractiveLessonLogic.isLessonPassed(l, mapOf(0 to 100, 1 to 59))).isFalse()
        assertThat(InteractiveLessonLogic.isLessonPassed(l, mapOf(0 to 100, 1 to 60))).isTrue()
    }

    // ── firstOpenQuizSection ──────────────────────────────────────────────

    @Test
    fun `firstOpenQuizSection - liefert den ERSTEN offenen Quiz-Index`() {
        val l = lesson(
            section(textBlock()),          // 0: kein Quiz
            section(quizBlock()),          // 1: offen
            section(quizBlock()),          // 2: offen
        )
        assertThat(InteractiveLessonLogic.firstOpenQuizSection(l, emptyMap())).isEqualTo(1)
    }

    @Test
    fun `firstOpenQuizSection - ueberspringt bestandene Quizzes`() {
        val l = lesson(
            section(quizBlock()),          // 0: bestanden
            section(quizBlock()),          // 1: offen
        )
        assertThat(InteractiveLessonLogic.firstOpenQuizSection(l, mapOf(0 to 80))).isEqualTo(1)
    }

    @Test
    fun `firstOpenQuizSection - null wenn alles bestanden`() {
        val l = lesson(section(quizBlock()))
        assertThat(InteractiveLessonLogic.firstOpenQuizSection(l, mapOf(0 to 60))).isNull()
    }

    @Test
    fun `firstOpenQuizSection - null wenn keine Quizzes existieren`() {
        val l = lesson(section(textBlock()))
        assertThat(InteractiveLessonLogic.firstOpenQuizSection(l, emptyMap())).isNull()
    }

    // ── scrollTargetForOpenQuiz ───────────────────────────────────────────

    @Test
    fun `scrollTarget - null wenn kein offenes Quiz (Button darf abschliessen)`() {
        val l = lesson(section(quizBlock()))
        assertThat(
            InteractiveLessonLogic.scrollTargetForOpenQuiz(l, mapOf(0 to 100), 1000)
        ).isNull()
    }

    @Test
    fun `scrollTarget - maxValue 0 (vor Layout) liefert 0 statt NaN` () {
        val l = lesson(section(quizBlock()), section(quizBlock()))
        assertThat(
            InteractiveLessonLogic.scrollTargetForOpenQuiz(l, emptyMap(), 0)
        ).isEqualTo(0)
    }

    @Test
    fun `scrollTarget - steigt monoton mit dem Section-Index`() {
        val l = lesson(
            section(quizBlock()),   // 0
            section(quizBlock()),   // 1
            section(quizBlock()),   // 2
        )
        val t0 = InteractiveLessonLogic.scrollTargetForOpenQuiz(l, mapOf(0 to 60, 1 to 100), 3000)!!
        val t1 = InteractiveLessonLogic.scrollTargetForOpenQuiz(l, mapOf(0 to 60), 3000)!!
        // je weiter rechts das offene Quiz liegt (mehr bestandene Sections davor),
        // desto weiter muss gescrollt werden: idx 2 -> 2/3*3000=2000; idx 1 -> 1/3*3000=1000
        assertThat(t0).isGreaterThan(t1)
    }

    @Test
    fun `scrollTarget - bleibt innerhalb des Scrollbereichs`() {
        val l = lesson(section(quizBlock()), section(quizBlock()))
        val t = InteractiveLessonLogic.scrollTargetForOpenQuiz(l, emptyMap(), 2000)!!
        assertThat(t).isAtLeast(0)
        assertThat(t).isAtMost(2000)
    }

    // ── Konstante ────────────────────────────────────────────────────────

    @Test
    fun `QUIZ_PASS_SCORE ist 60 (Vertrag mit Website und LessonDetail)`() {
        assertThat(InteractiveLessonLogic.QUIZ_PASS_SCORE).isEqualTo(60)
    }
}
