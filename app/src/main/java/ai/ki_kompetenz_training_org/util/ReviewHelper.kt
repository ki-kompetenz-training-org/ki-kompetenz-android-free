package ai.ki_kompetenz_training_org.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * DSGVO-compliant in-app review helper (F-Droid edition).
 *
 * Triggers a store-review intent once after the user has completed
 * [LESSON_THRESHOLD] lessons. The "already shown" flag is persisted locally in
 * SharedPreferences — no data leaves the device.
 *
 * Unlike the Play edition, this build carries NO Google Play dependency
 * (com.google.android.play:review is rejected by F-Droid's scanner). Instead
 * it opens the device's market app via a plain `market://` intent: sideloads
 * with Play land on the store listing, F-Droid devices with no market app
 * silently no-op.
 */
object ReviewHelper {
    private const val TAG = "ReviewHelper"
    private const val PREFS_NAME = "kikompetenz_review"
    private const val KEY_REVIEW_SHOWN = "review_shown"

    /** Number of completed lessons required before the review prompt is shown. */
    const val LESSON_THRESHOLD = 3

    /**
     * Trigger the review flow once the user has completed [LESSON_THRESHOLD]
     * lessons. Subsequent calls are no-ops once the flow has been requested.
     *
     * The "shown" flag is set synchronously before the intent is fired, so
     * recomposition or repeated calls cannot fire the flow more than once.
     *
     * @param activity The host Activity.
     * @param completedLessons The number of lessons the user has completed.
     * @return `true` if the review flow was requested, `false` otherwise.
     */
    fun maybeRequestReview(
        activity: Activity,
        completedLessons: Int,
    ): Boolean {
        if (completedLessons < LESSON_THRESHOLD) return false
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REVIEW_SHOWN, false)) return false

        // Mark synchronously to avoid re-entry from recomposition.
        prefs.edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()

        val market =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.packageName))
        try {
            activity.startActivity(market)
            Log.i(TAG, "Store review intent launched after $completedLessons completed lessons.")
        } catch (e: ActivityNotFoundException) {
            // No market app installed (typical F-Droid device) — silently skip.
            Log.i(TAG, "No market app for review intent; skipped.")
        }
        return true
    }
}

/**
 * Walks the [ContextWrapper] chain to find the hosting [Activity], or null
 * if none is available (e.g. in a preview or service context).
 */
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
