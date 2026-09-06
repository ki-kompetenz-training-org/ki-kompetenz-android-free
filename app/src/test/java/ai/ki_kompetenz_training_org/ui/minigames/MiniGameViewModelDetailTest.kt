/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.minigames

import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
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
import kotlin.random.Random

/**
 * Detail-Verträge des 2D-Fake-or-Echt-Spiels ([MiniGameViewModel]),
 * ergänzend zu MiniGameViewModelFakeOrRealTest (der die Session-Ziehung
 * und das perfekte Spiel abdeckt): hier die Guards, Teil-Ergebnisse,
 * XP-Formel-Enden und der Daily-Challenge-Bonus.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MiniGameViewModelDetailTest {

    private val gamification = mockk<GamificationRepository>(relaxed = true)
    private val dailyChallenge = mockk<DailyChallengeRepository>(relaxed = true)

    private val fakeGame = MiniGames.ALL.first { it.isFakeOrReal }
    private val otherGame = MiniGames.ALL.first { it.id != fakeGame.id }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(daily: DailyChallengeRepository? = null) =
        MiniGameViewModel(fakeGame, gamification, dailyChallengeRepository = daily, rng = Random(7))

    private fun playAll(vm: MiniGameViewModel, correct: Int) {
        repeat(vm.sessionRounds.size) { i ->
            val round = vm.sessionRounds[i]
            val pick = if (i < correct) round.correctIndex
            else (round.correctIndex + 1) % round.optionsDe.size
            vm.selectOption(pick)
            vm.next()
        }
    }

    // ── Guards ───────────────────────────────────────────────────────────

    @Test
    fun `Doppelklick auf dieselbe Frage wird ignoriert`() = runTest {
        val vm = viewModel()
        vm.selectOption(vm.sessionRounds[0].correctIndex)
        val after = vm.state.value

        vm.selectOption(0)

        assertThat(vm.state.value.answers).hasSize(1)
        assertThat(vm.state.value.selectedOption)
            .isEqualTo(after.selectedOption)
    }

    @Test
    fun `Antwort nach Spielende wird ignoriert (RESULT ist final)`() = runTest {
        val vm = viewModel()
        playAll(vm, correct = 10)
        assertThat(vm.state.value.phase).isEqualTo(GamePhase.RESULT)
        val answersAtEnd = vm.state.value.answers.size

        vm.selectOption(0)

        assertThat(vm.state.value.answers).hasSize(answersAtEnd)
    }

    @Test
    fun `next ohne Auswahl wechselt nicht die Frage`() = runTest {
        val vm = viewModel()

        vm.next()

        assertThat(vm.state.value.currentIndex).isEqualTo(0)
        assertThat(vm.state.value.phase).isEqualTo(GamePhase.PLAYING)
    }

    @Test
    fun `Antwortindex -1 wird als falsch gewertet wie im Quiz (Ueberspringen-Zweig)`() = runTest {
        val vm = viewModel()

        vm.selectOption(-1)

        assertThat(vm.state.value.selectedOption).isEqualTo(-1)
        assertThat(vm.state.value.answers).containsExactly(false)
    }

    @Test
    fun `falsche Antwort wird als false gespeichert und zaehlt nicht als Treffer`() = runTest {
        val vm = viewModel()
        val round = vm.sessionRounds[0]
        val wrong = (round.correctIndex + 1) % round.optionsDe.size

        vm.selectOption(wrong)

        assertThat(vm.state.value.answers).containsExactly(false)
    }

    // ── Teil-Ergebnis & XP ───────────────────────────────────────────────

    @Test
    fun `7 von 10 richtig - RESULT mit XP nach miniGameXp-Formel und onMiniGameFinished(7,10)`() = runTest {
        val vm = viewModel()
        playAll(vm, correct = 7)

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(GamePhase.RESULT)
        assertThat(s.answers.count { it }).isEqualTo(7)
        // Symbolisch: identisch zur VM-Formel (Float-Konvertierung ist
        // nuanciert, 20*0.7f kann 13 ODER 14 sein — das Formelverhalten
        // selbst ist in MiniGameXpTest verankert).
        assertThat(s.earnedXp).isEqualTo(
            GamificationRules.miniGameXp(7, 10, "INTERMEDIATE")
        )

        coVerify { gamification.onMiniGameFinished(7, 10, "fake_or_real") }
    }

    @Test
    fun `perfektes Spiel - 45 XP inklusive 25er-Bonus (INTERMEDIATE)`() = runTest {
        val vm = viewModel()
        playAll(vm, correct = 10)

        assertThat(vm.state.value.earnedXp).isEqualTo(45)
        // Formel-Kreuzpruefung: miniGameXp(10,10,INTERMEDIATE) == 20 + 25
        assertThat(GamificationRules.miniGameXp(10, 10, "INTERMEDIATE")).isEqualTo(45)
    }

    @Test
    fun `restart setzt auf frischen Spielstand zurueck`() = runTest {
        val vm = viewModel()
        playAll(vm, correct = 10)
        assertThat(vm.state.value.phase).isEqualTo(GamePhase.RESULT)

        vm.restart()

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(GamePhase.PLAYING)
        assertThat(s.currentIndex).isEqualTo(0)
        assertThat(s.answers).isEmpty()
        assertThat(s.earnedXp).isEqualTo(0)
        assertThat(s.dailyChallengeBonusXp).isEqualTo(0)
    }

    // ── Daily-Challenge-Bonus ────────────────────────────────────────────

    @Test
    fun `Bonus bei heutiger Challenge als dieses Spiel und noch nicht abgeschlossen - 30 XP werden gutgeschrieben`() = runTest {
        every { dailyChallenge.getTodayChallenge(any(), any()) } returns fakeGame
        every { dailyChallenge.isCompletedToday(any()) } returns false
        every { dailyChallenge.completeChallenge(any(), any()) } returns 30
        val vm = viewModel(daily = dailyChallenge)

        playAll(vm, correct = 10)

        coVerify { gamification.addXp(30) }
        assertThat(vm.state.value.dailyChallengeBonusXp).isEqualTo(30)
        assertThat(vm.state.value.earnedXp).isEqualTo(45) // Bonus additiv, nicht ersetzend
    }

    @Test
    fun `kein Bonus bei anderer heutiger Challenge`() = runTest {
        every { dailyChallenge.getTodayChallenge(any(), any()) } returns otherGame
        val vm = viewModel(daily = dailyChallenge)

        playAll(vm, correct = 10)

        coVerify(exactly = 0) { gamification.addXp(any()) }
        assertThat(vm.state.value.dailyChallengeBonusXp).isEqualTo(0)
        verify(exactly = 0) { dailyChallenge.completeChallenge(any(), any()) }
    }

    @Test
    fun `kein zweiter Bonus bei schon abgeschlossener Challenge`() = runTest {
        every { dailyChallenge.getTodayChallenge(any(), any()) } returns fakeGame
        every { dailyChallenge.isCompletedToday(any()) } returns true
        val vm = viewModel(daily = dailyChallenge)

        playAll(vm, correct = 10)

        coVerify(exactly = 0) { gamification.addXp(any()) }
        verify(exactly = 0) { dailyChallenge.completeChallenge(any(), any()) }
        assertThat(vm.state.value.dailyChallengeBonusXp).isEqualTo(0)
    }
}
