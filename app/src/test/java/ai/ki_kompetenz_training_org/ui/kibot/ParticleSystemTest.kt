package ai.ki_kompetenz_training_org.ui.kibot

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the pure particle helpers in ParticleSystem.kt
 * (spawnCelebrationParticles / spawnConfusedParticles / spawnSleepyParticles /
 * updateParticles). All logic is deterministic: spawn ranges are validated by
 * interval checks, physics by known-input integration, no randomness seeds or
 * Compose machinery required.
 */
class ParticleSystemTest {

    private val epsilon = 1e-4f

    // ------------------------------------------------------------------
    // spawnCelebrationParticles
    // ------------------------------------------------------------------

    @Test
    fun `spawnCelebrationParticles with 20 yields 20 particles with life 1 at y 0_5 moving upward`() {
        val particles = spawnCelebrationParticles(20)

        assertThat(particles).hasSize(20)
        particles.forEach { p ->
            // Fresh burst: full life, fixed origin, upward velocity.
            assertThat(p.life).isWithin(epsilon).of(1f)
            assertThat(p.y).isWithin(epsilon).of(0.5f)
            assertThat(p.vy).isLessThan(0f)
        }
    }

    @Test
    fun `spawnCelebrationParticles keeps x within 0_3 to 0_7, maxLife within 0_5 to 1_0 and size within 2 to 6`() {
        val particles = spawnCelebrationParticles(50)

        assertThat(particles).isNotEmpty()
        particles.forEach { p ->
            assertThat(p.x).isAtLeast(0.3f)
            assertThat(p.x).isAtMost(0.7f)
            assertThat(p.maxLife).isAtLeast(0.5f)
            assertThat(p.maxLife).isAtMost(1.0f)
            assertThat(p.size).isAtLeast(2f)
            assertThat(p.size).isAtMost(6f)
        }
    }

    @Test
    fun `spawnCelebrationParticles with explicit count 0 returns an empty list`() {
        assertThat(spawnCelebrationParticles(0)).isEmpty()
    }

    // ------------------------------------------------------------------
    // spawnConfusedParticles
    // ------------------------------------------------------------------

    @Test
    fun `spawnConfusedParticles with 5 yields 5 question marks at y 0_3 drifting slowly upward`() {
        val particles = spawnConfusedParticles(5)

        assertThat(particles).hasSize(5)
        particles.forEach { p ->
            assertThat(p.emoji).isEqualTo("?")
            assertThat(p.y).isWithin(epsilon).of(0.3f)
            // Gentle upward drift: vy in [-0.8, -0.3].
            assertThat(p.vy).isAtLeast(-0.8f)
            assertThat(p.vy).isAtMost(-0.3f)
            // Centered around the head region: x in [0.35, 0.65].
            assertThat(p.x).isAtLeast(0.35f)
            assertThat(p.x).isAtMost(0.65f)
        }
    }

    // ------------------------------------------------------------------
    // spawnSleepyParticles
    // ------------------------------------------------------------------

    @Test
    fun `spawnSleepyParticles with 3 yields 3 zzz particles with vx 0_2 at x 0_65 plus i times 0_05`() {
        val particles = spawnSleepyParticles(3)

        assertThat(particles).hasSize(3)
        particles.forEachIndexed { i, p ->
            assertThat(p.emoji).isEqualTo("💤")
            assertThat(p.vx).isWithin(epsilon).of(0.2f)
            // Staircase layout: x strictly increases with the index.
            assertThat(p.x).isWithin(epsilon).of(0.65f + i * 0.05f)
        }
        // Explicitly document the "x rises with index" invariant.
        assertThat(particles[1].x).isGreaterThan(particles[0].x)
        assertThat(particles[2].x).isGreaterThan(particles[1].x)
    }

    // ------------------------------------------------------------------
    // updateParticles — physics
    // ------------------------------------------------------------------

    @Test
    fun `updateParticles integrates position with velocity, applies gravity 1_5 per second and decays life`() {
        val particle = Particle(x = 1f, y = 2f, vx = 3f, vy = -4f, life = 1f, maxLife = 1f)

        val updated = updateParticles(listOf(particle), dt = 0.1f)

        assertThat(updated).hasSize(1)
        val p = updated[0]
        assertThat(p.x).isWithin(epsilon).of(1.3f)          // 1 + 3 * 0.1
        assertThat(p.y).isWithin(epsilon).of(1.6f)          // 2 + (-4) * 0.1
        assertThat(p.vy).isWithin(epsilon).of(-3.85f)       // -4 + 1.5 * 0.1 (gravity)
        assertThat(p.vx).isWithin(epsilon).of(3f)           // no horizontal acceleration
        assertThat(p.life).isWithin(epsilon).of(0.9f)       // 1 - 0.1
        assertThat(p.maxLife).isWithin(epsilon).of(1f)      // maxLife untouched by stepping
    }

    @Test
    fun `updateParticles removes particles whose remaining life drops to zero or below`() {
        val dying = Particle(x = 0f, y = 0f, vx = 0f, vy = 0f, life = 0.05f, maxLife = 1f)
        val surviving = Particle(x = 0f, y = 0f, vx = 0f, vy = 0f, life = 0.15f, maxLife = 1f)

        val updated = updateParticles(listOf(dying, surviving), dt = 0.1f)

        // dying: 0.05 - 0.1 = -0.05 <= 0 -> gone.
        // surviving: 0.15 - 0.1 = 0.05 -> kept.
        assertThat(updated).hasSize(1)
        assertThat(updated[0].life).isWithin(epsilon).of(0.05f)
    }

    @Test
    fun `updateParticles on an empty list returns an empty list`() {
        assertThat(updateParticles(emptyList(), dt = 0.1f)).isEmpty()
    }

    // ------------------------------------------------------------------
    // updateParticles — immutability & order
    // ------------------------------------------------------------------

    @Test
    fun `updateParticles does not mutate the input particles`() {
        val original = Particle(x = 1f, y = 2f, vx = 3f, vy = -4f, life = 1f, maxLife = 1f)
        val input = listOf(original)

        val updated = updateParticles(input, dt = 0.1f)

        // A new particle instance is produced; the original keeps its old values.
        assertThat(updated[0]).isNotSameInstanceAs(original)
        assertThat(original.x).isWithin(epsilon).of(1f)
        assertThat(original.y).isWithin(epsilon).of(2f)
        assertThat(original.vy).isWithin(epsilon).of(-4f)
        assertThat(original.life).isWithin(epsilon).of(1f)
    }

    @Test
    fun `updateParticles preserves the relative order of surviving particles`() {
        val a = Particle(x = 1f, y = 0f, vx = 0f, vy = 0f, life = 1f, maxLife = 1f)
        val dead = Particle(x = 2f, y = 0f, vx = 0f, vy = 0f, life = 0.05f, maxLife = 1f)
        val b = Particle(x = 3f, y = 0f, vx = 0f, vy = 0f, life = 1f, maxLife = 1f)

        val updated = updateParticles(listOf(a, dead, b), dt = 0.1f)

        assertThat(updated).hasSize(2)
        assertThat(updated[0].x).isWithin(epsilon).of(a.x)
        assertThat(updated[1].x).isWithin(epsilon).of(b.x)
    }
}
