/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Win/Lose-Ende des Touch-native-3D-Minigames (JUnit4 + Truth).
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests fuer Spielende und won-Bewertung (GameRules.endGame + GameEngine).
 *
 * Regeln (Quelle: GameRules.kt / GameEngine.kt / GameTypes.kt):
 * - endGame(s, reason) setzt ended=true, endReason und won = (score >= target).
 *   ORB_HUNT: target = 250, maxHealth = 3, wrongPoints = 1.
 * - resolveDecision falsch: -1 Health; health <= 0 → endGame(HEALTH).
 * - Zeitablauf in stepGame → endGame(TIME) — Timeout-Ende kann trotzdem
 *   gewonnen sein, wenn das target erreicht wurde.
 * - endGame ist idempotent: der ERSTE Grund gewinnt, won wird nicht
 *   ueberschrieben (FIX 2026-09-01).
 * - stepGame nach s.ended ist ein No-Op (Frueh-Rueckkehr, keine Exception).
 *
 * Determinismus: rng {0.5} → isRisk = (0.5 < 0.5) = false, d. h. createState
 * mit emptyContent spawnt ausschliesslich FACT-Orbs. Eine RISK-Antwort auf
 * einen FACT-Orb ist damit garantiert falsch (Score bleibt 0 < target).
 */
class GameWinLoseTest {

    private lateinit var emptyContent: LiteracyContentProvider
    private val rng: () -> Double = { 0.5 }
    private val tuning = TouchTuning.STANDARD

    @Before
    fun setup() {
        emptyContent = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Fakt.", "Fact.", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Risiko.", "Risk.", "Test", true)
        }
    }

    private fun newState(): GameState =
        GameEngine.createState(GameMode.ORB_HUNT, emptyContent, rng, tuning)

    /**
     * Spielt drei garantiert falsche Klassifizierungen ueber die Engine:
     * Tap auf FACT-Orb (Index 0) + Antwort RISK. Falsche Orbs werden nicht
     * entfernt (nur korrekte), Index 0 bleibt also gueltig. Health 3 → 0.
     */
    private fun answerWrongThreeTimes(s: GameState) {
        repeat(3) {
            GameEngine.onAction(s, GameAction.TapEntity(0), emptyContent, rng, tuning)
            assertThat(s.pendingDecision).isNotNull()
            GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, rng, tuning)
        }
    }

    // ========== endGame: won = score >= target ==========

    @Test
    fun endGame_scoreExaktTarget_istGewonnen() {
        val s = newState()
        s.score = s.target
        GameRules.endGame(s, EndReason.TIME)
        assertThat(s.ended).isTrue()
        assertThat(s.won).isTrue()
    }

    @Test
    fun endGame_scoreEinPunktUnterTarget_istVerloren() {
        val s = newState()
        s.score = s.target - 1
        GameRules.endGame(s, EndReason.TIME)
        assertThat(s.ended).isTrue()
        assertThat(s.won).isFalse()
    }

    @Test
    fun endGame_scoreUeberTarget_istGewonnen() {
        val s = newState()
        s.score = s.target + 50
        GameRules.endGame(s, EndReason.HEALTH)
        assertThat(s.ended).isTrue()
        assertThat(s.won).isTrue()
    }

    // ========== Ende ueber die Engine: Health-Tod ==========

    @Test
    fun engine_dreiFalscheAntworten_beendetMitHealthUndVerloren() {
        val s = newState()
        assertThat(s.health).isEqualTo(3)

        answerWrongThreeTimes(s)

        assertThat(s.ended).isTrue()
        assertThat(s.endReason).isEqualTo(EndReason.HEALTH)
        assertThat(s.health).isEqualTo(0)
        // Drei falsche Antworten: Score 0 < target 250 → verloren.
        assertThat(s.score).isEqualTo(0)
        assertThat(s.won).isFalse()
        assertThat(s.classifications).hasSize(3)
    }

    // ========== Idempotenz: erster Grund gewinnt ==========

    @Test
    fun endGame_nachHealthEnde_zweiterAufrufMitTimeAenderaNichts() {
        val s = newState()
        answerWrongThreeTimes(s)
        assertThat(s.endReason).isEqualTo(EndReason.HEALTH)

        // REGRESSION (FIX 2026-09-01): Ein spaeterer endGame-Aufruf darf den
        // Grund UND die won-Bewertung nicht mehr ueberschreiben — selbst wenn
        // der Score zwischenzeitlich das target erreicht.
        s.score = s.target
        GameRules.endGame(s, EndReason.TIME)

        assertThat(s.endReason).isEqualTo(EndReason.HEALTH)
        assertThat(s.won).isFalse()
        assertThat(s.ended).isTrue()
    }

    // ========== Zustand nach Spielende eingefroren ==========

    @Test
    fun stepGame_nachSpielende_aendertZustandNichtMehr() {
        val s = newState()
        answerWrongThreeTimes(s)

        val scoreBefore = s.score
        val timeLeftBefore = s.timeLeft
        val collectiblesBefore = s.collectibles.toList()

        // Keine Exception, keine Zustaenderung (Frueh-Rueckkehr wegen ended):
        repeat(5) {
            GameEngine.stepGame(s, emptyContent, tuning, rng, 0.5)
        }

        assertThat(s.ended).isTrue()
        assertThat(s.endReason).isEqualTo(EndReason.HEALTH)
        assertThat(s.health).isEqualTo(0)
        assertThat(s.score).isEqualTo(scoreBefore)
        assertThat(s.timeLeft).isEqualTo(timeLeftBefore)
        assertThat(s.collectibles).hasSize(collectiblesBefore.size)
    }

    // ========== Zeitablauf mit erreichtem target ==========

    @Test
    fun engine_zeitablaufMitErreichtemTarget_istTrotzdemGewonnen() {
        val s = newState()
        s.score = s.target
        s.timeLeft = 0.05

        GameEngine.stepGame(s, emptyContent, tuning, rng, 0.1)

        assertThat(s.ended).isTrue()
        assertThat(s.endReason).isEqualTo(EndReason.TIME)
        assertThat(s.timeLeft).isEqualTo(0.0)
        // Timeout-Ende schliesst einen Sieg nicht aus (score >= target):
        assertThat(s.won).isTrue()
    }
}
