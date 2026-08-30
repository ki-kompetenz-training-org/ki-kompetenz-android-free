package ai.ki_kompetenz_training_org.data.minigames3d

/** Tunable configuration and level data for each game mode. */
object GameConfig {
    const val GOAL_RADIUS = 0.9
    const val GOAL_POINTS = 40
    const val BULLET_RADIUS = 0.32
    const val BULLET_LIFE = 1.4

    /** Number of distinct AI-literacy fact categories surfaced by orbHunt. */
    const val ORB_FACT_KINDS = 8
    /** Number of distinct AI-risk categories (hallucination, bias, deepfake, injection). */
    const val HAZARD_KINDS = 4
    /** Distance within which an item's statement is visible and classifiable. */
    const val SCAN_RADIUS = 3.0
    /** Base points for a correct classification (orbHunt). */
    const val CLASSIFY_POINTS = 30
    /** Bonus per consecutive correct classification (capped at +50). */
    const val CLASSIFY_STREAK_BONUS = 5
    /** Health lost for a wrong classification or expired maze decision. */
    const val WRONG_CLASSIFY_PENALTY = 1
    /** Seconds to answer a maze decision before it expires. */
    const val DECISION_TIMER = 5.0

    val MODES: Map<GameMode, ModeConfig> = mapOf(
        GameMode.ORB_HUNT to ModeConfig(
            arenaRadius = 11.0, duration = 60.0, target = 250, maxHealth = 3,
            playerRadius = 0.6, playerSpeed = 9.0,
            collectRadius = 0.5, collectPoints = 25,
            hazardRadius = 0.7, hazardSpeed = 3.2, hazardPoints = 0,
            initialCollect = 3, initialHazard = 4,
            hasWalls = false, hasGoal = false, hasBullets = false,
            bulletSpeed = 0.0, fireCooldown = 0.0,
            minCollect = 3, minHazard = 4,
            factKinds = ORB_FACT_KINDS, riskKinds = HAZARD_KINDS,
        ),
        GameMode.MAZE_RUN to ModeConfig(
            arenaRadius = 12.0, duration = 60.0, target = 200, maxHealth = 3,
            playerRadius = 0.6, playerSpeed = 8.5,
            collectRadius = 0.0, collectPoints = 0,
            hazardRadius = 0.7, hazardSpeed = 2.8, hazardPoints = 0,
            initialCollect = 0, initialHazard = 3,
            hasWalls = true, hasGoal = true, hasBullets = false,
            bulletSpeed = 0.0, fireCooldown = 0.0,
            minCollect = 0, minHazard = 3,
            factKinds = 8, riskKinds = 4,
        ),
        GameMode.TRUTH_SNIPE to ModeConfig(
            arenaRadius = 12.0, duration = 60.0, target = 300, maxHealth = 3,
            playerRadius = 0.6, playerSpeed = 9.5,
            collectRadius = 0.5, collectPoints = 20,
            hazardRadius = 0.6, hazardSpeed = 2.6, hazardPoints = 15,
            initialCollect = 4, initialHazard = 5,
            hasWalls = false, hasGoal = false, hasBullets = true,
            bulletSpeed = 20.0, fireCooldown = 0.32,
            minCollect = 4, minHazard = 5,
            factKinds = 8, riskKinds = 4,
        ),
    )

    fun getModeConfig(mode: GameMode): ModeConfig = MODES[mode]!!

    /**
     * Four short barriers arranged as a pinwheel around the center, leaving
     * wide diagonal channels to the four corners — a fully connected maze.
     */
    val MAZE_WALLS: List<RectWall> = listOf(
        RectWall(x = -4.0, z = -2.0, w = 0.8, d = 2.0),
        RectWall(x = 4.0, z = 2.0, w = 0.8, d = 2.0),
        RectWall(x = -2.0, z = 4.0, w = 2.0, d = 0.8),
        RectWall(x = 2.0, z = -4.0, w = 2.0, d = 0.8),
    )

    /** Reachable open spawn points for the maze goal (center + 4 corners). */
    val GOAL_SPOTS: List<Vec2> = listOf(
        Vec2(0.0, 0.0),
        Vec2(9.0, 9.0),
        Vec2(9.0, -9.0),
        Vec2(-9.0, 9.0),
        Vec2(-9.0, -9.0),
    )
}
