package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.*

/**
 * mazeRun mode — Decision-at-goal mechanic.
 *
 * Pedagogical design: reaching the goal doesn't immediately score. Instead it
 * triggers a classify decision with a DECISION_TIMER countdown. Correct answers
 * bank GOAL_POINTS + streak bonus; wrong answers or timeouts cost health.
 * Roamers still move during the decision, maintaining pressure.
 */
object MazeRunHandler : ModeHandler {

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        for (i in 0 until cfg.initialHazard) GameSpawn.spawnRoamer(s, cfg, rng, content)
    }

    override fun step(s: GameState, cfg: ModeConfig, input: InputState, rng: () -> Double, content: LiteracyContentProvider?, dt: Double) {
        val p = s.player

        // Handle pending decision at a goal
        if (s.pendingDecision != null) {
            s.pendingDecision = s.pendingDecision?.let { it.copy(timer = it.timer - dt) }
            // Roamers still move during the decision
            for (i in s.hazards.indices.reversed()) {
                val e = s.hazards[i]
                e.x += e.vx * dt
                e.z += e.vz * dt
                GameGeometry.bounceArena(e, cfg.arenaRadius - e.r)
                if (cfg.hasWalls) GameGeometry.bounceWalls(e, s.walls, e.r)
                if (GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, e.x, e.z, e.r)) {
                    if (p.invuln <= 0) {
                        p.invuln = 1.0
                        s.health -= 1
                        s.hitKind = e.kind
                        s.justHit = true
                        s.hitX = e.x
                        s.hitZ = e.z
                        val ns = GameGeometry.edgeSpawn(cfg, rng, 1.0)
                        e.x = ns.x; e.z = ns.z; e.vx = ns.vx; e.vz = ns.vz
                        if (s.health <= 0) {
                            GameRules.endGame(s, EndReason.HEALTH)
                            return
                        }
                    }
                }
            }
            val pd = s.pendingDecision ?: return
            if (input.classify != null) {
                val playerSaidRisk = input.classify == ClassifyAction.RISK
                val correct = playerSaidRisk == pd.statement.isRisk
                s.lastClassify = ClassifyResult(correct, pd.statement.hashCode().and(0xff), pd.statement.isRisk)
                s.classifications.add(ClassifyLog(pd.statement.domain, correct, pd.statement))
                if (correct) {
                    val bonus = minOf(s.classifyStreak * GameConfig.CLASSIFY_STREAK_BONUS, 50)
                    s.score += GameConfig.GOAL_POINTS + bonus
                    s.classifyStreak++
                    s.scoreKind = pd.statement.hashCode().and(0xff)
                    s.justScored = true
                    s.scoreX = s.goal?.x ?: 0.0
                    s.scoreZ = s.goal?.z ?: 0.0
                } else {
                    s.health -= GameConfig.WRONG_CLASSIFY_PENALTY
                    s.classifyStreak = 0
                    s.hitKind = pd.statement.hashCode().and(0xff)
                    s.justHit = true
                    s.hitX = s.goal?.x ?: 0.0
                    s.hitZ = s.goal?.z ?: 0.0
                }
                s.pendingDecision = null
                s.goal = GameSpawn.nextGoalSpot(s.goal, rng)
                s.goalIndex += 1
                if (s.goalIndex % 2 == 1 && s.hazards.size < 6) GameSpawn.spawnRoamer(s, cfg, rng, content)
                if (s.health <= 0) {
                    GameRules.endGame(s, EndReason.HEALTH)
                    return
                }
            } else if (pd.timer <= 0) {
                // Decision timed out — wrong answer
                s.health -= GameConfig.WRONG_CLASSIFY_PENALTY
                s.classifyStreak = 0
                s.hitKind = pd.statement.hashCode().and(0xff)
                s.justHit = true
                s.pendingDecision = null
                s.goal = GameSpawn.nextGoalSpot(s.goal, rng)
                s.goalIndex += 1
                if (s.health <= 0) {
                    GameRules.endGame(s, EndReason.HEALTH)
                    return
                }
            }
            return // skip normal processing while decision is pending
        }

        // Normal hazard movement
        for (i in s.hazards.indices.reversed()) {
            val e = s.hazards[i]
            e.x += e.vx * dt
            e.z += e.vz * dt
            GameGeometry.bounceArena(e, cfg.arenaRadius - e.r)
            if (cfg.hasWalls) GameGeometry.bounceWalls(e, s.walls, e.r)
            if (GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, e.x, e.z, e.r)) {
                if (p.invuln <= 0) {
                    p.invuln = 1.0
                    s.health -= 1
                    s.hitKind = e.kind
                    s.justHit = true
                    s.hitX = e.x
                    s.hitZ = e.z
                    val ns = GameGeometry.edgeSpawn(cfg, rng, 1.0)
                    e.x = ns.x; e.z = ns.z; e.vx = ns.vx; e.vz = ns.vz
                    if (s.health <= 0) {
                        GameRules.endGame(s, EndReason.HEALTH)
                        return
                    }
                }
            }
        }

        // Goal reached — trigger a decision (not immediate score)
        if (s.goal != null && GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, s.goal!!.x, s.goal!!.z, GameConfig.GOAL_RADIUS)) {
            val isRisk = rng() < 0.5
            val stmt = if (isRisk) content?.randomRisk(rng) else content?.randomFact(rng)
            val fallback = LiteracyStatement(
                if (isRisk) "Desinformation." else "Eine verifizierte KI-Fakt.",
                if (isRisk) "Disinformation." else "A verified AI fact.",
                "Grundlagen der KI", isRisk,
            )
            s.pendingDecision = PendingDecision(stmt ?: fallback, GameConfig.DECISION_TIMER)
            s.scoreX = s.goal!!.x
            s.scoreZ = s.goal!!.z
        }

        GameSpawn.topUp(s, cfg, rng, GameMode.MAZE_RUN, content)
    }
}
