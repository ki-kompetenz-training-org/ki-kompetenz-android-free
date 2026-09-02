/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * XP- und End-of-Game-Tests für den touch-nativen MiniGame3DViewModel.
 *
 * computeXp-Formel (MiniGame3DViewModel):
 *   base        = (xpPerMiniGameWinBeginner * 2 * correct/total).toInt()
 *   streakBonus = min(classifyStreak * 2, 20)
 *   winBonus    = 15 bei Sieg, sonst 0
 *   XP          = (base + streakBonus + winBonus) mit Floor 10
 *
 * handleEnd (Spielende): mastery.recordClassifications(ClassifyLogs aus dem
 * GameState), danach XP- und Ergebnisberechnung — genau einmal (idempotent).
 */
package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.minigames3d.ModeConfig
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MiniGame3DXpTest {

    private lateinit var mastery: MasteryTracker
    private lateinit var gamification: GamificationRepository

    @Before
    fun setUp() {
        // handleEnd nutzt viewModelScope → Main-Dispatcher für JUnit setzen.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mastery = mockk(relaxed = true)
        // Relaxed-Mock liefert "" für selectDomain → MasteryBankContent fände
        // keine Statements (leerer Pool → mod(0)-Crash). Gültige Domain stubben:
        every { mastery.selectDomain(any()) } returns LiteracyBank.DOMAINS.first()
        gamification = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel(): MiniGame3DViewModel =
        MiniGame3DViewModel(GameMode.ORB_HUNT, gamification, mastery)

    /**
     * Frischer Arena-State; computeXp liest ausschließlich won und
     * classifyStreak, alle anderen Felder bleiben auf create()-Defaults.
     */
    private fun state(won: Boolean = false, classifyStreak: Int = 0): GameState {
        val s = GameState.create(GameMode.ORB_HUNT, ModeConfig.orbHunt())
        s.won = won
        s.classifyStreak = classifyStreak
        return s
    }

    // ========== computeXp: Win-Bonus ==========

    @Test
    fun computeXp_allCorrectWon_earnsMoreThanAllCorrectNotWon() {
        val vm = newViewModel()
        val xpWon = vm.computeXp(state(won = true), correct = 5, total = 5)
        val xpNotWon = vm.computeXp(state(won = false), correct = 5, total = 5)

        assertThat(xpWon).isGreaterThan(xpNotWon)
        // Deterministisch über dem Floor: exakt der Win-Bonus von 15 XP.
        assertThat(xpWon - xpNotWon).isEqualTo(15)
    }

    @Test
    fun computeXp_zeroOfFiveCorrect_returnsMinimumXpFloor() {
        val vm = newViewModel()
        // masteryShare 0 → base 0, kein Streak, kein Sieg: nur der Floor bleibt.
        val xp = vm.computeXp(state(won = false, classifyStreak = 0), correct = 0, total = 5)

        assertThat(xp).isEqualTo(10)
    }

    @Test
    fun computeXp_halfCorrect_liesStrictlyBetweenZeroAndAllCorrect() {
        val vm = newViewModel()
        val s = state() // identischer State → nur die Quote variiert
        val zero = vm.computeXp(s, correct = 0, total = 6)
        val half = vm.computeXp(s, correct = 3, total = 6)
        val all = vm.computeXp(s, correct = 6, total = 6)

        assertThat(half).isGreaterThan(zero)
        assertThat(half).isLessThan(all)
    }

    // ========== computeXp: Streak-Einfluss ==========

    @Test
    fun computeXp_higherStreak_neverEarnsLess_andAddsSixAboveFloor() {
        val vm = newViewModel()
        val xpStreak0 = vm.computeXp(state(classifyStreak = 0), correct = 5, total = 5)
        val xpStreak3 = vm.computeXp(state(classifyStreak = 3), correct = 5, total = 5)

        assertThat(xpStreak3).isAtLeast(xpStreak0)
        // Beide Werte liegen über dem Floor → deterministisch +6 (min(3*2, 20)).
        assertThat(xpStreak3 - xpStreak0).isEqualTo(6)
    }

    @Test
    fun computeXp_streakBonus_isCapped() {
        val vm = newViewModel()
        // 10 korrekte Streaks = Bonus 20 = Cap; 99 Streaks dürfen nicht mehr geben.
        val atCap = vm.computeXp(state(classifyStreak = 10), correct = 5, total = 5)
        val beyondCap = vm.computeXp(state(classifyStreak = 99), correct = 5, total = 5)

        assertThat(atCap).isEqualTo(beyondCap)
        val fullBase = GamificationRules.xpPerMiniGameWinBeginner * 2
        assertThat(beyondCap).isEqualTo(fullBase + 20)
    }

    @Test
    fun computeXp_totalZero_fallsBackToMinimumXp() {
        val vm = newViewModel()
        // total == 0 → masteryShare 0 → ohne Boni bleibt nur der Floor.
        val xp = vm.computeXp(state(won = false, classifyStreak = 0), correct = 0, total = 0)

        assertThat(xp).isEqualTo(10)
    }

    // ========== handleEnd: Mastery-Integration ==========

    @Test
    fun handleEnd_recordsStateClassificationsIntoMasteryTracker() {
        val vm = newViewModel()
        vm.start()
        val game = vm.game!!
        assertThat(game.ended).isFalse()

        // Eine korrekte Klassifikation über den echten Touch-Weg auslösen:
        // Tap auf Entity 0 → Decision → richtige Antwort (RISK/FACT passend
        // zum Disk, genau das konsultiert resolveDecision).
        vm.onTapEntity(0)
        val pending = vm.game!!.pendingDecision
        assertThat(pending).isNotNull()
        val answer = if (vm.game!!.collectibles[0].isRisk) ClassifyAction.RISK else ClassifyAction.FACT
        vm.onClassify(answer)

        val logged = vm.game!!.classifications.toList()
        assertThat(logged).hasSize(1)
        assertThat(logged.single().correct).isTrue()

        // Zeitablauf erzwingen, dann handleEnd über einen weiteren step triggern:
        vm.step(120.0)
        assertThat(vm.game!!.ended).isTrue()
        vm.step(0.016)

        val captured = slot<List<ClassifyLog>>()
        verify(exactly = 1) { mastery.recordClassifications(capture(captured)) }
        assertThat(captured.captured).isEqualTo(logged)

        // Ergebnis-Screen zeigt dieselben Zahlen wie computeXp für diesen State.
        val result = vm.state.value.result
        assertThat(result).isNotNull()
        assertThat(result!!.correct).isEqualTo(1)
        assertThat(result.total).isEqualTo(1)
        assertThat(result.earnedXp).isEqualTo(vm.computeXp(vm.game!!, correct = 1, total = 1))
    }

    @Test
    fun handleEnd_isIdempotent_repeatedStepsRecordOnlyOnce() {
        val vm = newViewModel()
        vm.start()
        vm.step(120.0) // Zeit läuft sofort ab → ended
        assertThat(vm.game!!.ended).isTrue()

        repeat(5) { vm.step(0.016) }

        verify(exactly = 1) { mastery.recordClassifications(any()) }
        assertThat(vm.state.value.phase).isEqualTo(ArenaPhase.RESULT)
    }
}
