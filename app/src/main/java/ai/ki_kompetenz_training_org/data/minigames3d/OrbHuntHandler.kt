/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native OrbHunt mode handler - replaces OrbHuntHandler.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Touch-native OrbHunt handler.
 * 
 * Mode behavior:
 * - Orbs (disks) float around the arena
 * - Player taps an orb to trigger a classification decision
 * - Correct: +25 points + streak bonus, orb removed
 * - Wrong: -1 health, orb stays
 * - Decision timeout: -1 health, orb stays
 */
object OrbHuntHandler : ModedHandler {

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        // Initialize orbs
        topUp(s, cfg, TouchTuning.STANDARD, rng, content)
    }

    override fun step(s: GameState, cfg: ModeConfig, tuning: TouchTuning, content: LiteracyContentProvider?, rng: () -> Double, dt: Double) {
        // Move orbs in circles
        val speed = tuning.speedMultiplier
        s.collectibles.forEach { disk ->
            disk.age += dt
            // Circular motion
            disk.x += cos(disk.phase + disk.age * 0.5) * speed * dt
            disk.z += sin(disk.phase + disk.age * 0.5) * speed * dt
            // Clamp to arena
            val (clampedX, clampedZ) = GameGeometry.clampToArena(disk.x, disk.z, disk.r, cfg.arenaRadius)
            disk.x = clampedX
            disk.z = clampedZ
        }
        
        // Top up orbs if needed
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
        // OrbHunt doesn't use dash
    }

    override fun onDecisionClosed(s: GameState, cfg: ModeConfig, closed: PendingDecision, correct: Boolean, content: LiteracyContentProvider?, rng: () -> Double) {
        if (correct && closed.diskIndex >= 0 && closed.diskIndex < s.collectibles.size) {
            // Remove the orb on correct classification
            s.collectibles.removeAt(closed.diskIndex)
        }
    }

    override fun topUp(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?) {
        while (s.collectibles.size < cfg.minChips) {
            spawnOrb(s, cfg, tuning, rng, content)
        }
    }

    private fun spawnOrb(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?) {
        val contentProvider = content ?: return
        val isRisk = rng() < 0.5
        val statement = if (isRisk) contentProvider.randomRisk(rng) else contentProvider.randomFact(rng)
        
        val spawn = GameGeometry.randomSpawn(cfg.arenaRadius, cfg.chipRadius, 0.0, 0.0, rng)
        
        val disk = Disk(
            x = spawn.x,
            z = spawn.z,
            r = cfg.chipRadius,
            vx = 0.0,
            vz = 0.0,
            kind = if (isRisk) 1 else 0,
            isRisk = isRisk,
            statement = statement,
            classified = false,
            age = 0.0,
            phase = rng() * 2 * PI,
        )
        
        s.collectibles.add(disk)
    }
}
