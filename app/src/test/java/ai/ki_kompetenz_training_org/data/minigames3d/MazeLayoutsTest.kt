/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MazeLayoutsTest {

    @Test fun layout0_is5x5() {
        val g = MazeLayouts.LAYOUTS[0]
        assertEquals(5, g.rows)
        assertEquals(5, g.cols)
    }

    @Test fun layout1_is7x7() {
        val g = MazeLayouts.LAYOUTS[1]
        assertEquals(7, g.rows)
        assertEquals(7, g.cols)
    }

    @Test fun layout2_is7x7() {
        val g = MazeLayouts.LAYOUTS[2]
        assertEquals(7, g.rows)
        assertEquals(7, g.cols)
    }

    @Test fun layout0_startAt_1_1() {
        val g = MazeLayouts.LAYOUTS[0]
        assertEquals(1 to 1, g.startPos())
    }

    @Test fun layout0_goalAt_3_2() {
        val g = MazeLayouts.LAYOUTS[0]
        assertTrue(g.isGoal(3, 2))
        assertFalse(g.isGoal(3, 3))
    }

    @Test fun layout0_hasTwoBonus() {
        val g = MazeLayouts.LAYOUTS[0]
        val b = listOf(1 to 3, 3 to 1)
        b.forEach { (r, c) -> assertTrue("B at ($r,$c)", g.isBonus(r, c)) }
    }

    @Test fun layout0_wallCount() {
        val g = MazeLayouts.LAYOUTS[0]
        var walls = 0
        for (r in 0 until g.rows) for (c in 0 until g.cols) if (g.isWall(r, c)) walls++
        assertEquals(16, walls)
    }

    @Test fun layout_cyclesByModulo() {
        // layout() returns reference from LAYOUTS list by index
        assertEquals(MazeLayouts.LAYOUTS[0].rows, MazeLayouts.layout(0).rows)
        assertEquals(MazeLayouts.LAYOUTS[1].rows, MazeLayouts.layout(1).rows)
        assertEquals(MazeLayouts.LAYOUTS[2].rows, MazeLayouts.layout(2).rows)
        assertEquals(MazeLayouts.LAYOUTS[0].rows, MazeLayouts.layout(3).rows)
        assertEquals(MazeLayouts.LAYOUTS[1].rows, MazeLayouts.layout(100).rows)
        assertEquals(MazeLayouts.LAYOUTS[2].rows, MazeLayouts.layout(5).rows)
    }

    @Test fun grid_accessor_isCorrect() {
        val g = MazeLayouts.LAYOUTS[0]
        assertEquals('#', g[0, 0])
        assertEquals('S', g[1, 1])
        assertEquals('G', g[3, 2])
    }

    @Test fun walkable_excludesWalls() {
        val g = MazeLayouts.LAYOUTS[0]
        assertFalse(g.isWalkable(0, 0))
        assertTrue(g.isWalkable(1, 1))
    }
}
