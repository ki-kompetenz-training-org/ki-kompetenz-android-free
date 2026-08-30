package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * orbHunt mode — Classification mechanic.
 *
 * Pedagogical design: items (orbs and chasers) carry AI-literacy statements.
 * The player approaches an item to scan it (within SCAN_RADIUS), reads the
 * statement, and classifies it as fact or risk. Correct classification scores
 * points with a streak bonus; wrong classification costs health. Chasers still
 * pursue and damage the player if not classified in time.
 *
 * Individualization: statements come from [LiteracyContentProvider], which
 * weights domains by the learner's mastery profile (weak areas appear more).
 */
object OrbHuntHandler : ModeHandler {

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        for (i in 0 until cfg.initialCollect) GameSpawn.spawnOrb(s, cfg, rng, content)
        for (i in 0 until cfg.initialHazard) GameSpawn.spawnChaser(s, cfg, rng, content)
    }

    override fun step(s: GameState, cfg: ModeConfig, input: InputState, rng: () -> Double, content: LiteracyContentProvider?, dt: Double) {
        val p = s.player

        // Find nearest unclassified item within scan radius
        val allItems = s.collectibles + s.hazards
        var nearestIdx = -1
        var nearestDist = Double.MAX_VALUE
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item.classified) continue
            val d = GameGeometry.dist2D(p.x, p.z, item.x, item.z)
            if (d < GameConfig.SCAN_RADIUS && d < nearestDist) {
                nearestDist = d
                nearestIdx = i
            }
        }

        // Update scanned item info for renderer
        if (nearestIdx >= 0) {
            val item = allItems[nearestIdx]
            s.scannedIndex = nearestIdx
            s.scannedIsRisk = item.isRisk
            s.scannedKind = item.kind
        } else {
            s.scannedIndex = -1
        }

        // Handle classification input
        if (input.classify != null && nearestIdx >= 0) {
            val item = allItems[nearestIdx]
            val playerSaidRisk = input.classify == ClassifyAction.RISK
            val correct = playerSaidRisk == item.isRisk
            item.classified = true
            s.lastClassify = ClassifyResult(correct, item.kind, item.isRisk)
            val stmt = item.statement ?: LiteracyStatement("", "", "Grundlagen der KI", item.isRisk)
            s.classifications.add(ClassifyLog(stmt.domain, correct, stmt))
            if (correct) {
                val bonus = minOf(s.classifyStreak * GameConfig.CLASSIFY_STREAK_BONUS, 50)
                s.score += GameConfig.CLASSIFY_POINTS + bonus
                s.classifyStreak++
                s.scoreKind = item.kind
                s.justScored = true
                s.scoreX = item.x
                s.scoreZ = item.z
            } else {
                s.health -= GameConfig.WRONG_CLASSIFY_PENALTY
                s.classifyStreak = 0
                s.hitKind = item.kind
                s.justHit = true
                s.hitX = item.x
                s.hitZ = item.z
            }
            // Remove classified item and respawn
            if (nearestIdx < s.collectibles.size) {
                s.collectibles.removeAt(nearestIdx)
                GameSpawn.spawnOrb(s, cfg, rng, content)
            } else {
                s.hazards.removeAt(nearestIdx - s.collectibles.size)
                GameSpawn.spawnChaser(s, cfg, rng, content)
            }
            if (s.health <= 0) {
                GameRules.endGame(s, EndReason.HEALTH)
                return
            }
        }

        // Hazards chase and damage on contact (if not classified in time)
        for (i in s.hazards.indices.reversed()) {
            val e = s.hazards[i]
            if (e.classified) continue
            val d = maxOf(GameGeometry.dist2D(e.x, e.z, p.x, p.z), 0.0001)
            e.x += ((p.x - e.x) / d) * cfg.hazardSpeed * dt
            e.z += ((p.z - e.z) / d) * cfg.hazardSpeed * dt
            if (GameGeometry.overlaps(p.x, p.z, cfg.playerRadius, e.x, e.z, e.r)) {
                if (p.invuln <= 0) {
                    p.invuln = 1.2
                    s.health -= 1
                    s.hitKind = e.kind
                    s.justHit = true
                    s.hitX = e.x
                    s.hitZ = e.z
                    s.hazards.removeAt(i)
                    GameSpawn.spawnChaser(s, cfg, rng, content)
                    if (s.health <= 0) {
                        GameRules.endGame(s, EndReason.HEALTH)
                        return
                    }
                }
            }
        }
        GameSpawn.topUp(s, cfg, rng, GameMode.ORB_HUNT, content)
    }
}
