package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Test

class GameGeometryTest {

    @Test
    fun clampToArena_keeps_point_inside() {
        val r = GameGeometry.clampToArena(1.0, 1.0, 10.0, 0.5)
        assertEquals(1.0, r.x, 1e-9)
        assertEquals(1.0, r.z, 1e-9)
    }

    @Test
    fun clampToArena_pulls_point_inside_circle() {
        val r = GameGeometry.clampToArena(20.0, 0.0, 10.0, 0.5)
        assertEquals(9.5, r.x, 1e-9)
        assertEquals(0.0, r.z, 1e-9)
    }

    @Test
    fun clampToArena_scales_diagonally() {
        // (8,8) is 11.31 from center -> pulled back to radius 10
        val r = GameGeometry.clampToArena(8.0, 8.0, 10.0, 0.0)
        val dist = kotlin.math.hypot(r.x, r.z)
        assertEquals(10.0, dist, 1e-6)
    }

    @Test
    fun dist2D_horizontal() {
        assertEquals(5.0, GameGeometry.dist2D(0.0, 0.0, 5.0, 0.0), 1e-9)
    }

    @Test
    fun dist2D_diagonal() {
        assertEquals(5.0, GameGeometry.dist2D(0.0, 0.0, 3.0, 4.0), 1e-9)
    }

    @Test
    fun overlaps_true_when_touching() {
        assertTrue(GameGeometry.overlaps(0.0, 0.0, 1.0, 2.0, 0.0, 1.0))
    }

    @Test
    fun overlaps_false_when_far() {
        assertFalse(GameGeometry.overlaps(0.0, 0.0, 1.0, 10.0, 0.0, 1.0))
    }

    @Test
    fun circleRectOverlap_detects_inside() {
        assertTrue(GameGeometry.circleRectOverlap(0.0, 0.0, 0.5, RectWall(0.0, 0.0, 1.0, 1.0)))
    }

    @Test
    fun circleRectOverlap_false_when_far() {
        assertFalse(GameGeometry.circleRectOverlap(5.0, 5.0, 0.5, RectWall(0.0, 0.0, 1.0, 1.0)))
    }

    @Test
    fun resolveWalls_pushes_out_of_wall() {
        val wall = RectWall(0.0, 0.0, 1.0, 0.5)
        val r = GameGeometry.resolveWalls(0.0, 0.0, 0.5, listOf(wall))
        val inside = GameGeometry.circleRectOverlap(r.x, r.z, 0.5, wall)
        assertFalse(inside)
    }

    @Test
    fun resolveWalls_no_overlap_when_clear() {
        val r = GameGeometry.resolveWalls(5.0, 5.0, 0.5, listOf(RectWall(0.0, 0.0, 1.0, 1.0)))
        assertEquals(5.0, r.x, 1e-9)
        assertEquals(5.0, r.z, 1e-9)
    }

    @Test
    fun randomSpawn_avoids_player_radius() {
        val p = GameGeometry.randomSpawn(
            radius = 10.0,
            minDistFromPlayer = 5.0,
            px = 0.0, pz = 0.0,
            rng = { 0.5 },
        )
        assertTrue(GameGeometry.dist2D(p.x, p.z, 0.0, 0.0) >= 4.9)
    }

    @Test
    fun bounceArena_reflects_velocity() {
        val d = Disk(x = 11.0, z = 0.0, r = 0.5, vx = 1.0, vz = 0.0)
        GameGeometry.bounceArena(d, 10.0)
        assertTrue(kotlin.math.abs(d.x) <= 10.0)
        assertTrue(d.vx < 0)
    }

    @Test
    fun edgeSpawn_returns_point_on_edge() {
        val e = GameGeometry.edgeSpawn(
            GameConfig.MODES[GameMode.ORB_HUNT]!!,
            rng = { 0.5 },
            speedScale = 1.0,
        )
        val dist = kotlin.math.hypot(e.x, e.z)
        assertTrue(dist > 8.0)
        assertTrue(dist <= 11.0)
    }
}
