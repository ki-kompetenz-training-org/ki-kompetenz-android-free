package ai.ki_kompetenz_training_org.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Audience density mode (ux-polish-pack / audience-modes):
 *
 * - STANDARD: default UI.
 * - KIDS: same layout as standard; home shows a single prominent kids card.
 * - SENIORS: 1.15x font scale (via LocalDensity) + 56dp primary touch targets.
 *
 * The card count on home MUST NOT grow in any mode: standard shows the two
 * audience cards, a special mode replaces them with ONE prominent card.
 */
enum class AudienceMode(val storageKey: String) {
    STANDARD("standard"),
    KIDS("kids"),
    SENIORS("seniors");

    /** Font scale multiplier applied via LocalDensity. */
    val fontScaleFactor: Float
        get() = if (this == SENIORS) 1.15f else 1.0f

    /** Home cards for this mode (never more than the two standard cards). */
    fun homeCardIds(): List<String> = when (this) {
        STANDARD -> listOf("forkids", "forseniors")
        KIDS -> listOf("forkids")
        SENIORS -> listOf("forseniors")
    }

    /** Minimum touch target in dp for primary home actions. */
    val minTouchTargetDp: Int
        get() = if (this == SENIORS) 56 else 48

    companion object {
        fun fromKey(key: String?): AudienceMode =
            entries.firstOrNull { it.storageKey == key } ?: STANDARD
    }
}

/** Provided from MainActivity state; defaults to [AudienceMode.STANDARD]. */
val LocalAudienceMode = staticCompositionLocalOf { AudienceMode.STANDARD }
