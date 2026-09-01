/*
 * Copyright 2026 Tobias Weiss
import kotlin.math.abs
 * Touch-native MiniGame3DViewModel tests - will replace MiniGame3DViewModelTest.kt in T2-T4
 * 14 tests total as specified in the plan
 */
package ai.ki_kompetenz_training_org.ui.minigames3d
import kotlin.math.abs

import ai.ki_kompetenz_training_org.data.minigames3d.Direction
import ai.ki_kompetenz_training_org.data.minigames3d.GameAction
import ai.ki_kompetenz_training_org.data.minigames3d.GameEngine
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyContentProvider
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyStatement
import ai.ki_kompetenz_training_org.data.minigames3d.PendingDecision
import ai.ki_kompetenz_training_org.data.minigames3d.TouchTuning
import ai.ki_kompetenz_training_org.data.minigames3d.Disk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Touch-native MiniGame3DViewModel tests.
 * Note: These tests use GameEngine directly since the touch-native ViewModel
 * is not yet implemented. They test the touch-native concepts.
 */
class MiniGame3DViewModelTest {

    private lateinit var emptyContent: LiteracyContentProvider

    @Before
    fun setup() {
        emptyContent = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement = LiteracyStatement("Fact.", "Fact.", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement = LiteracyStatement("Risk.", "Risk.", "Test", true)
        }
    }

    // ========== Touch Actions ==========

    @Test
    fun onTapEntity_triggersPendingDecision_orbHunt() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNull(s.pendingDecision)
        GameEngine.onAction(s, GameAction.TapEntity(0), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNotNull("TapEntity should trigger pendingDecision in OrbHunt", s.pendingDecision)
    }

    @Test
    fun onDash_movesPlayer_mazeRun() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, null, { 0.5 }, TouchTuning.STANDARD)
        val initialRow = s.playerCellRow
        val initialCol = s.playerCellCol
        GameEngine.onAction(s, GameAction.Dash(Direction.RIGHT), null, { 0.5 }, TouchTuning.STANDARD)
        assertFalse("Player should move on valid dash", 
            initialRow == s.playerCellRow && initialCol == s.playerCellCol)
    }

    @Test
    fun onDash_rejectedOnWall() {
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

    // ========== Classify Actions ==========

    @Test
    fun classify_correctFact_increasesScore() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        val initialScore = s.score
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertTrue("Score should increase on correct classify", s.score > initialScore)
    }

    @Test
    fun classify_wrongRisk_decreasesHealth() {
        // null-Content: leere Arena → der manuelle Disk ist wirklich Index 0
        // (mit emptyContent spawnt topUp minChips und diskIndex=0 träfe den
        // falschen, gespawnten Chip → Entscheidung wäre fälschlich korrekt)
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Risk", "Risk", "Test", true),
            10.0, 10.0, 0.0, 0.0, false, 0, true
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, true, s.pendingDecision!!.statement))
        val initialHealth = s.health
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Health should decrease on wrong classify", initialHealth - 1, s.health)
    }

    // ========== Freeze Invariant ==========

    @Test
    fun touchActions_ignoredWhilePendingDecision() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        val initialChipX = s.collectibles[0].x
        GameEngine.onAction(s, GameAction.TapEntity(0), null, { 0.5 }, TouchTuning.STANDARD)
        GameEngine.onAction(s, GameAction.Dash(Direction.LEFT), null, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Chip should not change during pending decision", initialChipX, s.collectibles[0].x, 0.001)
    }

    @Test
    fun onlyClassify_allowedDuringPendingDecision() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertNull("Classify should resolve pending decision", s.pendingDecision)
    }

    // ========== Streak ==========

    @Test
    fun classify_correct_continuousIncreasesStreak() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        repeat(3) {
            s.pendingDecision = PendingDecision(
                LiteracyStatement("Fact", "Fact", "Test", false),
                10.0, 10.0, 0.0, 0.0, false, 0, false
            )
            s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
            GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        }
        assertEquals("Streak should be 3", 3, s.classifyStreak)
    }

    @Test
    fun classify_wrong_resetsStreak() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        repeat(5) {
            s.pendingDecision = PendingDecision(
                LiteracyStatement("Fact", "Fact", "Test", false),
                10.0, 10.0, 0.0, 0.0, false, 0, false
            )
            s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
            GameEngine.onAction(s, GameAction.Classify(ClassifyAction.FACT), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        }
        assertEquals(5, s.classifyStreak)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("Fact", "Fact", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        s.collectibles.add(Disk(0.0, 0.0, 0.5, 0.0, 0.0, 0, false, s.pendingDecision!!.statement))
        GameEngine.onAction(s, GameAction.Classify(ClassifyAction.RISK), emptyContent, { 0.5 }, TouchTuning.STANDARD)
        assertEquals("Streak should reset to 0", 0, s.classifyStreak)
    }

    // ========== Game State ==========

    @Test
    fun stepGame_entitiesMoveWithNoPendingDecision() {
        // emptyContent: topUp spawnt Chips, die drift-animiert werden
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        val initialX = s.collectibles[0].x
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertFalse("Entities should move when no decision pending", 
            abs(s.collectibles[0].x - initialX) < 0.001)
    }

    @Test
    fun stepGame_entitiesFreezeWithPendingDecision() {
        // emptyContent: damit existiert ein beobachtbarer Chip während der Decision
        val s = GameEngine.createState(GameMode.ORB_HUNT, emptyContent, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            10.0, 10.0, 0.0, 0.0, false, 0, false
        )
        val initialX = s.collectibles[0].x
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertEquals("Entities should not move during pending decision", initialX, s.collectibles[0].x, 0.001)
    }

    @Test
    fun stepGame_decisionTimerCountsDown() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, { 0.5 }, TouchTuning.STANDARD)
        s.pendingDecision = PendingDecision(
            LiteracyStatement("T", "T", "Test", false),
            5.0, 5.0, 0.0, 0.0, false, 0, false
        )
        val initialTimer = s.pendingDecision!!.timer
        GameEngine.stepGame(s, null, TouchTuning.STANDARD, { 0.5 }, 0.1)
        assertTrue("Decision timer should count down", s.pendingDecision!!.timer < initialTimer)
    }

    @Test
    fun createState_allModes_returnsValidState() {
        val modes = listOf(GameMode.ORB_HUNT, GameMode.MAZE_RUN, GameMode.TRUTH_SNIPE)
        for (mode in modes) {
            val s = GameEngine.createState(mode, null, { 0.5 }, TouchTuning.STANDARD)
            assertNotNull("Should return valid state for $mode", s)
            assertEquals(mode, s.mode)
        }
    }
}
