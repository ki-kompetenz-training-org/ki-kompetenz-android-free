package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.*

/**
 * Entity spawning utilities — disks, orbs, chasers, roamers, edge spawns,
 * and goal placement. Content-aware: each entity gets a [LiteracyStatement]
 * selected by the [LiteracyContentProvider] (individualized by mastery).
 */
object GameSpawn {

    fun makeDisk(
        x: Double,
        z: Double,
        r: Double,
        vx: Double,
        vz: Double,
        kind: Int = 0,
        isRisk: Boolean = false,
        statement: LiteracyStatement? = null,
    ): Disk = Disk(x, z, r, vx, vz, kind, isRisk, statement)

    /** Pick a goal spot different from the previous one. */
    fun nextGoalSpot(prev: Vec2?, rng: () -> Double = { Math.random() }): Vec2 {
        var spot = GameConfig.GOAL_SPOTS[0]
        for (i in 0 until 12) {
            spot = GameConfig.GOAL_SPOTS[Math.floor(rng() * GameConfig.GOAL_SPOTS.size).toInt()]
            if (prev == null || abs(spot.x - prev.x) > 0.5 || abs(spot.z - prev.z) > 0.5) break
        }
        return Vec2(spot.x, spot.z)
    }

    /** Spawn a truth-orb (collectible, isRisk = false) at a safe distance from the player. */
    fun spawnOrb(s: GameState, cfg: ModeConfig, rng: () -> Double = { Math.random() }, content: LiteracyContentProvider? = null) {
        val p = GameGeometry.randomSpawn(cfg.arenaRadius - 1, 3.0, s.player.x, s.player.z, rng)
        val stmt = content?.randomFact(rng) ?: LiteracyStatement("KI kann Fehler machen.", "AI can make mistakes.", "Grundlagen der KI", false)
        s.collectibles.add(
            makeDisk(p.x, p.z, cfg.collectRadius, 0.0, 0.0, kind = stmt.hashCode().and(0xff), isRisk = false, statement = stmt)
        )
    }

    /** Spawn a chaser hazard (isRisk = true) at the arena edge. */
    fun spawnChaser(s: GameState, cfg: ModeConfig, rng: () -> Double = { Math.random() }, content: LiteracyContentProvider? = null) {
        val ang = rng() * PI * 2
        val r = cfg.arenaRadius - 1.5
        val stmt = content?.randomRisk(rng) ?: LiteracyStatement("KI-Halluzination.", "AI hallucination.", "Grundlagen der KI", true)
        s.hazards.add(
            makeDisk(cos(ang) * r, sin(ang) * r, cfg.hazardRadius, 0.0, 0.0, kind = stmt.hashCode().and(0xff), isRisk = true, statement = stmt)
        )
    }

    /** Spawn a roamer hazard with edge velocity (isRisk = true). */
    fun spawnRoamer(s: GameState, cfg: ModeConfig, rng: () -> Double = { Math.random() }, content: LiteracyContentProvider? = null) {
        val e = GameGeometry.edgeSpawn(cfg, rng, 1.0)
        val stmt = content?.randomRisk(rng) ?: LiteracyStatement("KI-Halluzination.", "AI hallucination.", "Grundlagen der KI", true)
        s.hazards.add(
            makeDisk(e.x, e.z, cfg.hazardRadius, e.vx, e.vz, kind = stmt.hashCode().and(0xff), isRisk = true, statement = stmt)
        )
    }

    /** Spawn a drifting entity at the arena edge (hazard or collectible). */
    fun spawnEdge(s: GameState, cfg: ModeConfig, rng: () -> Double = { Math.random() }, kind: SpawnKind, content: LiteracyContentProvider? = null) {
        val e = GameGeometry.edgeSpawn(cfg, rng, if (kind == SpawnKind.HAZARD) 1.0 else 1.3)
        val risk = kind == SpawnKind.HAZARD
        val stmt = if (risk) content?.randomRisk(rng) else content?.randomFact(rng)
        val fallback = LiteracyStatement(
            if (risk) "Desinformation." else "Eine verifizierte KI-Fakt.",
            if (risk) "Disinformation." else "A verified AI fact.",
            "Grundlagen der KI", risk,
        )
        val d = makeDisk(
            e.x, e.z,
            if (kind == SpawnKind.HAZARD) cfg.hazardRadius else cfg.collectRadius,
            e.vx, e.vz,
            kind = (stmt ?: fallback).hashCode().and(0xff),
            isRisk = risk,
            statement = stmt ?: fallback,
        )
        if (kind == SpawnKind.HAZARD) s.hazards.add(d) else s.collectibles.add(d)
    }

    enum class SpawnKind { HAZARD, COLLECT }

    /** Maintain minimum entity counts by spawning as needed. */
    fun topUp(s: GameState, cfg: ModeConfig, rng: () -> Double = { Math.random() }, mode: GameMode, content: LiteracyContentProvider? = null) {
        while (s.collectibles.size < cfg.minCollect) {
            if (mode == GameMode.ORB_HUNT) spawnOrb(s, cfg, rng, content)
            else spawnEdge(s, cfg, rng, SpawnKind.COLLECT, content)
        }
        while (s.hazards.size < cfg.minHazard) {
            when (mode) {
                GameMode.ORB_HUNT -> spawnChaser(s, cfg, rng, content)
                GameMode.MAZE_RUN -> spawnRoamer(s, cfg, rng, content)
                GameMode.TRUTH_SNIPE -> spawnEdge(s, cfg, rng, SpawnKind.HAZARD, content)
            }
        }
    }
}
