/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native TruthSnipe mode handler - replaces TruthSnipeHandler.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.abs

/**
 * Touch-native TruthSnipe handler.
 * 
 * Mode behavior:
 * - Chips (disks) drift right across the arena
 * - Player taps a chip to trigger classification
 * - FAKE (isRisk=true): destroy chip, +20 points + streak
 * - FACT (isRisk=false): archive chip, +10 points + streak
 * - Wrong: -1 health, chip continues drifting
 * - Edge crossing: -1 health, edge flash
 * - Decision timeout: chip continues drifting (no health penalty)
 */
object TruthSnipeHandler : ModedHandler {

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        // Initialize chips
        topUp(s, cfg, TouchTuning.STANDARD, rng, content)
    }

    override fun step(s: GameState, cfg: ModeConfig, tuning: TouchTuning, content: LiteracyContentProvider?, rng: () -> Double, dt: Double) {
        val speed = tuning.speedMultiplier * cfg.chipSpeed
        val radius = cfg.arenaRadius
        
        s.collectibles.forEach { disk ->
            disk.age += dt
            disk.x += speed * dt
            
            // Check edge crossing
            if (abs(disk.x) > radius - disk.r) {
                // Mark for removal and penalize health
                disk.x = if (disk.x > 0) radius - disk.r else -(radius - disk.r)
                // Edge crossing penalty handled in stepGame
            }
        }
        
        // Remove old chips
        s.collectibles.removeIf { disk ->
            disk.age > cfg.chipLifetime || abs(disk.x) > cfg.arenaRadius + disk.r
        }
        
        // Top up chips
        topUp(s, cfg, tuning, rng, content)
    }

    override fun onTap(s: GameState, cfg: ModeConfig, diskIndex: Int, content: LiteracyContentProvider?, rng: () -> Double) {
        if (diskIndex < 0 || diskIndex >= s.collectibles.size) return
        
        val disk = s.collectibles[diskIndex]
        val decisionTimer = cfg.decisionSeconds
        
        s.pendingDecision = PendingDecision(
            statement = disk.statement ?: LiteracyStatement("Error", "Error", "Error", false),
            timerMax = decisionTimer,
            timer = decisionTimer,
            x = disk.x,
            z = disk.z,
            fromBonus = false,
            diskIndex = diskIndex,
            isRisk = disk.isRisk,
        )
    }

    override fun onDash(s: GameState, cfg: ModeConfig, dir: Direction, content: LiteracyContentProvider?, rng: () -> Double) {
        // TruthSnipe doesn't use dash
    }

    override fun onDecisionClosed(s: GameState, cfg: ModeConfig, closed: PendingDecision, correct: Boolean, content: LiteracyContentProvider?, rng: () -> Double) {
        if (correct) {
            // Remove the chip on correct classification
            if (closed.diskIndex >= 0 && closed.diskIndex < s.collectibles.size) {
                s.collectibles.removeAt(closed.diskIndex)
            }
        }
        // Wrong: chip continues drifting
    }

    override fun topUp(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?) {
        while (s.collectibles.size < cfg.minChips) {
            spawnChip(s, cfg, tuning, rng, content)
        }
    }

    private fun spawnChip(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?) {
        val contentProvider = content ?: return
        val isRisk = rng() < 0.5
        val statement = if (isRisk) contentProvider.randomRisk(rng) else contentProvider.randomFact(rng)
        
        // Spawn on left edge, random z
        val z = (rng() * 2 - 1) * cfg.arenaRadius * 0.8
        val x = -cfg.arenaRadius + cfg.chipRadius
        
        val disk = Disk(
            x = x,
            z = z,
            r = cfg.chipRadius,
            vx = 0.0,
            vz = 0.0,
            kind = if (isRisk) 1 else 0,
            isRisk = isRisk,
            statement = statement,
            classified = false,
            age = 0.0,
            phase = rng() * 2 * Math.PI,
        )
        
        s.collectibles.add(disk)
    }
}
