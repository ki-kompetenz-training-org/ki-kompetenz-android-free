/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * TruthSnipeHandler unit tests — dispatched via taskfleet (TEST-SNIPE).
 * Covers: spawn semantics, rightward drift, tap-to-decision, classify
 * correct/wrong, topUp refill + null-content freeze guard (regression).
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TruthSnipeHandlerTest {

    private lateinit var emptyContent: LiteracyContentProvider
    private lateinit var cfg: ModeConfig

    @Before
    fun setup() {
        emptyContent = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Fact.", "Fact.", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Risk.", "Risk.", "Test", true)
        }
        cfg = GameConfig.getModeConfig(GameMode.TRUTH_SNIPE)
    }

    /** rng = { 0.5 } → isRisk = (0.5 < 0.5) = false → alle Chips sind FACTs. */
    private fun stateWithChips(): GameState =
        GameEngine.createState(GameMode.TRUTH_SNIPE, emptyContent, { 0.5 }, TouchTuning.STANDARD)

    // ========== Spawn-Semantik ==========

    @Test
    fun createState_spawntGenauMinChips() {
        val s = stateWithChips()
        assertThat(s.collectibles).hasSize(cfg.minChips)
        assertThat(cfg.minChips).isEqualTo(4)
    }

    @Test
    fun gespawnteChips_habenStatementsUndPositionAmLinkenRand() {
        val s = stateWithChips()
        s.collectibles.forEach { chip ->
            assertThat(chip.statement).isNotNull()
            assertThat(chip.x).isWithin(1e-9).of(-cfg.arenaRadius + cfg.chipRadius)
            assertThat(chip.r).isWithin(1e-9).of(cfg.chipRadius)
        }
    }

    // ========== Drift ==========

    @Test
    fun step_verschiebtChipsDeterministischNachRechts() {
        val s = stateWithChips()
        val before = s.collectibles.map { it.x }
        TruthSnipeHandler.step(s, cfg, TouchTuning.STANDARD, null, { 0.5 }, dt = 0.5)
        // speed = tuning.speedMultiplier * cfg.chipSpeed; x += speed * dt
        s.collectibles.forEachIndexed { i, chip ->
            assertThat(chip.x).isAtLeast(before[i])
        }
        assertThat(s.collectibles[0].x).isGreaterThan(before[0])
    }

    @Test
    fun step_entferntChipsUeberLebensdauerUndFülltNichtOhneContent() {
        val s = stateWithChips()
        s.collectibles.forEach { it.age = cfg.chipLifetime + 1.0 }
        // content = null → topUp early-return → keine Neufüllung:
        TruthSnipeHandler.step(s, cfg, TouchTuning.STANDARD, null, { 0.5 }, dt = 0.1)
        assertThat(s.collectibles).isEmpty()
    }

    // ========== Tap-to-Decision ==========

    @Test
    fun onTap_setztPendingDecisionMitChipStatement() {
        val s = stateWithChips()
        val chip = s.collectibles[0]
        TruthSnipeHandler.onTap(s, cfg, 0, emptyContent, { 0.5 })
        val pd = s.pendingDecision
        assertThat(pd).isNotNull()
        assertThat(pd!!.diskIndex).isEqualTo(0)
        assertThat(pd.statement).isSameInstanceAs(chip.statement)
        assertThat(pd.isRisk).isEqualTo(chip.isRisk)
        assertThat(pd.timerMax).isWithin(1e-9).of(cfg.decisionSeconds)
        assertThat(pd.timer).isWithin(1e-9).of(cfg.decisionSeconds)
        assertThat(pd.fromBonus).isFalse()
    }

    @Test
    fun onTap_ungueltigeIndizes_machenNichts() {
        val s = stateWithChips()
        TruthSnipeHandler.onTap(s, cfg, 99, emptyContent, { 0.5 })
        assertThat(s.pendingDecision).isNull()
        TruthSnipeHandler.onTap(s, cfg, -1, emptyContent, { 0.5 })
        assertThat(s.pendingDecision).isNull()
    }

    @Test
    fun onTap_ohneChips_machtNichts() {
        // Freeze-Guard-Regression: ohne Content ist die Arena leer (createState
        // terminiert), Tap auf Index 0 darf nicht crashen:
        val s = GameEngine.createState(GameMode.TRUTH_SNIPE, null, { 0.5 }, TouchTuning.STANDARD)
        TruthSnipeHandler.onTap(s, cfg, 0, null, { 0.5 })
        assertThat(s.pendingDecision).isNull()
    }

    // ========== Classify korrekt/falsch ==========

    @Test
    fun resolveKorrekt_entferntChipVergibtPunkte() {
        val s = stateWithChips()
        val sizeBefore = s.collectibles.size // 4
        TruthSnipeHandler.onTap(s, cfg, 0, emptyContent, { 0.5 })
        // FACT-Chip als FACT klassifiziert = korrekt:
        GameEngine.resolveDecision(s, ClassifyAction.FACT, emptyContent, { 0.5 }, cfg, TouchTuning.STANDARD)
        assertThat(s.collectibles).hasSize(sizeBefore - 1)
        assertThat(s.score).isEqualTo(GameConfig.TRUTH_SNIPE_FACT_POINTS)
        assertThat(s.health).isEqualTo(3)
        assertThat(s.classifyStreak).isEqualTo(1)
        assertThat(s.pendingDecision).isNull()
    }

    @Test
    fun resolveFalsch_verringertHealthUndBehaeltChip() {
        val s = stateWithChips()
        val sizeBefore = s.collectibles.size
        TruthSnipeHandler.onTap(s, cfg, 0, emptyContent, { 0.5 })
        // FACT-Chip als RISK klassifiziert = falsch:
        GameEngine.resolveDecision(s, ClassifyAction.RISK, emptyContent, { 0.5 }, cfg, TouchTuning.STANDARD)
        assertThat(s.collectibles).hasSize(sizeBefore)
        assertThat(s.score).isEqualTo(0)
        assertThat(s.health).isEqualTo(2) // wrongPoints = 1
        assertThat(s.classifyStreak).isEqualTo(0)
    }

    // ========== topUp ==========

    @Test(timeout = 5000)
    fun topUp_fuelltArenaWiederAufMinChips() {
        val s = stateWithChips()
        s.collectibles.removeAt(0)
        s.collectibles.removeAt(0)
        assertThat(s.collectibles).hasSize(cfg.minChips - 2)
        TruthSnipeHandler.topUp(s, cfg, TouchTuning.STANDARD, { 0.5 }, emptyContent)
        assertThat(s.collectibles).hasSize(cfg.minChips)
    }

    /**
     * REGRESSION (Freeze-Bug 2026-09-01): topUp mit content == null kehrte
     * nicht zurück, weil spawnChip bei null early-returnt und die
     * while-Schleife endlos lief → App-Freeze. Muss terminieren und leer
     * bleiben.
     */
    @Test(timeout = 5000)
    fun topUp_ohneContent_terminiertOhneFreeze() {
        val s = GameEngine.createState(GameMode.TRUTH_SNIPE, null, { 0.5 }, TouchTuning.STANDARD)
        TruthSnipeHandler.topUp(s, cfg, TouchTuning.STANDARD, { 0.5 }, null)
        assertThat(s.collectibles).isEmpty()
    }
}
