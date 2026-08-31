package ai.ki_kompetenz_training_org.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AudienceModeTest {

    @Test
    fun `fromKey maps storage keys`() {
        assertEquals(AudienceMode.STANDARD, AudienceMode.fromKey("standard"))
        assertEquals(AudienceMode.KIDS, AudienceMode.fromKey("kids"))
        assertEquals(AudienceMode.SENIORS, AudienceMode.fromKey("seniors"))
    }

    @Test
    fun `fromKey falls back to standard`() {
        assertEquals(AudienceMode.STANDARD, AudienceMode.fromKey(null))
        assertEquals(AudienceMode.STANDARD, AudienceMode.fromKey("bogus"))
    }

    @Test
    fun `font scale factor is 1_15 only for seniors`() {
        assertEquals(1.15f, AudienceMode.SENIORS.fontScaleFactor, 0.001f)
        assertEquals(1.0f, AudienceMode.STANDARD.fontScaleFactor, 0.001f)
        assertEquals(1.0f, AudienceMode.KIDS.fontScaleFactor, 0.001f)
    }

    @Test
    fun `home cards per mode - standard shows both cards`() {
        assertEquals(listOf("forkids", "forseniors"), AudienceMode.STANDARD.homeCardIds())
    }

    @Test
    fun `home cards per mode - kids shows one prominent card`() {
        assertEquals(listOf("forkids"), AudienceMode.KIDS.homeCardIds())
        assertEquals(listOf("forseniors"), AudienceMode.SENIORS.homeCardIds())
    }

    @Test
    fun `touch target is 56dp for seniors, 48dp otherwise`() {
        assertEquals(56, AudienceMode.SENIORS.minTouchTargetDp)
        assertEquals(48, AudienceMode.STANDARD.minTouchTargetDp)
        assertEquals(48, AudienceMode.KIDS.minTouchTargetDp)
    }
}
