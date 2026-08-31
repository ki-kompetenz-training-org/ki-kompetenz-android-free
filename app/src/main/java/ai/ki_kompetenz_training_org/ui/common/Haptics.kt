package ai.ki_kompetenz_training_org.ui.common

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Haptic feedback policy (ux-polish-pack / states-and-feedback).
 *
 * Feedback is fired via [View.performHapticFeedback] so the OS automatically
 * suppresses it when the user disabled haptics system-wide.
 */
object Haptics {

    /**
     * Resolves Settings.System.HAPTIC_FEEDBACK_ENABLED:
     * 0 = off (no feedback), 1 = on, -1 = unknown (assume on).
     */
    fun isSystemEnabled(setting: Int): Boolean = setting != 0

    /** Subtle tap for answer selection in quiz / fake-or-real. */
    fun answerTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

/** Pure visibility rule for skeleton placeholders. */
object SkeletonVisibility {
    /** Skeleton shows only during the first load (loading && nothing to show yet). */
    fun shouldShow(loading: Boolean, items: Int): Boolean = loading && items == 0
}
