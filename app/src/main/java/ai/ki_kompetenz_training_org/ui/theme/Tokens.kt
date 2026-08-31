package ai.ki_kompetenz_training_org.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shared layout tokens (ux-polish-pack / states-and-feedback).
 * Screens use these instead of scattered corner/padding literals.
 */
object KiTokens {
    /** Large card corner radius (hero cards, KiBot card). */
    val CardRadiusLarge = 16.dp

    /** Compact card corner radius (grid rows, list cards). */
    val CardRadiusCompact = 14.dp

    /** Standard horizontal screen padding. */
    val ScreenPadding = 16.dp

    /** Vertical gap between stacked cards. */
    val CardGap = 10.dp
}

/** Convenience for shape usage. */
fun KiTokens.largeShape(): RoundedCornerShape = RoundedCornerShape(CardRadiusLarge)

fun KiTokens.compactShape(): RoundedCornerShape = RoundedCornerShape(CardRadiusCompact)
