package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Domain types for the 3D AI-literacy minigame engine.
 *
 * Pure Kotlin, no Android dependencies. Fully unit-testable.
 *
 * Three game modes:
 *  - orbHunt:    scan items, classify as fact or risk. Read the AI-literacy
 *                statement and decide correctly to score.
 *  - mazeRun:    steer through a maze to the goal, then answer an AI-literacy
 *                decision to bank the points.
 *  - truthSnipe: drifting facts & fakes; read the statement, shoot fakes,
 *                collect facts. Shooting a fact costs points.
 *
 * Individualization: each entity carries a [LiteracyStatement] from the question
 * bank. The [MasteryTracker] weights spawning toward weak domains so learners
 * practice what they do not know yet.
 */

enum class GameMode { ORB_HUNT, MAZE_RUN, TRUTH_SNIPE }

enum class EndReason { TIME, HEALTH }

data class Vec2(val x: Double, val z: Double)

/** A moving circular entity (orbs, hazards, bullets). Fields are var for in-place mutation. */
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

data class Player(
    var x: Double,
    var z: Double,
    var vx: Double,
    var vz: Double,
    var dir: Double,
    var invuln: Double,
)

/** One-shot classification input: FACT or RISK or null. */
enum class ClassifyAction { FACT, RISK }

data class InputState(
    val up: Boolean,
    val down: Boolean,
    val left: Boolean,
    val right: Boolean,
    val fire: Boolean,
    val classify: ClassifyAction? = null,
)

data class PendingDecision(
    val statement: LiteracyStatement,
    val timer: Double,
)

data class GameState(
    val mode: GameMode,
    var time: Double,
    var timeLeft: Double,
    var score: Int,
    var health: Int,
    val maxHealth: Int,
    val target: Int,
    val player: Player,
    val collectibles: MutableList<Disk>,
    val hazards: MutableList<Disk>,
    val bullets: MutableList<Disk>,
    val walls: List<RectWall>,
    var goal: Vec2?,
    var goalIndex: Int,
    var fireCd: Double,
    var ended: Boolean,
    var endReason: EndReason?,
    var won: Boolean,
    var justScored: Boolean,
    var justHit: Boolean,
    var justFired: Boolean,
    var hitX: Double,
    var hitZ: Double,
    var scoreX: Double,
    var scoreZ: Double,
    var scoreKind: Int,
    var hitKind: Int,
    var scannedIndex: Int,
    var scannedIsRisk: Boolean,
    var scannedKind: Int,
    var lastClassify: ClassifyResult?,
    var classifyStreak: Int,
    var pendingDecision: PendingDecision?,
    /** Per-game classification log for post-game mastery update. */
    val classifications: MutableList<ClassifyLog>,
)

data class ClassifyResult(val correct: Boolean, val kind: Int, val isRisk: Boolean)

data class ClassifyLog(
    val domain: String,
    val correct: Boolean,
    val statement: LiteracyStatement,
)

data class ModeConfig(
    val arenaRadius: Double,
    val duration: Double,
    val target: Int,
    val maxHealth: Int,
    val playerRadius: Double,
    val playerSpeed: Double,
    val collectRadius: Double,
    val collectPoints: Int,
    val hazardRadius: Double,
    val hazardSpeed: Double,
    val hazardPoints: Int,
    val initialCollect: Int,
    val initialHazard: Int,
    val hasWalls: Boolean,
    val hasGoal: Boolean,
    val hasBullets: Boolean,
    val bulletSpeed: Double,
    val fireCooldown: Double,
    val minCollect: Int,
    val minHazard: Int,
    val factKinds: Int,
    val riskKinds: Int,
)
