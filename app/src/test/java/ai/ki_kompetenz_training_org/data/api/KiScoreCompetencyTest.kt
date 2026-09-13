/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.api

import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.ui.quiz.QuizViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * KI-Score -> KIKI-Kompetenzindex: Jede Quiz-Antwort muss in den
 * [MasteryTracker] fliessen. Vertrag:
 * 1. Alle 50 Offline-Fragen haben eine nicht-leere Domain aus den
 *    9 [LiteracyBank.DOMAINS].
 * 2. selectOption() ruft recordResult(domain, correct) auf.
 * 3. Fragen OHNE Domain (API-Antworten ohne Feld) werden ignoriert —
 *    Default "" haelt die Deserialisierung kompatibel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KiScoreCompetencyTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val contentRepository: ContentRepository = mockk()
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)
    private val db: AppDatabase = mockk(relaxed = true)
    private val masteryTracker: MasteryTracker = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Fallback-Pool: Domain-Abdeckung ──────────────────────────────────

    @Test
    fun `alle 50 Offline-Fragen haben eine nicht-leere Domain`() {
        assertThat(KiScoreFallback.data.questions).hasSize(50)
        val withoutDomain = KiScoreFallback.data.questions.filter { it.domain.isEmpty() }
        assertThat(withoutDomain).isEmpty()
    }

    @Test
    fun `jede Frage-Domain ist eine der 9 LiteracyBank-Domains`() {
        val invalid = KiScoreFallback.data.questions
            .filter { it.domain !in LiteracyBank.DOMAINS }
            .map { "${it.id}: '${it.domain}'" }
        assertThat(invalid).isEmpty()
    }

    // ── selectOption -> MasteryTracker ───────────────────────────────────

    private fun question(id: Int, correct: Int, domain: String) = KiScoreQuestionDto(
        id = id,
        text = "t",
        options = listOf("a", "b", "c", "d"),
        correct = correct,
        explanation = "e",
        emoji = "❓",
        domain = domain,
    )

    private fun viewModelWith(first: KiScoreQuestionDto): QuizViewModel {
        coEvery { contentRepository.fetchKiScoreData() } returns Result.success(
            KiScoreDataDto(questions = listOf(first), tiers = emptyList(), share = null),
        )
        val vm = QuizViewModel(contentRepository, db, gamificationRepository, masteryTracker)
        vm.start()
        return vm
    }

    @Test
    fun `richtige Antwort wird mit Frage-Domain im MasteryTracker erfasst`() = runTest {
        val vm = viewModelWith(question(1, correct = 2, domain = "EU AI Act & Risikoklassen"))

        vm.selectOption(2)

        verify(exactly = 1) { masteryTracker.recordResult("EU AI Act & Risikoklassen", true) }
    }

    @Test
    fun `falsche Antwort wird mit false erfasst`() = runTest {
        val vm = viewModelWith(question(2, correct = 0, domain = "Datenschutz & DSGVO"))

        vm.selectOption(3)

        verify(exactly = 1) { masteryTracker.recordResult("Datenschutz & DSGVO", false) }
    }

    @Test
    fun `Frage ohne Domain (API-Kompatibilitaet) wird nicht erfasst`() = runTest {
        val vm = viewModelWith(question(3, correct = 1, domain = ""))

        vm.selectOption(1)

        verify(exactly = 0) { masteryTracker.recordResult(any(), any()) }
    }
}
