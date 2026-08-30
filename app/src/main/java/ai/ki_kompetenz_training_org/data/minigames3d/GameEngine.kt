package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.*

/**
 * Engine core — state factory, per-frame loop, player movement, mode dispatch.
 * The engine is mode-agnostic: delegates init/step to the ModeHandler strategy.
 */
object GameEngine {

    fun createState(mode: GameMode, content: LiteracyContentProvider? = null, rng: () -> Double = { Math.random() }): GameState {
        val cfg = GameConfig.MODES[mode]!!
        val s = GameState(
            mode = mode,
            time = 0.0,
            timeLeft = cfg.duration,
            score = 0,
            health = cfg.maxHealth,
            maxHealth = cfg.maxHealth,
            target = cfg.target,
            player = Player(0.0, 0.0, 0.0, 0.0, -PI / 2, 0.0),
            collectibles = mutableListOf(),
            hazards = mutableListOf(),
            bullets = mutableListOf(),
            walls = if (cfg.hasWalls) GameConfig.MAZE_WALLS.map { it } else emptyList(),
            goal = if (cfg.hasGoal) GameSpawn.nextGoalSpot(null, rng) else null,
            goalIndex = 0,
            fireCd = 0.0,
            ended = false,
            endReason = null,
            won = false,
            justScored = false,
            justHit = false,
            justFired = false,
            hitX = 0.0, hitZ = 0.0,
            scoreX = 0.0, scoreZ = 0.0,
            scoreKind = 0, hitKind = 0,
            scannedIndex = -1,
            scannedIsRisk = false,
            scannedKind = 0,
            lastClassify = null,
            classifyStreak = 0,
            pendingDecision = null,
            classifications = mutableListOf(),
        )
        ModedHandlers.get(mode).init(s, cfg, rng, content)
        return s
    }

    fun stepGame(
        s: GameState,
        input: InputState,
        rng: () -> Double = { Math.random() },
        content: LiteracyContentProvider? = null,
        dt: Double = 1.0 / 60.0,
    ) {
        if (s.ended) return
        val cfg = GameConfig.MODES[s.mode]!!
        s.justScored = false
        s.justHit = false
        s.justFired = false

        s.time += dt
        s.timeLeft -= dt

        movePlayer(s, input, cfg, dt)
        ModedHandlers.get(s.mode).step(s, cfg, input, rng, content, dt)

        if (s.player.invuln > 0) s.player.invuln -= dt
        if (s.fireCd > 0) s.fireCd -= dt

        if (s.timeLeft <= 0) {
            s.timeLeft = 0.0
            GameRules.endGame(s, EndReason.TIME)
        }
    }

    /** Move the player based on directional input, clamping to the arena and walls. */
    fun movePlayer(s: GameState, input: InputState, cfg: ModeConfig, dt: Double) {
        var ax = 0.0
        var az = 0.0
        if (input.up) az -= 1
        if (input.down) az += 1
        if (input.left) ax -= 1
        if (input.right) ax += 1
        val len = hypot(ax, az)
        if (len > 0) {
            val vx = ax / len
            val vz = az / len
            s.player.vx = vx
            s.player.vz = vz
            s.player.dir = atan2(vz, vx)
            s.player.x += vx * cfg.playerSpeed * dt
            s.player.z += vz * cfg.playerSpeed * dt
        }
        val c = GameGeometry.clampToArena(s.player.x, s.player.z, cfg.arenaRadius, cfg.playerRadius)
        s.player.x = c.x
        s.player.z = c.z
        if (cfg.hasWalls) {
            val r = GameGeometry.resolveWalls(s.player.x, s.player.z, cfg.playerRadius, s.walls)
            s.player.x = r.x
            s.player.z = r.z
        }
    }
}
