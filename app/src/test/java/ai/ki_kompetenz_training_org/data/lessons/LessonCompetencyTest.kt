/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import ai.ki_kompetenz_training_org.data.minigames3d.InMemoryPrefs
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * GAP-1: Lektions-Quiz-Ergebnisse fließen in den Kompetenz-Index
 * (MasteryTracker) ein. Jede gebündelte Lektion trägt eine
 * [InteractiveLesson.primaryDomain] aus [LiteracyBank.DOMAINS].
 */
class LessonCompetencyTest {

    @Test
    fun `alle 14 Lektionen haben eine nicht-leere primaryDomain aus LiteracyBank DOMAINS`() {
        assertThat(BundledLessons.all).hasSize(14)
        BundledLessons.all.forEach { lesson ->
            assertThat(lesson.primaryDomain).isNotEmpty()
            assertThat(LiteracyBank.DOMAINS).contains(lesson.primaryDomain)
        }
    }

    @Test
    fun `primaryDomain entspricht dem Thema der jeweiligen Lektion`() {
        val expected = mapOf(
            1 to "Grundlagen der KI",
            2 to "Grundlagen der KI",
            3 to "Grundlagen der KI",
            4 to "Datenschutz & DSGVO",
            5 to "Grundlagen der KI",
            6 to "KI-Tools im Arbeitsalltag",
            7 to "Grundlagen der KI",
            8 to "Erlaubte & verbotene Nutzung",
            9 to "EU AI Act & Risikoklassen",
            10 to "KI-Tools im Arbeitsalltag",
            11 to "KI-Governance im Unternehmen",
            12 to "Transparenzpflichten",
            13 to "Grundlagen der KI",
            14 to "KI-Tools im Arbeitsalltag",
        )
        BundledLessons.all.forEach { lesson ->
            assertThat(lesson.primaryDomain).isEqualTo(expected[lesson.lessonNumber])
        }
    }

    /** Spiegelt die Verdrahtung in InteractiveLessonScreen: score >= 70 gilt als bestanden. */
    private fun recordQuizScore(tracker: MasteryTracker, lesson: InteractiveLesson, score: Int) {
        if (lesson.primaryDomain.isNotEmpty()) {
            tracker.recordResult(lesson.primaryDomain, score >= 70)
        }
    }

    @Test
    fun `Quiz-Antwort wird mit der primaryDomain der Lektion im MasteryTracker vermerkt`() {
        val tracker = MasteryTracker(InMemoryPrefs())
        val lesson = BundledLessons.byId("lesson-4")!!

        recordQuizScore(tracker, lesson, 100) // bestanden
        recordQuizScore(tracker, lesson, 0) // nicht bestanden

        val mastery = tracker.getMastery("Datenschutz & DSGVO")
        assertThat(mastery.total).isEqualTo(2)
        assertThat(mastery.domain).isEqualTo("Datenschutz & DSGVO")
    }

    @Test
    fun `leere primaryDomain wird nicht getrackt`() {
        val tracker = MasteryTracker(InMemoryPrefs())
        val blank = BundledLessons.all.first().copy(primaryDomain = "")

        recordQuizScore(tracker, blank, 100)

        assertThat(tracker.getMastery(blank.primaryDomain).total).isEqualTo(0)
    }
}
