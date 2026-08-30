package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Strategy interface for mode-specific game rules.
 * Each mode provides an init (spawn initial entities) and a step (per-frame logic).
 * The engine dispatches to the handler; adding a mode = one new object + one registry line.
 */
interface ModeHandler {
    fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?)
    fun step(s: GameState, cfg: ModeConfig, input: InputState, rng: () -> Double, content: LiteracyContentProvider?, dt: Double)
}

/** Mode -> handler registry. */
object ModedHandlers {
    private val handlers: Map<GameMode, ModeHandler> = mapOf(
        GameMode.ORB_HUNT to OrbHuntHandler,
        GameMode.MAZE_RUN to MazeRunHandler,
        GameMode.TRUTH_SNIPE to TruthSnipeHandler,
    )
    fun get(mode: GameMode): ModeHandler = handlers[mode]!!
}
