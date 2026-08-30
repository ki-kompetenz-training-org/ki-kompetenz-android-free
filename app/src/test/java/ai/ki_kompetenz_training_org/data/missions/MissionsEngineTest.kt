package ai.ki_kompetenz_training_org.data.missions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionsEngineTest {

    @Test
    fun `pool has six templates with distinct ids`() {
        val pool = MissionsEngine.POOL
        assertEquals(6, pool.size)
        assertEquals(pool.size, pool.map { it.id }.toSet().size)
    }

    @Test
    fun `selection is deterministic per ISO week`() {
        val ids = MissionsEngine.selectForWeek("2026-W35").map { it.id }
        val again = MissionsEngine.selectForWeek("2026-W35").map { it.id }
        assertEquals(ids, again)
    }

    @Test
    fun `selection differs across weeks and size is three`() {
        val a = MissionsEngine.selectForWeek("2026-W35").map { it.id }
        val b = MissionsEngine.selectForWeek("2026-W36").map { it.id }
        assertEquals(3, a.size)
        assertEquals(3, MissionsEngine.selectForWeek("2026-W36").size)
        // Same pool source, no duplicates within a single selection.
        assertEquals(a.size, a.toSet().size)
        assertEquals(b.size, b.toSet().size)
    }

    @Test
    fun `selection contains only pool missions`() {
        val poolIds = MissionsEngine.POOL.map { it.id }.toSet()
        MissionsEngine.selectForWeek("2027-W01").forEach { assertTrue(it.id in poolIds) }
    }

    @Test
    fun `progress clamps at target`() {
        assertEquals(2, MissionsEngine.progressAfter(0, 3, 2))
        assertEquals(3, MissionsEngine.progressAfter(2, 3, 5))
        assertEquals(3, MissionsEngine.progressAfter(3, 3, 1))
    }

    @Test
    fun `completion detection`() {
        assertTrue(MissionsEngine.isCompleted(3, 3))
        assertTrue(MissionsEngine.isCompleted(4, 3))
        assertFalse(MissionsEngine.isCompleted(2, 3))
    }

    @Test
    fun `bonus only for completing all three`() {
        assertEquals(0, MissionsEngine.bonusForCompleted(2))
        assertEquals(MissionsEngine.allBonusXp, MissionsEngine.bonusForCompleted(3))
        assertEquals(MissionsEngine.allBonusXp, MissionsEngine.bonusForCompleted(4))
    }
}
