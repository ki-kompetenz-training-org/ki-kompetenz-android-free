package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.repo.SrsQuality
import ai.ki_kompetenz_training_org.data.repo.SrsSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsSessionTest {

    @Test
    fun `progress scales correctly`() {
        assertEquals(0f, SrsSession.progress(0, 10), 0.001f)
        assertEquals(0.5f, SrsSession.progress(5, 10), 0.001f)
        assertEquals(1f, SrsSession.progress(10, 10), 0.001f)
    }

    @Test
    fun `progress handles empty queue`() {
        assertEquals(0f, SrsSession.progress(0, 0), 0.001f)
        assertEquals(0f, SrsSession.progress(3, 0), 0.001f)
    }

    @Test
    fun `progress never exceeds 1`() {
        assertEquals(1f, SrsSession.progress(15, 10), 0.001f)
    }

    @Test
    fun `session finishes when all reviewed`() {
        assertFalse(SrsSession.isFinished(0, 10))
        assertFalse(SrsSession.isFinished(9, 10))
        assertTrue(SrsSession.isFinished(10, 10))
        assertFalse(SrsSession.isFinished(0, 0))
    }

    @Test
    fun `quality labels map correctly`() {
        assertEquals("Wieder vergessen", SrsQuality.fromValue(1).label)
        assertEquals("Schwer", SrsQuality.fromValue(2).label)
        assertEquals("Gut", SrsQuality.fromValue(3).label)
        assertEquals("Leicht", SrsQuality.fromValue(4).label)
        assertEquals("Perfekt", SrsQuality.fromValue(5).label)
        // unknown values fall back to GOOD
        assertEquals(SrsQuality.GOOD, SrsQuality.fromValue(99))
        assertEquals(3, SrsQuality.GOOD.value)
    }
}