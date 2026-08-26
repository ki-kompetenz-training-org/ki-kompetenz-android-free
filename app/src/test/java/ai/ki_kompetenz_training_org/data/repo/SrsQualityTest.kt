package ai.ki_kompetenz_training_org.data.repo

import org.junit.Assert.*
import org.junit.Test

class SrsQualityTest {

    @Test
    fun `AGAIN has value 1`() {
        assertEquals(1, SrsQuality.AGAIN.value)
    }

    @Test
    fun `HARD has value 2`() {
        assertEquals(2, SrsQuality.HARD.value)
    }

    @Test
    fun `GOOD has value 3`() {
        assertEquals(3, SrsQuality.GOOD.value)
    }

    @Test
    fun `EASY has value 4`() {
        assertEquals(4, SrsQuality.EASY.value)
    }

    @Test
    fun `PERFECT has value 5`() {
        assertEquals(5, SrsQuality.PERFECT.value)
    }

    @Test
    fun `fromValue returns correct quality for 1`() {
        assertEquals(SrsQuality.AGAIN, SrsQuality.fromValue(1))
    }

    @Test
    fun `fromValue returns correct quality for 5`() {
        assertEquals(SrsQuality.PERFECT, SrsQuality.fromValue(5))
    }

    @Test
    fun `fromValue returns GOOD for unknown value`() {
        assertEquals(SrsQuality.GOOD, SrsQuality.fromValue(99))
    }

    @Test
    fun `fromValue returns GOOD for 0`() {
        assertEquals(SrsQuality.GOOD, SrsQuality.fromValue(0))
    }

    @Test
    fun `fromValue returns GOOD for negative`() {
        assertEquals(SrsQuality.GOOD, SrsQuality.fromValue(-1))
    }

    @Test
    fun `all qualities have non-empty emoji`() {
        SrsQuality.entries.forEach { q ->
            assertTrue("Quality ${q.name} has empty emoji", q.emoji.isNotBlank())
        }
    }

    @Test
    fun `all qualities have non-empty label`() {
        SrsQuality.entries.forEach { q ->
            assertTrue("Quality ${q.name} has empty label", q.label.isNotBlank())
        }
    }

    @Test
    fun `qualities are ordered by value ascending`() {
        val values = SrsQuality.entries.map { it.value }
        assertEquals(listOf(1, 2, 3, 4, 5), values)
    }

    @Test
    fun `SrsSession progress 0 of 0 returns 0`() {
        assertEquals(0f, SrsSession.progress(0, 0))
    }

    @Test
    fun `SrsSession progress 5 of 10 returns 0_5`() {
        assertEquals(0.5f, SrsSession.progress(5, 10))
    }

    @Test
    fun `SrsSession progress 10 of 10 returns 1`() {
        assertEquals(1f, SrsSession.progress(10, 10))
    }

    @Test
    fun `SrsSession progress 15 of 10 clamps to 1`() {
        assertEquals(1f, SrsSession.progress(15, 10))
    }

    @Test
    fun `SrsSession isFinished true when reviewed equals total`() {
        assertTrue(SrsSession.isFinished(10, 10))
    }

    @Test
    fun `SrsSession isFinished true when reviewed exceeds total`() {
        assertTrue(SrsSession.isFinished(11, 10))
    }

    @Test
    fun `SrsSession isFinished false when reviewed less than total`() {
        assertFalse(SrsSession.isFinished(5, 10))
    }

    @Test
    fun `SrsSession isFinished false when total is 0`() {
        assertFalse(SrsSession.isFinished(0, 0))
    }
}
