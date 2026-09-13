/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames

import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.ui.minigames.MiniGameViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * Statische Minispiele (QUIZ + FAKE_OR_REAL) speisen den KIKI-Kompetenz-Index:
 * jede Antwort wird über [MasteryTracker.recordResult] in die primäre Domäne
 * des Spiels geschrieben. ADAPTIVE_QUIZ-Spiele tracken bereits über
 * AdaptiveQuizViewModel und behalten daher primaryDomain = "".
 */
class StaticGameCompetencyTest {

    private val expectedDomains = mapOf(
        "human_or_ai" to "Grundlagen der KI",
        "fact_or_hallucination" to "Grundlagen der KI",
        "high_risk_blitz" to "EU AI Act & Risikoklassen",
        "agent_ampel" to "KI-Governance im Unternehmen",
        "shadow_ai_check" to "KI-Governance im Unternehmen",
        "prompt_profis" to "KI-Tools im Arbeitsalltag",
        "bias_spotter" to "Transparenzpflichten",
        "dsgvo_check" to "Datenschutz & DSGVO",
        "fake_or_real" to "Grundlagen der KI",
    )

    @Test
    fun `alle 9 statischen Spiele haben eine primaere Domäne aus LiteracyBank`() {
        val static = MiniGames.ALL.filter { !it.isAdaptiveQuiz }

        assertThat(static.map { it.id }.sorted()).containsExactlyElementsIn(expectedDomains.keys.sorted())
        for (game in static) {
            assertThat(game.primaryDomain).isEqualTo(expectedDomains[game.id])
            assertThat(LiteracyBank.DOMAINS).contains(game.primaryDomain)
        }
    }

    @Test
    fun `adaptive Spiele behalten leere primaryDomain (Tracking laeuft ueber AdaptiveQuizViewModel)`() {
        assertThat(MiniGames.ADAPTIVE).hasSize(3)
        for (game in MiniGames.ADAPTIVE) {
            assertThat(game.primaryDomain).isEmpty()
        }
    }

    @Test
    fun `selectOption schreibt richtige und falsche Antworten in die primaere Domäne`() {
        val tracker = mockk<MasteryTracker>(relaxed = true)
        val game = MiniGames.byId("human_or_ai")!!
        val vm = MiniGameViewModel(
            game = game,
            gamification = mockk<GamificationRepository>(relaxed = true),
            masteryTracker = tracker,
        )

        vm.selectOption(vm.sessionRounds[0].correctIndex)
        val wrong = (vm.sessionRounds[1].correctIndex + 1) % vm.sessionRounds[1].optionsDe.size
        vm.next()
        vm.selectOption(wrong)

        verify { tracker.recordResult("Grundlagen der KI", true) }
        verify { tracker.recordResult("Grundlagen der KI", false) }
    }

    @Test
    fun `ohne primaere Domäne wird nichts getrackt`() {
        val tracker = mockk<MasteryTracker>(relaxed = true)
        val game = MiniGame(
            id = "test_no_domain", emoji = "❓",
            titleDe = "T", titleEn = "T",
            descriptionDe = "D", descriptionEn = "D",
            rounds = listOf(
                MiniGameRound(
                    promptDe = "P", promptEn = "P",
                    optionsDe = listOf("A", "B"), optionsEn = listOf("A", "B"),
                    correctIndex = 0,
                    explanationDe = "E", explanationEn = "E",
                ),
            ),
        )
        val vm = MiniGameViewModel(
            game = game,
            gamification = mockk<GamificationRepository>(relaxed = true),
            masteryTracker = tracker,
        )

        vm.selectOption(0)

        verify(exactly = 0) { tracker.recordResult(any(), any()) }
    }
}
