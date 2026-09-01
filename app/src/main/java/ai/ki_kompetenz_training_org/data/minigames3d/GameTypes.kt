package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Domain types for the 3D AI-literacy minigame engine.
 *
 * Pure Kotlin, no Android dependencies. Fully unit-testable.
 *
 * Three game modes (touch-native):
 *  - ORB_HUNT:    tap orbs to classify as fact or risk
 *  - MAZE_RUN:    swipe to dash through maze, classify at goal
 *  - TRUTH_SNIPE: swipe to move, tap to classify drifting chips
 *
 * Individualization: each entity carries a [LiteracyStatement] from the question
 * bank. The [MasteryTracker] weights spawning toward weak domains so learners
 * practice what they do not know yet.
 */

enum class GameMode { ORB_HUNT, MAZE_RUN, TRUTH_SNIPE }

enum class EndReason { TIME, HEALTH }

data class Vec2(val x: Double, val z: Double)

/** A moving circular entity (orbs, hazards, chips). Fields are var for in-place mutation. */
data class Disk(
    var x: Double,
    var z: Double,
    val r: Double,
    var vx: Double,
    var vz: Double,
    val kind: Int = 0,
    val isRisk: Boolean = false,
    val statement: LiteracyStatement? = null,
    var classified: Boolean = false,
    var age: Double = 0.0,
    val phase: Double = Math.random(),
)

/** Axis-aligned wall, centered at (x,z) with half-extents (w,d). */
data class RectWall(val x: Double, val z: Double, val w: Double, val d: Double)

/** One-shot classification input: FACT or RISK or null. */
enum class ClassifyAction { FACT, RISK }

/** Legacy input state for joystick - kept for backward compatibility but not used in touch-native */
data class InputState(
    val up: Boolean,
    val down: Boolean,
    val left: Boolean,
    val right: Boolean,
    val fire: Boolean,
    val classify: ClassifyAction? = null,
)

/**
 * Touch-native pending decision.
 * Triggered when player taps an entity (ORB_HUNT, TRUTH_SNIPE) or reaches goal (MAZE_RUN).
 * 
 * @param statement The AI literacy statement to classify
 * @param timerMax Initial timer value (counts down)
 * @param timer Current timer value
 * @param x Position x for visualization
 * @param z Position z for visualization
 * @param fromBonus True if this decision is from a maze bonus cell
 * @param diskIndex Index in collectibles list (or -1 for maze statements)
 * @param isRisk Whether the statement is a risk (for display purposes)
 */
data class PendingDecision(
    val statement: LiteracyStatement,
    val timerMax: Double,
    var timer: Double,
    val x: Double,
    val z: Double,
    val fromBonus: Boolean,
    val diskIndex: Int,
    val isRisk: Boolean,
)

data class ClassifyResult(val correct: Boolean, val kind: Int, val isRisk: Boolean)

/** Classification log entry for post-game mastery update. */
data class ClassifyLog(
    val domain: String,
    val correct: Boolean,
    val statement: LiteracyStatement,
)

/**
 * Touch-native mode configuration.
 * All timing in seconds, distances in world units.
 * Replaces old joystick-based ModeConfig.
 */
data class ModeConfig(
    val arenaRadius: Double,
    val duration: Double,
    val target: Int,
    val maxHealth: Int,
    val wrongPoints: Int,
    val decisionSeconds: Double,
    val chipRadius: Double,
    val chipSpeed: Double,
    val chipLifetime: Double,
    val spawnInterval: Double,
    val minChips: Int,
    val maxChips: Int,
    val mazeLevel: Int,
) {
    companion object {
        fun orbHunt(): ModeConfig = ModeConfig(
            arenaRadius = 15.0,
            duration = 60.0,
            target = 250,
            maxHealth = 3,
            wrongPoints = 1,
            decisionSeconds = 5.0,
            chipRadius = 0.8,
            chipSpeed = 0.0,
            chipLifetime = 30.0,
            spawnInterval = 2.0,
            minChips = 6,
            maxChips = 12,
            mazeLevel = 0,
        )

        fun mazeRun(): ModeConfig = ModeConfig(
            arenaRadius = 15.0,
            duration = 60.0,
            target = 200,
            maxHealth = 3,
            wrongPoints = 1,
            decisionSeconds = 6.0,
            chipRadius = 0.0,
            chipSpeed = 0.0,
            chipLifetime = 0.0,
            spawnInterval = 0.0,
            minChips = 0,
            maxChips = 0,
            mazeLevel = 0,
        )

        fun truthSnipe(): ModeConfig = ModeConfig(
            arenaRadius = 15.0,
            duration = 60.0,
            target = 300,
            maxHealth = 3,
            wrongPoints = 1,
            decisionSeconds = 4.0,
            chipRadius = 0.5,
            chipSpeed = 1.5,
            chipLifetime = 30.0,
            spawnInterval = 1.5,
            minChips = 4,
            maxChips = 8,
            mazeLevel = 0,
        )
    }
}

// ========== TOUCH-NATIVE CORE TYPES (T1) ==========

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    companion object {
        /** Compatibility: Direction.values() instead of .entries */
        val values: Array<Direction> get() = enumValues()
    }
}

/** Sealed hierarchy of in-game actions produced by touch input. */
sealed interface GameAction {
    /** Player tapped entity at collectibles[index] */
    data class TapEntity(val diskIndex: Int) : GameAction

    /** Player attempted a dash (directional swipe) in MAZE_RUN. */
    data class Dash(val dir: Direction) : GameAction

    /** Player delivered a classify choice for a pending decision. */
    data class Classify(val action: ClassifyAction) : GameAction
}

/** Touch-specific tuning parameters (no dependency on AudienceMode to avoid cycles). */
data class TouchTuning(
    val speedMultiplier: Double,
    val decisionSeconds: Double?,
    val spawnRateMultiplier: Double,
) {
    companion object {
        val STANDARD = TouchTuning(1.0, null, 1.0)
        val KIDS = TouchTuning(1.15, 8.0, 1.2)
        val SENIORS = TouchTuning(0.6, 18.0, 0.8)
    }
}

data class MazeConfig(val level: Int) {
    val layoutIndex: Int get() = level % MazeLayouts.LAYOUTS.size
}
