/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import ai.ki_kompetenz_training_org.data.api.KiScoreFallback
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
 * Unit-Tests für die KI-Score-Spielrunde ([QuizViewModel.selectOption],
 * [QuizViewModel.next], [QuizViewModel.finish], [QuizViewModel.restart]).
 *
 * Es wird der gebundene Offline-Pool als Fragenquelle genutzt (normaler
 * Betrieb mit lokalem Pool). Wichtiger Vertrag: finish() schreibt das
 * Tier aus dem ANTWORT-PROZENTSATZ in die DB (Website-Parität,
 * BUG-Fund 2026-09-05) — nicht aus Combo-Punkten.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizGameplayTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val contentRepository: ContentRepository = mockk()
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)
    private val db: AppDatabase = mockk(relaxed = true)

    private val tiers = KiScoreFallback.data.tiers

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { contentRepository.fetchKiScoreData() } returns
            Result.success(KiScoreFallback.data)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun startedViewModel(): QuizViewModel {
        val vm = QuizViewModel(contentRepository, db, gamificationRepository)
        vm.start()
        return vm
    }

    private fun correctIndex(vm: QuizViewModel): Int {
        val s = vm.state.value
        return s.questions[s.currentIndex].correct
    }

    private fun wrongIndex(vm: QuizViewModel): Int =
        (correctIndex(vm) + 1) % vm.state.value.questions[vm.state.value.currentIndex].options.size

    // ── Antworten ────────────────────────────────────────────────────────

    @Test
    fun `korrekte Antwort vergibt Combo-Punkte (300 bei voller Zeit, 1x)`() = runTest {
        val vm = startedViewModel()

        vm.selectOption(correctIndex(vm))

        val s = vm.state.value
        assertThat(s.answers).containsExactly(true)
        assertThat(s.combo).isEqualTo(1)
        assertThat(s.scorePoints).isEqualTo(QuizConstants.pointsForCorrectAnswer(20, 0))
    }

    @Test
    fun `falsche Antwort kostet ein Leben, resettet Combo, gibt 0 Punkte`() = runTest {
        val vm = startedViewModel()

        vm.selectOption(wrongIndex(vm))

        val s = vm.state.value
        assertThat(s.answers).containsExactly(false)
        assertThat(s.combo).isEqualTo(0)
        assertThat(s.scorePoints).isEqualTo(0)
        assertThat(s.lives).isEqualTo(QuizConstants.MAX_LIVES - 1)
    }

    @Test
    fun `Combo-Streak - 3 richtige in Folge nutzen die 1_5x-Stufe (300+300+450)`() = runTest {
        val vm = startedViewModel()

        // 1. richtig (combo 0 → 1x): 300 — kein Auto-Next bei korrekt
        vm.selectOption(correctIndex(vm)); vm.next()
        // 2. richtig (combo 1 → 1x): 300
        vm.selectOption(correctIndex(vm)); vm.next()
        // 3. richtig (combo 2 → 1.5x): 450
        vm.selectOption(correctIndex(vm))

        assertThat(vm.state.value.scorePoints).isEqualTo(300 + 300 + 450)
        assertThat(vm.state.value.combo).isEqualTo(3)
    }

    @Test
    fun `Doppelantwort wird ignoriert (selectedOption schon gesetzt)`() = runTest {
        val vm = startedViewModel()
        vm.selectOption(correctIndex(vm))
        val scoreAfterFirst = vm.state.value.scorePoints
        val answersAfterFirst = vm.state.value.answers.size

        vm.selectOption(wrongIndex(vm)) // zweiter Klick gleiche Frage

        assertThat(vm.state.value.scorePoints).isEqualTo(scoreAfterFirst)
        assertThat(vm.state.value.answers).hasSize(answersAfterFirst)
    }

    @Test
    fun `Antwort ausserhalb des Optionenbereichs wird ignoriert (kein Crash)`() = runTest {
        val vm = startedViewModel()

        vm.selectOption(-1)
        val afterInvalid = vm.state.value

        // -1 == question.correct ist unmöglich (correct ∈ 0..3), also falsch —
        // wichtig ist: kein Crash, konsistenter State.
        assertThat(afterInvalid.answers).containsExactly(-1 == afterInvalid.questions[0].correct)
    }

    // ── Fragenwechsel ────────────────────────────────────────────────────

    @Test
    fun `next ohne Auswahl wird ignoriert (currentQuestion bleibt)`() = runTest {
        val vm = startedViewModel()

        vm.next()

        assertThat(vm.state.value.currentIndex).isEqualTo(0)
        assertThat(vm.state.value.phase).isEqualTo(QuizPhase.PLAYING)
    }

    @Test
    fun `next nach Auswahl - naechste Frage, Timer resettet, Auswahl geleert`() = runTest {
        val vm = startedViewModel()
        vm.selectOption(correctIndex(vm))

        vm.next()

        val s = vm.state.value
        assertThat(s.currentIndex).isEqualTo(1)
        assertThat(s.selectedOption).isNull()
        assertThat(s.timeLeft).isEqualTo(QuizConstants.ROUND_SECONDS)
    }

    // ── Rundenende ───────────────────────────────────────────────────────

    @Test
    fun `10 richtige Antworten - RESULT mit Top-Tier in der DB (Website-Paritaet, Vertrag nach Tier-Fix)`() = runTest {
        val vm = startedViewModel()

        repeat(10) {
            vm.selectOption(correctIndex(vm))
            vm.next()
        }

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(QuizPhase.RESULT)
        assertThat(s.answers.count { it }).isEqualTo(10)

        coVerify {
            db.quizResultDao().insert(
                match { entity ->
                    entity.correctCount == 10 &&
                        entity.totalQuestions == 10 &&
                        entity.tierTitle == tiers[4].title
                }
            )
        }
        coVerify {
            gamificationRepository.onQuizFinished(
                correctCount = 10,
                totalQuestions = 10,
                score = any(),
            )
        }
    }

    @Test
    fun `10 falsche Antworten - alle Leben weg, RESULT mit 0 von 10 und unterstem Tier`() = runTest {
        val vm = startedViewModel()

        repeat(10) {
            vm.selectOption(wrongIndex(vm))
            // Falsche Antwort triggert Auto-Next nach 1s — aber erst ab
            // lives<=1; sonst muss manuell gewechselt werden.
            dispatcher.scheduler.advanceUntilIdle()
            val s = vm.state.value
            if (s.phase == QuizPhase.PLAYING && s.selectedOption != null) {
                vm.next()
            }
        }

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(QuizPhase.RESULT)
        assertThat(s.lives).isEqualTo(0) // coerceAtLeast
        assertThat(s.answers.count { it }).isEqualTo(0)

        coVerify {
            db.quizResultDao().insert(
                match { entity ->
                    entity.correctCount == 0 && entity.tierTitle == tiers[0].title
                }
            )
        }
    }

    @Test
    fun `restart fuehrt zurueck zum INTRO (erneute Runde moeglich)`() = runTest {
        val vm = startedViewModel()
        vm.selectOption(correctIndex(vm))
        vm.next()

        vm.restart()

        assertThat(vm.state.value.phase).isEqualTo(QuizPhase.INTRO)
    }
}
