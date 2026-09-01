/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Cell-based maze layouts for MAZE_RUN.
 * S = start position
 * G = goal cell (triggers decision)
 * B = bonus cell (triggers bonus decision, consumed once)
 * # = wall
 * . = empty floor
 *
 * Player is always at integer (row, col).
 */
object MazeLayouts {

    /**
     * Maze grid. Cells are characters from the legend above.
     */
    data class MazeGrid(
        val rows: Int,
        val cols: Int,
        val cells: List<List<Char>>,
    ) {
        init {
            require(cells.size == rows) { "MazeGrid has ${cells.size} rows, expected $rows" }
            cells.forEachIndexed { r, row ->
                require(row.size == cols) { "Row $r has ${row.size} cols, expected $cols" }
            }
        }

        operator fun get(r: Int, c: Int): Char = cells[r][c]

        fun isWall(r: Int, c: Int): Boolean = get(r, c) == '#'
        fun isGoal(r: Int, c: Int): Boolean = get(r, c) == 'G'
        fun isBonus(r: Int, c: Int): Boolean = get(r, c) == 'B'
        fun isStart(r: Int, c: Int): Boolean = get(r, c) == 'S'
        fun isWalkable(r: Int, c: Int): Boolean = !isWall(r, c)

        /** Find the unique start cell coordinates. */
        fun startPos(): Pair<Int, Int> {
            for (r in 0 until rows) for (c in 0 until cols) if (isStart(r, c)) return r to c
            error("Maze layout has no start cell S")
        }

        /** Find all goal cell coordinates. */
        fun goalPositions(): List<Pair<Int, Int>> {
            val result = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until rows) for (c in 0 until cols) if (isGoal(r, c)) result.add(r to c)
            return result
        }
    }

    private fun parse(rows: Int, cols: Int, lines: List<String>): MazeGrid {
        require(lines.size == rows) { "parse: ${lines.size} lines != $rows" }
        val validChars = setOf('S', 'G', 'B', '#', '.')
        lines.forEachIndexed { r, line ->
            require(line.length == cols) { "Row $r length ${line.length} != $cols" }
            line.forEachIndexed { c, ch ->
                require(ch in validChars) { "Invalid maze char '$ch' at ($r,$c)" }
            }
        }
        val grid = MazeGrid(rows, cols, lines.map { it.toList() })
        require(grid.startPositions().size == 1) { "Maze must have exactly one S" }
        require(grid.goalPositions().isNotEmpty()) { "Maze must have at least one G" }
        return grid
    }

    private fun MazeGrid.startPositions() = buildList { forEachCell { r, c -> if (isStart(r, c)) add(r to c) } }
    private inline fun MazeGrid.forEachCell(block: (Int, Int) -> Unit) {
        for (r in 0 until rows) for (c in 0 until cols) block(r, c)
    }

    /**
     * Layout 0: 5 x 5 simple.
     * S at (1,1), G at (3,3), B at (1,3) and (3,1).
     */
    private fun layout0(): MazeGrid = parse(
        5, 5,
        listOf(
            "#####",
            "#S.B#",
            "#...#",
            "#BG.#",
            "#####",
        ),
    )

    /**
     * Layout 1: 7 x 7 medium.
     * S at (1,1), G at (5,5), B at (3,3) and (4,5).
     */
    private fun layout1(): MazeGrid = parse(
        7, 7,
        listOf(
            "#######",
            "#S....#",
            "#.###.#",
            "#.#B..#",
            "#.#.#B#",
            "#...#G#",
            "#######",
        ),
    )

    /**
     * Layout 2: 7 x 7 hard.
     * S at (1,1), G at (5,5), B at (3,2) and (3,4).
     * Hard path verified:
     * (1,1)->(1,2)->(2,2)->(3,2)->B -> (4,2)->(5,2)->(5,3)->(5,4)->B -> (5,5) G
     */
    private fun layout2(): MazeGrid = parse(
        7, 7,
        listOf(
            "#######",
            "#S#...#",
            "#.#.#.#",
            "#.B.B.#",
            "#.#.#.#",
            "#...#G#",
            "#######",
        ),
    )

    val LAYOUTS: List<MazeGrid> = listOf(layout0(), layout1(), layout2())

    fun layout(level: Int): MazeGrid = LAYOUTS[level % LAYOUTS.size]
    fun count(): Int = LAYOUTS.size
}
