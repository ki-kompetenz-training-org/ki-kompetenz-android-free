/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * GAP-7: Die CognitiveLevel-Progression über alle 14 gebündelten Lektionen
 * muss monoton nicht-absteigend sein (FOUNDATION -> APPLICATION -> MASTERY).
 * L13 war fälschlich FOUNDATION hinter L12 (MASTERY).
 */
class CognitiveLevelTest {

    @Test
    fun `Enum-Reihenfolge ist FOUNDATION APPLICATION MASTERY`() {
        assertThat(CognitiveLevel.values().toList())
            .containsExactly(
                CognitiveLevel.FOUNDATION,
                CognitiveLevel.APPLICATION,
                CognitiveLevel.MASTERY,
            )
            .inOrder()
    }

    @Test
    fun `Lektionen 1 bis 8 sind FOUNDATION`() {
        BundledLessons.all
            .filter { it.lessonNumber in 1..8 }
            .forEach { lesson ->
                assertThat(lesson.cognitiveLevel).isEqualTo(CognitiveLevel.FOUNDATION)
            }
    }

    @Test
    fun `Lektionen 9 bis 14 sind APPLICATION oder MASTERY`() {
        BundledLessons.all
            .filter { it.lessonNumber in 9..14 }
            .forEach { lesson ->
                assertThat(lesson.cognitiveLevel).isAnyOf(
                    CognitiveLevel.APPLICATION,
                    CognitiveLevel.MASTERY,
                )
            }
    }

    @Test
    fun `keine Lektion nach MASTERY faellt auf FOUNDATION zurueck`() {
        var seenMastery = false
        BundledLessons.all.sortedBy { it.lessonNumber }.forEach { lesson ->
            if (lesson.cognitiveLevel == CognitiveLevel.MASTERY) {
                seenMastery = true
            } else if (seenMastery) {
                assertThat(lesson.cognitiveLevel).isNotEqualTo(CognitiveLevel.FOUNDATION)
            }
        }
    }
}
