/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native rewrite - will replace GameEngine.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Core game engine — touch-native rewrite.
 *
 * Rules:
 * - stepGame always freezes entities when pendingDecision != null (only timer ticks)
 * - onAction dispatches to the mode handler; Classify resolves pendingDecision
 * - Decision timeout triggers resolveDecision with correct=false
 * - Health <= 0 ends game immediately
 * - Time <= 0 ends game immediately
 */
object GameEngine {

    /**
     * Touch-native entry. Creates a fresh state, then immediately invokes
     * handler.init + topUpAll via createStateInternal.
     * Content/rng for statements, tuning defaults STANDARD.
     */
    fun createState(
        mode: GameMode,
        content: LiteracyContentProvider? = null,
        rng: () -> Double = { Random.nextDouble() },
        tuning: TouchTuning = TouchTuning.STANDARD,
    ): GameState {
        val cfg = GameConfig.getModeConfig(mode)
        return createStateInternal(mode, cfg, content, rng, tuning)
    }



    private fun createStateInternal(
        mode: GameMode,
        cfg: ModeConfig,
        content: LiteracyContentProvider?,
        rng: () -> Double,
        tuning: TouchTuning,
    ): GameState {
        val s = GameState.create(mode, cfg)
        val handler = ModedHandlers.get(mode)
        handler.init(s, cfg, rng, content)
        topUp(s, cfg, tuning, mode, rng, content)
        return s
    }

    /**
     * Per-frame advance – touch-native.
     * Freeze invariant: if pendingDecision != null only decision timer runs.
     * No input state; input comes via onAction.
     */
    fun stepGame(
        s: GameState,
        content: LiteracyContentProvider?,
        tuning: TouchTuning,
        rng: () -> Double,
        dt: Double,
    ) {
        if (s.ended) return
        val cfg = GameConfig.getModeConfig(s.mode)

        // Freeze: if a decision is pending, only tick its timer
        if (s.pendingDecision != null) {
            s.pendingDecision = s.pendingDecision!!.copy(timer = s.pendingDecision!!.timer - dt)
            if (s.pendingDecision!!.timer <= 0) {
                // Decision timed out -> treat as wrong
                resolveDecision(s, ClassifyAction.RISK, content, rng, cfg, tuning)
            }
            return
        }

        // Normal tick: reset transients
        s.justScored = false
        s.justHit = false

        s.time += dt
        s.timeLeft = max(0.0, s.timeLeft - dt)

        ModedHandlers.get(s.mode).step(s, cfg, tuning, content, rng, dt)

        if (s.timeLeft <= 0.0) {
            s.timeLeft = 0.0
            // FIX (2026-09-01): Zeitablauf endete fälschlich mit HEALTH statt TIME
            // → falsche "won"-Bewertung und falsche Endbegründung im Ergebnis-Screen.
            GameRules.endGame(s, EndReason.TIME)
        }
    }

    /**
     * Handle a touch action (tap / dash / classify).
     * Dash/Tap are ignored while pendingDecision != null.
     * Classify only allowed while pendingDecision != null.
     */
    fun onAction(
        s: GameState,
        action: GameAction,
        content: LiteracyContentProvider?,
        rng: () -> Double,
        tuning: TouchTuning,
    ) {
        if (s.ended) return
        val cfg = GameConfig.getModeConfig(s.mode)

        if (s.pendingDecision != null) {
            if (action is GameAction.Classify) {
                resolveDecision(s, action.action, content, rng, cfg, tuning)
            }
            return
        }

        when (action) {
            is GameAction.TapEntity -> ModedHandlers.get(s.mode).onTap(s, cfg, action.diskIndex, content, rng)
            is GameAction.Dash -> ModedHandlers.get(s.mode).onDash(s, cfg, action.dir, content, rng)
            is GameAction.Classify -> {
                // classify without pendingDecision – ignore (should not happen)
            }
        }
    }

    /**
     * Close the pending decision with player's chosen action.
     * Handles scoring, streak, health, and mode-specific cleanup via onDecisionClosed.
     */
    fun resolveDecision(
        s: GameState,
        action: ClassifyAction,
        content: LiteracyContentProvider?,
        rng: () -> Double,
        cfg: ModeConfig,
        tuning: TouchTuning,
    ) {
        val pd = s.pendingDecision ?: return

        // Determine correctness
        val correct: Boolean = if (pd.diskIndex >= 0 && pd.diskIndex < s.collectibles.size) {
            val disk = s.collectibles[pd.diskIndex]
            (action == ClassifyAction.RISK) == disk.isRisk
        } else {
            // maze: statement already carries isRisk
            (action == ClassifyAction.RISK) == pd.statement.isRisk
        }

        // Log classification
        s.classifications.add(ClassifyLog(pd.statement.domain, correct, pd.statement))

        // Score and health
        if (correct) {
            val bonus = minOf(s.classifyStreak * GameConfig.CLASSIFY_STREAK_BONUS, 50)
            var points = if (pd.fromBonus) GameConfig.MAZE_BONUS_POINTS else GameConfig.CLASSIFY_POINTS
            
            // snipe fact archive = half points
            if (s.mode == GameMode.TRUTH_SNIPE && pd.diskIndex >= 0) {
                val disk = s.collectibles[pd.diskIndex]
                if (!disk.isRisk) {
                    points = GameConfig.TRUTH_SNIPE_FACT_POINTS
                }
            }
            
            s.score += points + bonus
            s.classifyStreak++
            s.justScored = true
            s.scoreKind = pd.statement.hashCode().and(0xff)
            
            if (pd.diskIndex >= 0 && pd.diskIndex < s.collectibles.size) {
                val d = s.collectibles[pd.diskIndex]
                s.scoreX = d.x
                s.scoreZ = d.z
            } else {
                s.scoreX = 0.0
                s.scoreZ = 0.0
            }
        } else {
            s.health -= cfg.wrongPoints
            s.classifyStreak = 0
            s.justHit = true
            if (pd.diskIndex >= 0 && pd.diskIndex < s.collectibles.size) {
                val d = s.collectibles[pd.diskIndex]
                s.hitX = d.x
                s.hitZ = d.z
                s.hitKind = d.kind
            } else {
                s.hitX = 0.0
                s.hitZ = 0.0
            }
        }

        val closed = pd
        s.pendingDecision = null
        ModedHandlers.get(s.mode).onDecisionClosed(s, cfg, closed, correct, content, rng)

        if (s.health <= 0) {
            s.health = 0
            GameRules.endGame(s, EndReason.HEALTH)
        }
    }

    /**
     * Keep the minimum number of collectibles defined by Config.
     */
    fun topUp(
        s: GameState,
        cfg: ModeConfig,
        tuning: TouchTuning,
        mode: GameMode,
        rng: () -> Double,
        content: LiteracyContentProvider?,
    ) {
        when (mode) {
            GameMode.ORB_HUNT -> OrbHuntHandler.topUp(s, cfg, tuning, rng, content)
            GameMode.MAZE_RUN -> { /* maze uses layout cells, no top-up */ }
            GameMode.TRUTH_SNIPE -> TruthSnipeHandler.topUp(s, cfg, tuning, rng, content)
        }
    }
}
