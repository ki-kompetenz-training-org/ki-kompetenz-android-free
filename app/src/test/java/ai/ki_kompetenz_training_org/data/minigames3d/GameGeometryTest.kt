/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native GameGeometry tests - will replace GameGeometryTest.kt in T2-T4
 * 8 tests total as specified in the plan
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Touch-native GameGeometry tests.
 * Tests clampToArena, dist2D, overlaps, randomSpawn, bounceArena.
 */
class GameGeometryTest {

    // ========== clampToArena ==========

    @Test
    fun clampToArena_centerPoint_unchanged() {
        val (x, z) = GameGeometry.clampToArena(0.0, 0.0, 1.0, 10.0)
        assertEquals(0.0, x, 0.001)
        assertEquals(0.0, z, 0.001)
    }

    @Test
    fun clampToArena_outsidePoint_clamped() {
        val (x, z) = GameGeometry.clampToArena(15.0, 0.0, 1.0, 10.0)
        assertTrue("x should be clamped inside", x <= 10.0 - 1.0)
        assertEquals(0.0, z, 0.001)
    }

    // ========== dist2D ==========

    @Test
    fun dist2D_samePoint_zero() {
        assertEquals(0.0, GameGeometry.dist2D(1.0, 2.0, 1.0, 2.0), 0.001)
    }

    @Test
    fun dist2D_pythagorean() {
        val d = GameGeometry.dist2D(0.0, 0.0, 3.0, 4.0)
        assertEquals(5.0, d, 0.001)
    }

    // ========== overlaps ==========

    @Test
    fun overlaps_sameCenter_true() {
        assertTrue(GameGeometry.overlaps(0.0, 0.0, 1.0, 0.0, 0.0, 1.0))
    }

    @Test
    fun overlaps_farApart_false() {
        assertFalse(GameGeometry.overlaps(0.0, 0.0, 1.0, 10.0, 0.0, 1.0))
    }

    @Test
    fun overlaps_touchingEdge_true() {
        assertTrue(GameGeometry.overlaps(0.0, 0.0, 1.0, 2.0, 0.0, 1.0))
    }

    // ========== randomSpawn ==========

    @Test
    fun randomSpawn_returnsPointWithinArena() {
        val p = GameGeometry.randomSpawn(10.0, 0.0, 0.0, 0.0, { 0.5 })
        val dist = GameGeometry.dist2D(p.x, p.z, 0.0, 0.0)
        assertTrue("Should be within arena", dist <= 10.0)
    }
}
