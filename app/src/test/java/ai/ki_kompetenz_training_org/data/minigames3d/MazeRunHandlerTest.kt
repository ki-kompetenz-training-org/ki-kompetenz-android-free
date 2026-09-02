/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native MazeRunHandler tests (JUnit4 + Truth).
 * Verifiziert Dash-Bewegung, Wand-/Bounds-Abweisung und die
 * Zell-Trigger (Goal / Bonus) des MAZE_RUN-Modus.
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Tests fuer [MazeRunHandler].
 *
 * Spielregeln (Quelle: MazeRunHandler.kt / MazeLayouts.kt):
 * - createState(MAZE_RUN, ...) setzt den Spieler auf die Startzelle der
 *   aktiven Layouts (mazeLevel 0 -> MazeLayouts.LAYOUTS[0], Start (1,1)).
 * - onDash bewegt den Spieler diskret um genau eine Zelle in [Direction];
 *   gegen Waende und ausserhalb der Grid-Grenzen wird der Zug verworfen
 *   (Position bleibt unveraendert).
 * - Goal-Zelle 'G' loest mit Content eine PendingDecision aus:
 *   diskIndex == -1, fromBonus == false, x/z = Goal-Spalte/-Zeile.
 * - Bonus-Zelle 'B' loest mit Content eine PendingDecision mit
 *   fromBonus == true aus.
 * - isRisk der PendingDecision ist immer konsistent zum zugrunde liegenden
 *   Statement (pd.isRisk == pd.statement.isRisk).
 * - Ohne Content (null) darf weder Goal- noch Bonus-Zelle eine Decision
 *   ausloesen (checkCellTrigger early-returns bei content == null).
 * - Solange pendingDecision != null wird onDash komplett ignoriert (Freeze).
 */
class MazeRunHandlerTest {

    private lateinit var content: LiteracyContentProvider
    private val rng: () -> Double = { 0.5 }
    private val tuning = TouchTuning.STANDARD

    @Before
    fun setup() {
        content = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Fakt-Text", "Fact text", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Risiko-Text", "Risk text", "Test", true)
        }
    }

    /** MAZE_RUN-State wie im Projektstandard: createState mit Content und fixem rng. */
    private fun mazeState(): Pair<GameState, ModeConfig> {
        val cfg = GameConfig.getModeConfig(GameMode.MAZE_RUN)
        val s = GameEngine.createState(GameMode.MAZE_RUN, content, rng, tuning)
        return s to cfg
    }

    private fun placeAt(s: GameState, row: Int, col: Int) {
        s.playerCellRow = row
        s.playerCellCol = col
    }

    private fun playerPos(s: GameState): Pair<Int, Int> = s.playerCellRow to s.playerCellCol

    // ========== Bewegung auf freier Zelle ==========

    @Test
    fun onDash_aufFreierZelle_bewegtSpielerUmEineZelle() {
        // Start (1,1) -> RIGHT auf (1,2) ('.'), freie Zelle
        val (s, cfg) = mazeState()
        val maze = s.maze!!
        placeAt(s, maze.startPos().first, maze.startPos().second)

        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, content, rng)

        assertThat(playerPos(s)).isEqualTo(1 to 2)
        assertWithMessage("Dash auf freie Zelle darf keine Decision ausloesen")
            .that(s.pendingDecision)
            .isNull()
    }

    @Test
    fun onDash_aufFreierZelle_inAlleVierRichtungen() {
        // Von (2,2) ('.') sind alle vier Nachbarzellen innerhalben ['#','G','B','.'] frei
        // begehbar und in Bounds; jede Richtung bewegt exakt eine Zelle.
        val (s, cfg) = mazeState()
        placeAt(s, 2, 2)

        MazeRunHandler.onDash(s, cfg, Direction.UP, content, rng)
        assertThat(playerPos(s)).isEqualTo(1 to 2)

        MazeRunHandler.onDash(s, cfg, Direction.DOWN, content, rng)
        assertThat(playerPos(s)).isEqualTo(2 to 2)

        MazeRunHandler.onDash(s, cfg, Direction.LEFT, content, rng)
        assertThat(playerPos(s)).isEqualTo(2 to 1)

        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, content, rng)
        assertThat(playerPos(s)).isEqualTo(2 to 2)
    }

    // ========== Wand-Abweisung ==========

    @Test
    fun onDash_gegenWand_aendertZelleNicht() {
        // (1,1) -> UP auf (0,1) ('#') - Wand, Zug wird verworfen
        val (s, cfg) = mazeState()
        placeAt(s, 1, 1)

        MazeRunHandler.onDash(s, cfg, Direction.UP, content, rng)

        assertWithMessage("Dash gegen Wand darf die Position nicht aendern")
            .that(playerPos(s))
            .isEqualTo(1 to 1)
        assertThat(s.pendingDecision).isNull()
    }

    // ========== Bounds-Abweisung ==========

    @Test
    fun onDash_ausserhalbDerBounds_aendertNichts() {
        // Spieler manuell auf das Grid-Rand-Mauerwerk gesetzt; jeder Dash, der die
        // Zelle aus [0, rows) x [0, cols) hinausfuehren wuerde, muss verworfen werden.
        // (Die Wand-Zellen sind hier nur bequeme Positionen am Rand; onDash prueft
        // ausschliesslich die Zielzelle gegen rows/cols.)
        val (s, cfg) = mazeState()
        val maze = s.maze!!

        // Links raus: (1,0) -> LEFT -> (1,-1)
        placeAt(s, 1, 0)
        MazeRunHandler.onDash(s, cfg, Direction.LEFT, content, rng)
        assertWithMessage("Dash ueber die linke Kante darf nichts aendern")
            .that(playerPos(s))
            .isEqualTo(1 to 0)

        // Oben raus: (0,2) -> UP -> (-1,2)
        placeAt(s, 0, 2)
        MazeRunHandler.onDash(s, cfg, Direction.UP, content, rng)
        assertWithMessage("Dash ueber die obere Kante darf nichts aendern")
            .that(playerPos(s))
            .isEqualTo(0 to 2)

        // Rechts raus: (2,4) -> RIGHT -> (2,5)
        placeAt(s, 2, 4)
        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, content, rng)
        assertWithMessage("Dash ueber die rechte Kante darf nichts aendern")
            .that(playerPos(s))
            .isEqualTo(2 to 4)

        // Unten raus: (4,2) -> DOWN -> (5,2)
        placeAt(s, 4, 2)
        MazeRunHandler.onDash(s, cfg, Direction.DOWN, content, rng)
        assertWithMessage("Dash ueber die untere Kante darf nichts aendern")
            .that(playerPos(s))
            .isEqualTo(4 to 2)

        assertThat(s.pendingDecision).isNull()
    }

    // ========== Goal-Trigger ==========

    @Test
    fun onDash_aufGoalZelle_mitContent_setztPendingDecision() {
        // Goal (3,2): von (2,2) nach DOWN -> (3,2)
        val (s, cfg) = mazeState()
        val maze = s.maze!!
        val goal = maze.goalPositions().first()
        placeAt(s, goal.first - 1, goal.second)

        MazeRunHandler.onDash(s, cfg, Direction.DOWN, content, rng)

        val pd = s.pendingDecision
        assertWithMessage("Dash auf Goal-Zelle muss eine Decision ausloesen")
            .that(pd)
            .isNotNull()
        assertWithMessage("Maze-Decisions tragen keinen Disk-Index (diskIndex -1)")
            .that(pd!!.diskIndex)
            .isEqualTo(-1)
        assertWithMessage("Goal-Decision ist keine Bonus-Decision")
            .that(pd.fromBonus)
            .isFalse()
        assertWithMessage("Gespielte Zelle muss der Goal-Zelle entsprechen")
            .that(pd.x to pd.z)
            .isEqualTo(goal.second.toDouble() to goal.first.toDouble())
        assertWithMessage("pd.isRisk muss zum vergebenen Statement passen")
            .that(pd.isRisk)
            .isEqualTo(pd.statement.isRisk)
        assertThat(pd.timerMax).isEqualTo(cfg.decisionSeconds)
    }

    @Test
    fun onDash_aufGoalZelle_mitContent_verwendetRngFuerRiskOderFact() {
        // rng = {0.5} -> Bedingung rng() < 0.5 ist false -> es wird randomFact gewaehlt
        // (isRisk false). Das Dokumentiert, dass die Statement-Wahl deterministisch
        // anhand von rng erfolgt und isRisk zum Statement passt.
        val (s, cfg) = mazeState()
        val maze = s.maze!!
        val goal = maze.goalPositions().first()
        placeAt(s, goal.first - 1, goal.second)

        MazeRunHandler.onDash(s, cfg, Direction.DOWN, content, rng)

        val pd = s.pendingDecision!!
        assertWithMessage("mit rng 0.5 muss der Fact-Zweig gewaehlt werden")
            .that(pd.statement.isRisk)
            .isFalse()
        assertThat(pd.isRisk).isFalse()
        assertThat(pd.isRisk).isEqualTo(pd.statement.isRisk)
    }

    // ========== Bonus-Trigger ==========

    @Test
    fun onDash_aufBonusZelle_mitContent_setztBonusDecision() {
        // Bonus (1,3): von (1,2) nach RIGHT -> (1,3)
        val (s, cfg) = mazeState()
        // Bonus-Zelle aus dem Layout: LAYOUTS[0] hat ein B bei (1,3)
        val bonusCell = 1 to 3
        placeAt(s, bonusCell.first, bonusCell.second - 1)

        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, content, rng)

        val pd = s.pendingDecision
        assertWithMessage("Dash auf Bonus-Zelle muss eine Decision ausloesen")
            .that(pd)
            .isNotNull()
        assertWithMessage("Bonus-Decision muss fromBonus == true setzen")
            .that(pd!!.fromBonus)
            .isTrue()
        assertWithMessage("Bonus-Decisions tragen ebenfalls diskIndex -1")
            .that(pd.diskIndex)
            .isEqualTo(-1)
        assertWithMessage("pd.isRisk muss zum vergebenen Statement passen")
            .that(pd.isRisk)
            .isEqualTo(pd.statement.isRisk)
        assertThat(pd.timerMax).isEqualTo(cfg.decisionSeconds)
    }

    // ========== Ohne Content ==========

    /**
     * Semantik: checkCellTrigger early-returned bei content == null.
     * Ohne Content darf ein Goal-Dash den Spieler zwar bewegen, aber KEINE
     * Decision ausloesen - sonst haenge das Quiz ab einer leeren
     * Fragebank. (Analog zum Freeze-Guard: ohne Content wird nie entschieden.)
     */
    @Test
    fun onDash_aufGoalZelle_ohneContent_setztKeineDecision() {
        val (s, cfg) = mazeState()
        val maze = s.maze!!
        val goal = maze.goalPositions().first()
        placeAt(s, goal.first - 1, goal.second)

        MazeRunHandler.onDash(s, cfg, Direction.DOWN, null, rng)

        assertWithMessage("Ohne Content darf Goal-Dash keine Decision ausloesen")
            .that(s.pendingDecision)
            .isNull()
        assertWithMessage("Bewegung selbst bleibt auch ohne Content erlaubt")
            .that(playerPos(s))
            .isEqualTo(goal.first to goal.second)
    }

    @Test
    fun onDash_aufBonusZelle_ohneContent_setztKeineDecision() {
        val (s, cfg) = mazeState()
        placeAt(s, 1, 2)

        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, null, rng)

        assertWithMessage("Ohne Content darf Bonus-Dash keine Decision ausloesen")
            .that(s.pendingDecision)
            .isNull()
    }

    // ========== Freeze bei pendingDecision ==========

    @Test
    fun onDash_beiPendingDecision_wirdIgnoriert() {
        // Freeze-Invariante: solange pendingDecision != null ist onDash wirkungslos,
        // nur der Decision-Timer tickt weiter (Quelle: MazeRunHandler.onDash Guard).
        val (s, cfg) = mazeState()
        placeAt(s, 1, 1)
        val stmt = LiteracyStatement("T", "T", "Test", false)
        s.pendingDecision = PendingDecision(
            statement = stmt,
            timerMax = cfg.decisionSeconds,
            timer = cfg.decisionSeconds,
            x = 1.0,
            z = 1.0,
            fromBonus = false,
            diskIndex = -1,
            isRisk = false,
        )

        MazeRunHandler.onDash(s, cfg, Direction.RIGHT, content, rng)

        assertWithMessage("Dash waehrend pendingDecision muss ignoriert werden")
            .that(playerPos(s))
            .isEqualTo(1 to 1)
        assertWithMessage("pendingDecision muss unveraendert bestehen bleiben")
            .that(s.pendingDecision)
            .isNotNull()
        assertThat(s.pendingDecision!!.statement).isSameInstanceAs(stmt)
    }
}
