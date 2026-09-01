package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.*

/**
 * Pure geometry & collision math for the 3D minigame engine.
 * No game logic — only spatial queries and collision response.
 */
object GameGeometry {

    /** Clamp a point to the circular arena (accounting for the entity radius). */
    fun clampToArena(x: Double, z: Double, radius: Double, entityRadius: Double = 0.0): Vec2 {
        val max = radius - entityRadius
        val r = hypot(x, z)
        if (r > max && r > 0) {
            val s = max / r
            return Vec2(x * s, z * s)
        }
        return Vec2(x, z)
    }

    /** Euclidean distance in the XZ plane. */
    fun dist2D(ax: Double, az: Double, bx: Double, bz: Double): Double =
        hypot(ax - bx, az - bz)

    /** True when two circles (centers + radii) overlap. */
    fun overlaps(ax: Double, az: Double, ar: Double, bx: Double, bz: Double, br: Double): Boolean =
        dist2D(ax, az, bx, bz) <= ar + br

    fun circleRectOverlap(x: Double, z: Double, r: Double, w: RectWall): Boolean {
        val cx = maxOf(w.x - w.w, minOf(x, w.x + w.w))
        val cz = maxOf(w.z - w.d, minOf(z, w.z + w.d))
        return dist2D(x, z, cx, cz) < r
    }

    /** Push a circle out of every wall it overlaps (iterated for corners). */
    fun resolveWalls(x: Double, z: Double, r: Double, walls: List<RectWall>): Vec2 {
        var px = x
        var pz = z
        for (iter in 0 until 3) {
            var moved = false
            for (w in walls) {
                val minX = w.x - w.w
                val maxX = w.x + w.w
                val minZ = w.z - w.d
                val maxZ = w.z + w.d
                val cx = maxOf(minX, minOf(px, maxX))
                val cz = maxOf(minZ, minOf(pz, maxZ))
                val dx = px - cx
                val dz = pz - cz
                val d2 = dx * dx + dz * dz
                if (d2 < r * r) {
                    val d = sqrt(d2)
                    if (d > 1e-6) {
                        val push = r - d
                        px = cx + (dx / d) * push
                        pz = cz + (dz / d) * push
                    } else {
                        val left = px - minX
                        val right = maxX - px
                        val top = pz - minZ
                        val bottom = maxZ - pz
                        val m = minOf(left, right, top, bottom)
                        if (m == left) px = minX - r
                        else if (m == right) px = maxX + r
                        else if (m == top) pz = minZ - r
                        else pz = maxZ + r
                    }
                    moved = true
                }
            }
            if (!moved) break
        }
        return Vec2(px, pz)
    }

    /**
     * Pick a random point inside a disc that is at least [minDistFromPlayer]
     * away from the player. Falls back opposite the player after bounded attempts.
     */
    fun randomSpawn(
        radius: Double,
        minDistFromPlayer: Double,
        px: Double,
        pz: Double,
        rng: () -> Double = { Math.random() },
    ): Vec2 {
        for (i in 0 until 50) {
            val ang = rng() * PI * 2
            val r = sqrt(rng()) * radius
            val x = cos(ang) * r
            val z = sin(ang) * r
            if (dist2D(x, z, px, pz) >= minDistFromPlayer) return Vec2(x, z)
        }
        val ang = atan2(pz, px) + PI
        return Vec2(cos(ang) * radius * 0.8, sin(ang) * radius * 0.8)
    }

    /** Bounce a disk off the circular arena boundary (reflects velocity). */
    fun bounceArena(d: Disk, maxR: Double) {
        val r = hypot(d.x, d.z)
        if (r > maxR && r > 0) {
            val nx = d.x / r
            val nz = d.z / r
            d.x = nx * maxR
            d.z = nz * maxR
            val dot = d.vx * nx + d.vz * nz
            d.vx -= 2 * dot * nx
            d.vz -= 2 * dot * nz
        }
    }

    /** Bounce a disk off axis-aligned walls (reflects velocity off the nearest face). */
    fun bounceWalls(d: Disk, walls: List<RectWall>, r: Double) {
        for (w in walls) {
            val minX = w.x - w.w
            val maxX = w.x + w.w
            val minZ = w.z - w.d
            val maxZ = w.z + w.d
            val cx = maxOf(minX, minOf(d.x, maxX))
            val cz = maxOf(minZ, minOf(d.z, maxZ))
            val dx = d.x - cx
            val dz = d.z - cz
            val d2 = dx * dx + dz * dz
            if (d2 < r * r) {
                if (d2 > 1e-6) {
                    val dist = sqrt(d2)
                    val nx = dx / dist
                    val nz = dz / dist
                    val push = r - dist
                    d.x = cx + nx * push
                    d.z = cz + nz * push
                    val dot = d.vx * nx + d.vz * nz
                    d.vx -= 2 * dot * nx
                    d.vz -= 2 * dot * nz
                } else {
                    val left = d.x - minX
                    val right = maxX - d.x
                    val top = d.z - minZ
                    val bottom = maxZ - d.z
                    val m = minOf(left, right, top, bottom)
                    if (m == left) { d.x = minX - r; d.vx = abs(d.vx) }
                    else if (m == right) { d.x = maxX + r; d.vx = -abs(d.vx) }
                    else if (m == top) { d.z = minZ - r; d.vz = abs(d.vz) }
                    else { d.z = maxZ + r; d.vz = -abs(d.vz) }
                }
            }
        }
    }

    /** Spawn position + velocity at the arena edge, heading inward. */
    fun edgeSpawn(cfg: ModeConfig, rng: () -> Double, speedScale: Double): EdgeSpawn {
        val ang = rng() * PI * 2
        val r = cfg.arenaRadius - 1
        val x = cos(ang) * r
        val z = sin(ang) * r
        val speed = cfg.chipSpeed * speedScale * (0.7 + rng() * 0.6)
        val ta = ang + PI + (rng() - 0.5) * 1.4
        return EdgeSpawn(x, z, cos(ta) * speed, sin(ta) * speed)
    }

    data class EdgeSpawn(val x: Double, val z: Double, val vx: Double, val vz: Double)
}
