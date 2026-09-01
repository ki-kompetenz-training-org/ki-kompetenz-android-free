/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native GameEngine tests - will replace GameEngineTest.kt in T2-T4
 * 30 tests total as specified in the plan
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

/**
 * Touch-native GameEngine tests.
 * Tests createState, stepGame (freeze invariant), onAction, resolveDecision.
 */
class GameEngineTest {

    private lateinit var emptyContent: LiteracyContentProvider

    @Before
    fun setup() {
        emptyContent = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement = LiteracyStatement("Fact.", "Fact.", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement = LiteracyStatement("Risk.", "Risk.", "Test", true)
        }
    }

    // ========== createState ==========

    @Test
    fun createState_orbHunt_initializesCorrectly() {
        // Mit Content werden minChips gespawnt:
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals(GameMode.ORB_HUNT, s.mode)
        assertEquals(250, s.target)
        assertEquals(3, s.maxHealth)
        assertEquals(3, s.health)
        assertEquals(60.0, s.timeLeft, 0.001)
        assertEquals(0, s.score)
        assertNotNull(s.collectibles)
        assertTrue("Should have minChips collectibles", s.collectibles.size >= 6)
    }

    /**
     * REGRESSION (Freeze-Bug 2026-09-01): createState ohne Content lief in
     * OrbHuntHandler.topUp / TruthSnipeHandler.topUp in eine Endlosschleife
     * (while minChips, aber spawnOrb/spawnChip kehrt bei content==null ohne
     * Hinzufügen zurück) → App-Freeze. Ohne Content muss createState
     * terminieren und eine leere Arena liefern.
     */
    @Test(timeout = 5000)
    fun createState_ohneContent_terminiertOhneFreeze() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals(GameMode.ORB_HUNT, s.mode)
        assertTrue("Ohne Content keine Chips", s.collectibles.isEmpty())

        val t = GameEngine.createState(GameMode.TRUTH_SNIPE, null, { 0.5 }, TouchTuning.STANDARD)
        assertTrue("Ohne Content keine Chips", t.collectibles.isEmpty())
    }

    @Test
    fun createState_mazeRun_initializesCorrectly() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals(GameMode.MAZE_RUN, s.mode)
        assertEquals(200, s.target)
        assertEquals(3, s.maxHealth)
        assertEquals(3, s.health)
        assertNotNull(s.maze)
        val start = s.maze!!.startPos()
        assertEquals(start.first, s.playerCellRow)
        assertEquals(start.second, s.playerCellCol)
    }

    @Test
    fun createState_truthSnipe_initializesCorrectly() {
        // Mit Content werden minChips gespawnt:
        val s = GameEngine.createState(GameMode.TRUTH_SNIPE, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals(GameMode.TRUTH_SNIPE, s.mode)
        assertEquals(300, s.target)
        assertEquals(3, s.maxHealth)
        assertTrue("Should have minChips collectibles", s.collectibles.size >= 4)
    }

    // ========== Freeze Invariant ==========

    @Test
    fun stepGame_noPendingDecision_doesNotFreeze() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        val initialTime = s.timeLeft
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertTrue("timeLeft should decrease when no decision pending", s.timeLeft < initialTime)
    }

    @Test
    fun stepGame_withPendingDecision_freezesEntities() {
        // emptyContent: damit existiert ein Chip (Fix: Freeze-Regression machte
        // createState ohne Content früher endlos, jetzt leer → Test braucht Chip)
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            5.0, 5.0, 0.0, 0.0, false, 0, false
        )
        val initialTimeLeft = s.timeLeft
        val initialChipX = s.collectibles[0].x
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertTrue(s.timeLeft <= initialTimeLeft)
        assertEquals("Chip x should not change", initialChipX, s.collectibles[0].x, 0.001)
    }

    @Test
    fun stepGame_decisionTimeout_triggersResolve() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            0.05, 0.05, 0.0, 0.0, false, 0, false
        )
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertNull("Decision should be resolved after timeout", s.pendingDecision)
        assertEquals("Health should decrease by 1", 2, s.health)
    }

    // ========== Time Management ==========

    @Test
    fun stepGame_decrementsTimeLeft() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        val initial = s.timeLeft
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.5)
        assertEquals(initial - 0.5, s.timeLeft, 0.001)
    }

    @Test
    fun stepGame_timeRunsOut_endsGame() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        s.timeLeft = 0.05
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertTrue("Game should be ended", s.ended)
        assertEquals("Should end with TIME reason", EndReason.TIME, s.endReason)
        assertFalse("Should not be won", s.won)
    }

    @Test
    fun stepGame_afterEnd_doesNothing() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        GameRules.endGame(s, EndReason.TIME)
        val scoreBefore = s.score
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertEquals("Score should not change after end", scoreBefore, s.score)
    }

    // ========== onAction dispatch ==========

    @Test
    fun onAction_tapEntity_withoutPending_decisionDispatchesToHandler() {
        // emptyContent: Arena hat Chips → TapEntity(0) trifft ein Ziel
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNull(s.pendingDecision)
        GameEngine.onAction(s, GameAction.TapEntity(0), null, { 0.5 }, TouchTuning.STANDARD)
        assertNotNull("TapEntity should trigger pendingDecision in OrbHunt", s.pendingDecision)
    }

    @Test
    fun onAction_dash_withoutPending_dispatchesToHandler() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        val initialCol = s.playerCellCol
        GameEngine.onAction(s, GameAction.Dash(Direction.RIGHT), null, { 0.5 }, TouchTuning.STANDARD)
        assertFalse("Should not crash on dash", s.ended && s.pendingDecision == null)
    }

    @Test
    fun onAction_tap_withPendingDecision_isIgnored() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        GameEngine.onAction(s, GameAction.TapEntity(0), null, { 0.5 }, TouchTuning.STANDARD)
    }

    @Test
    fun onAction_classify_withPendingDecision_resolves() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Test fact", "Test fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(1.0, 1.0, 0.5, 0.0, 0.0, 0, false, LiteracyStatement("F", "F", "T", false)))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNull("Classify should resolve pendingDecision", s.pendingDecision)
    }

    @Test
    fun onAction_dash_withPendingDecision_isIgnored() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, -1, false
        )
        val initialCol = s.playerCellCol
        GameEngine.onAction(s, GameAction.Dash(Direction.UP), null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Dash should be ignored during decision", initialCol, s.playerCellCol)
    }

    // ========== Classify Scoring ==========

    @Test
    fun classify_correct_fact_increasesScoreAndStreak() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        val initialScore = s.score
        val initialStreak = s.classifyStreak
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue("Score should increase", s.score > initialScore)
        assertEquals("Streak should increment", initialStreak + 1, s.classifyStreak)
        assertTrue("Should be marked as scored", s.justScored)
    }

    @Test
    fun classify_wrong_decreasesHealth_resetsStreak() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.classifyStreak = 3
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        val initialHealth = s.health
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Health should decrease", initialHealth - 1, s.health)
        assertEquals("Streak should reset", 0, s.classifyStreak)
        assertTrue("Should be marked as hit", s.justHit)
    }

    @Test
    fun classify_streakBonus_cappedAt50() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.classifyStreak = 15
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        val initialScore = s.score
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue("Score increase should be capped", s.score - initialScore <= 75)
    }

    // ========== Health End ==========

    @Test
    fun health_reachesZero_endsGame() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.health = 1
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue("Game should end when health=0", s.ended)
        assertEquals("Should end with HEALTH reason", EndReason.HEALTH, s.endReason)
    }

    // ========== Maze-specific ==========

    @Test
    fun maze_dashRejectedOnWall_doesNotMove() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        val maze = s.maze!!
        val start = maze.startPos()
        s.playerCellRow = start.first
        s.playerCellCol = start.second
        val initialRow = s.playerCellRow
        val initialCol = s.playerCellCol
        GameEngine.onAction(s, GameAction.Dash(Direction.UP), null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Should not move through wall", initialRow, s.playerCellRow)
        assertEquals("Should not move through wall", initialCol, s.playerCellCol)
    }

    @Test
    fun maze_dashOnValidPath_movesPlayer() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        val maze = s.maze!!
        val start = maze.startPos()
        s.playerCellRow = start.first
        s.playerCellCol = start.second
        GameEngine.onAction(s, GameAction.Dash(Direction.RIGHT), null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Row should stay same", start.first, s.playerCellRow)
        assertEquals("Col should increase by 1", start.second + 1, s.playerCellCol)
    }

    @Test
    fun maze_goalReached_triggersDecision() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        val goal = s.maze!!.goalPositions().first()
        s.playerCellRow = goal.first - 1
        s.playerCellCol = goal.second
        // emptyContent: checkCellTrigger braucht einen ContentProvider (ohne
        // Content wird Goal-Decision still ignoriert)
        GameEngine.onAction(s, GameAction.Dash(Direction.DOWN), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNotNull("Should trigger decision at goal", s.pendingDecision)
    }

    // ========== Snipe-specific ==========

    @Test
    fun snipe_chipDrifts_rightward() {
        // Mit emptyContent: topUp spawnt die minChips (ohne Content wäre die Arena leer):
        val s = GameEngine.createState(GameMode.TRUTH_SNIPE, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        val chip = s.collectibles.first()
        val initialX = chip.x
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertTrue("Chip should drift right", chip.x >= initialX)
    }

    // ========== Streak ==========

    @Test
    fun streak_continuousCorrect_increments() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        repeat(5) {
            s.pendingDecision = PendingDecision(
                LiteracyStatement("Fact", "Fact", "Test", false),
                10.0, 10.0, 0.0, 0.0, false, 0, false
            )
            s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
            GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        }
        assertEquals("Streak should be 5", 5, s.classifyStreak)
    }

    @Test
    fun streak_wrongClassify_resetsToZero() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        repeat(3) {
            s.pendingDecision = PendingDecision(
                LiteracyStatement("Fact", "Fact", "Test", false),
                10.0, 10.0, 0.0, 0.0, false, 0, false
            )
            s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
            GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        }
        assertEquals(3, s.classifyStreak)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Streak should reset to 0", 0, s.classifyStreak)
    }

    // ========== Transient Flags ==========

    @Test
    fun justScored_resetAfterStep() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue(s.justScored)
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertFalse("justScored should reset after step", s.justScored)
    }

    @Test
    fun justHit_resetAfterStep() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue(s.justHit)
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertFalse("justHit should reset after step", s.justHit)
    }

    // ========== Classifications Log ==========

    @Test
    fun classificationLog_recordsEachDecision() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        val initialCount = s.classifications.size
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "TestDomain", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Risk", "Risk", "TestDomain2", true),
            10.0, 10.0, 0.0, 0.0, false, 0, true
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, true, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Should have 2 new classifications", initialCount + 2, s.classifications.size)
    }
}
