package ai.ki_kompetenz_training_org.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderSchedulerTest {

    private val zone = ZoneId.of("UTC")

    @Test
    fun `delay to later today`() {
        val now = ZonedDateTime.of(2026, 9, 9, 10, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(9), ReminderScheduler.nextTriggerDelay(19, 0, now))
    }

    @Test
    fun `delay with minutes component`() {
        val now = ZonedDateTime.of(2026, 9, 9, 10, 30, 0, 0, zone)
        assertEquals(Duration.ofHours(8).plusMinutes(30), ReminderScheduler.nextTriggerDelay(19, 0, now))
    }

    @Test
    fun `delay wraps to next day when target passed`() {
        val now = ZonedDateTime.of(2026, 9, 9, 20, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(23), ReminderScheduler.nextTriggerDelay(19, 0, now))
    }

    @Test
    fun `delay exactly at target wraps to tomorrow`() {
        val now = ZonedDateTime.of(2026, 9, 9, 19, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(24), ReminderScheduler.nextTriggerDelay(19, 0, now))
    }

    @Test
    fun `morning preset wraps correctly at late evening`() {
        val now = ZonedDateTime.of(2026, 9, 9, 22, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(11), ReminderScheduler.nextTriggerDelay(9, 0, now))
    }
}
