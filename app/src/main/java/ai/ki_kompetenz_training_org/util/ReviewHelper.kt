package ai.ki_kompetenz_training_org.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * DSGVO-compliant in-app review helper.
 *
 * Wraps the Google Play In-App Review API (`com.google.android.play:review`). Triggers the
 * system review flow once after the user has completed [LESSON_THRESHOLD]
 * lessons. The "already shown" flag is persisted locally in SharedPreferences
 * — no data leaves the device beyond what the Play Review API itself requires.
 *
 * The review dialog UI is provided entirely by the system; this helper renders
 * no custom text and collects no analytics.
 */
object ReviewHelper {
    private const val TAG = "ReviewHelper"
    private const val PREFS_NAME = "kikompetenz_review"
    private const val KEY_REVIEW_SHOWN = "review_shown"

    /** Number of completed lessons required before the review prompt is shown. */
    const val LESSON_THRESHOLD = 3

    /**
     * Trigger the in-app review flow once the user has completed [LESSON_THRESHOLD]
     * lessons. Subsequent calls are no-ops once the flow has been requested.
     *
     * The "shown" flag is set synchronously before the async Play Task runs, so
     * recomposition or repeated calls cannot fire the flow more than once. The
     * Play API additionally enforces its own yearly quota.
     *
     * @param activity The host Activity (required by the Review API).
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

        // Mark synchronously to avoid re-entry from recomposition before the
        // async Task completes.
        prefs.edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                        .addOnFailureListener { e ->
                            Log.w(TAG, "launchReviewFlow failed", e)
                        }
                } else {
                    Log.w(TAG, "requestReviewFlow failed", task.exception)
                }
            }
        Log.i(TAG, "In-app review requested after $completedLessons completed lessons.")
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
