package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.*

/**
 * truthSnipe mode — Fact-checking mechanic.
 *
 * Pedagogical design: drifting facts (collectibles, blue) and fakes (hazards,
 * red) carry AI-literacy statements. The player reads the statement and
 * decides: shoot fakes to score, collect facts by touching them.
 * Shooting a fact (a true statement) is a penalty — destroying truth costs
 * points. This forces the player to distinguish fact from fake before acting.
 */
object TruthSnipeHandler : ModeHandler {

    /** Penalty for shooting a fact (a true statement). */
    private const val FACT_SHOOT_PENALTY = 20

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        for (i in 0 until cfg.initialHazard) GameSpawn.spawnEdge(s, cfg, rng, GameSpawn.SpawnKind.HAZARD, content)
        for (i in 0 until cfg.initialCollect) GameSpawn.spawnEdge(s, cfg, rng, GameSpawn.SpawnKind.COLLECT, content)
    }

    override fun step(s: GameState, cfg: ModeConfig, input: InputState, rng: () -> Double, content: LiteracyContentProvider?, dt: Double) {
        val p = s.player
        if (input.fire && s.fireCd <= 0) {
            val sp = cfg.bulletSpeed
            val b = GameSpawn.makeDisk(p.x, p.z, GameConfig.BULLET_RADIUS, cos(p.dir) * sp, sin(p.dir) * sp)
            s.bullets.add(b)
            s.fireCd = cfg.fireCooldown
            s.justFired = true
        }
        for (i in s.bullets.indices.reversed()) {
            val b = s.bullets[i]
            b.age += dt
            b.x += b.vx * dt
            b.z += b.vz * dt
            if (b.age > GameConfig.BULLET_LIFE || GameGeometry.dist2D(b.x, b.z, 0.0, 0.0) > cfg.arenaRadius + 1) {
                s.bullets.removeAt(i)
                continue
            }
            var consumed = false
            for (j in s.hazards.indices.reversed()) {
                val h = s.hazards[j]
                if (GameGeometry.overlaps(b.x, b.z, b.r, h.x, h.z, h.r)) {
                    s.score += cfg.hazardPoints
                    s.scoreKind = h.kind
                    s.justScored = true
                    s.scoreX = h.x
                    s.scoreZ = h.z
                    logShot(s, h)
                    s.hazards.removeAt(j)
                    s.bullets.removeAt(i)
                    consumed = true
                    break
                }
            }
            if (consumed) continue
            // Shooting a fact (collectible) is a penalty — you destroyed a true statement
            for (j in s.collectibles.indices.reversed()) {
                val o = s.collectibles[j]
                if (GameGeometry.overlaps(b.x, b.z, b.r, o.x, o.z, o.r)) {
                    s.score = maxOf(0, s.score - FACT_SHOOT_PENALTY)
                    s.hitKind = o.kind
                    s.justHit = true
                    s.hitX = o.x
                    s.hitZ = o.z
                    logPenalty(s, o)
                    s.collectibles.removeAt(j)
                    s.bullets.removeAt(i)
                    consumed = true
                    break
                }
            }
            if (consumed) continue
        }
        for (i in s.collectibles.indices.reversed()) {
            val o = s.collectibles[i]
            o.x += o.vx * dt
            o.z += o.vz * dt
            if (GameGeometry.dist2D(o.x, o.z, 0.0, 0.0) > cfg.arenaRadius + 1) {
                s.collectibles.removeAt(i)
                continue
            }
            if (GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, o.x, o.z, o.r)) {
                s.score += cfg.collectPoints
                s.justScored = true
                s.scoreKind = o.kind
                s.scoreX = o.x
                s.scoreZ = o.z
                logCollect(s, o)
                s.collectibles.removeAt(i)
            }
        }
        for (i in s.hazards.indices.reversed()) {
            val h = s.hazards[i]
            val d = maxOf(GameGeometry.dist2D(h.x, h.z, p.x, p.z), 0.0001)
            val homing = 0.6
            h.vx += (((p.x - h.x) / d) * cfg.hazardSpeed - h.vx) * homing * dt
            h.vz += (((p.z - h.z) / d) * cfg.hazardSpeed - h.vz) * homing * dt
            h.x += h.vx * dt
            h.z += h.vz * dt
            if (GameGeometry.dist2D(h.x, h.z, 0.0, 0.0) > cfg.arenaRadius + 1) {
                s.hazards.removeAt(i)
                continue
            }
            if (GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, h.x, h.z, h.r)) {
                if (p.invuln <= 0) {
                    p.invuln = 1.0
                    s.health -= 1
                    s.hitKind = h.kind
                    s.justHit = true
                    s.hitX = h.x
                    s.hitZ = h.z
                    s.hazards.removeAt(i)
                    if (s.health <= 0) {
                        GameRules.endGame(s, EndReason.HEALTH)
                        return
                    }
                }
            }
        }
        GameSpawn.topUp(s, cfg, rng, GameMode.TRUTH_SNIPE, content)
    }

    private fun logShot(s: GameState, h: Disk) {
        val stmt = h.statement ?: LiteracyStatement("", "", "Grundlagen der KI", true)
        s.classifications.add(ClassifyLog(stmt.domain, correct = true, statement = stmt))
    }

    private fun logPenalty(s: GameState, o: Disk) {
        val stmt = o.statement ?: LiteracyStatement("", "", "Grundlagen der KI", false)
        s.classifications.add(ClassifyLog(stmt.domain, correct = false, statement = stmt))
    }

    private fun logCollect(s: GameState, o: Disk) {
        val stmt = o.statement ?: LiteracyStatement("", "", "Grundlagen der KI", false)
        s.classifications.add(ClassifyLog(stmt.domain, correct = true, statement = stmt))
    }
}
