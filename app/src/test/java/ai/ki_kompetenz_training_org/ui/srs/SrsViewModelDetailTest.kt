/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.srs

import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
 * Detail-Verträge des SRS-Review-Flows ([SrsViewModel.rate],
 * [SrsViewModel.revealAnswer]) — ergänzend zu SrsViewModelTest (der Load-
 * und Fehlerpfade abdeckt).
 *
 * Wichtigster Vertrag: Das ViewModel reicht sessionFinished + Sessiongröße
 * korrekt an [GamificationRepository.onSrsReview] weiter — die
 * Bonus-Regel ("nur beendete Session mit >= 5 Karten") liegt im Repository
 * (GamificationRewardsTest), aber der FLAG-TRANSPORT von der letzten Karte
 * zum Repository ist dieser Test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SrsViewModelDetailTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val authRepository: AuthRepository = mockk()
    private val srsRepository: SrsRepository = mockk()
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.isLoggedIn() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun cards(n: Int) = (1..n).map { SrsCardDto(id = "c$it") }

    private fun viewModelN(n: Int): SrsViewModel {
        coEvery { srsRepository.getDueCards() } returns Result.success(cards(n))
        coEvery { srsRepository.postReview(any(), any()) } returns Result.success(mockk())
        return SrsViewModel(authRepository, srsRepository, gamificationRepository)
    }

    // ── reveal ───────────────────────────────────────────────────────────

    @Test
    fun `revealAnswer zeigt die Antwort - zweiter Aufruf ist idempotent`() = runTest {
        val vm = viewModelN(6)
        assertThat(vm.state.value.showAnswer).isFalse()

        vm.revealAnswer()
        assertThat(vm.state.value.showAnswer).isTrue()

        vm.revealAnswer() // bereits offen
        assertThat(vm.state.value.showAnswer).isTrue()
    }

    @Test
    fun `rate ohne vorheriges reveal wird ignoriert (showAnswer-Guard)`() = runTest {
        val vm = viewModelN(6)

        vm.rate(4)

        assertThat(vm.state.value.reviewsDone).isEqualTo(0)
        assertThat(vm.state.value.currentIndex).isEqualTo(0)
        coVerify(exactly = 0) { gamificationRepository.onSrsReview(any(), any()) }
    }

    // ── Rate mitten in der Session ───────────────────────────────────────

    @Test
    fun `rate mittendrin - naechste Karte, +1 Review, +5 XP, onSrsReview(false, 6)`() = runTest {
        val vm = viewModelN(6)
        vm.revealAnswer()
        vm.rate(4)

        val s = vm.state.value
        assertThat(s.currentIndex).isEqualTo(1)
        assertThat(s.reviewsDone).isEqualTo(1)
        assertThat(s.showAnswer).isFalse()
        assertThat(s.earnedXp).isEqualTo(5)
        coVerify { gamificationRepository.onSrsReview(sessionFinished = false, sessionSize = 6) }
    }

    // ── Session-Ende ─────────────────────────────────────────────────────

    @Test
    fun `letzte Karte (6 von 6) - FINISHED, 50 XP inklusive Session-Bonus, onSrsReview(true, 6)`() = runTest {
        val vm = viewModelN(6)

        repeat(6) {
            vm.revealAnswer()
            vm.rate(4)
        }

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(SrsPhase.FINISHED)
        assertThat(s.reviewsDone).isEqualTo(6)
        // 6 x 5 XP + 20 Session-Bonus
        assertThat(s.earnedXp).isEqualTo(50)
        coVerify { gamificationRepository.onSrsReview(sessionFinished = true, sessionSize = 6) }
    }

    @Test
    fun `kleine Session (3 Karten) beendet - FINISHED OHNE Bonus, onSrsReview(true, 3)`() = runTest {
        val vm = viewModelN(3)

        repeat(3) {
            vm.revealAnswer()
            vm.rate(4)
        }

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(SrsPhase.FINISHED)
        // 3 x 5 XP, KEIN Bonus (Sessiongroesse < 5)
        assertThat(s.earnedXp).isEqualTo(15)
        coVerify { gamificationRepository.onSrsReview(sessionFinished = true, sessionSize = 3) }
    }

    @Test
    fun `nach FINISHED blockt showAnswer=false weitere Bewertungen (es gibt keinen doppelten Bonus)`() = runTest {
        val vm = viewModelN(6)
        repeat(6) {
            vm.revealAnswer()
            vm.rate(4)
        }
        val xpAtFinish = vm.state.value.earnedXp

        vm.revealAnswer()
        vm.rate(4) // Versuch, nach Ende weiter zu bewerten

        assertThat(vm.state.value.earnedXp).isEqualTo(xpAtFinish)
        coVerify(exactly = 1) { gamificationRepository.onSrsReview(sessionFinished = true, sessionSize = 6) }
    }
}
