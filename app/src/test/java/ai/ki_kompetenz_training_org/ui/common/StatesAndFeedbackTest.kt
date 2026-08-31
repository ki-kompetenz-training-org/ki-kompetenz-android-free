package ai.ki_kompetenz_training_org.ui.common

import ai.ki_kompetenz_training_org.ui.theme.KiTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkeletonVisibilityTest {

    @Test
    fun `skeleton shows only while loading with no content`() {
        assertTrue(SkeletonVisibility.shouldShow(loading = true, items = 0))
        assertFalse(SkeletonVisibility.shouldShow(loading = true, items = 5))
        assertFalse(SkeletonVisibility.shouldShow(loading = false, items = 0))
        assertFalse(SkeletonVisibility.shouldShow(loading = false, items = 5))
    }
}

class HapticsPolicyTest {

    @Test
    fun `system haptic setting resolved - off disables feedback`() {
        // Settings.System.HAPTIC_FEEDBACK_ENABLED: 0 = off, 1 = on, -1 = unknown
        assertFalse(Haptics.isSystemEnabled(0))
        assertTrue(Haptics.isSystemEnabled(1))
        assertTrue(Haptics.isSystemEnabled(-1))
    }
}

class KiTokensTest {

    @Test
    fun `token values are stable`() {
        assertEquals(16f, KiTokens.CardRadiusLarge.value, 0.001f)
        assertEquals(14f, KiTokens.CardRadiusCompact.value, 0.001f)
        assertEquals(16f, KiTokens.ScreenPadding.value, 0.001f)
        assertEquals(10f, KiTokens.CardGap.value, 0.001f)
    }
}
