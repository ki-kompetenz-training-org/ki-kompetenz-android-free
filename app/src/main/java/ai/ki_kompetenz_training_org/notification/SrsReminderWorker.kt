package ai.ki_kompetenz_training_org.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ai.ki_kompetenz_training_org.KiKompetenzApp

/**
 * Periodic background worker that checks for due SRS cards.
 * If due cards exist, shows a notification reminding the user to review.
 *
 * DSGVO: No analytics, no server tracking. Only calls the existing
 * /api/srs/due endpoint (same as in-app). No data is stored or sent
 * beyond what the app already does.
 */
class SrsReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? KiKompetenzApp ?: return Result.success()

        // Only check if user is logged in
        if (!app.authRepository.isLoggedIn()) {
            return Result.success()
        }

        return try {
            val result = app.srsRepository.getDueCards()
            result.onSuccess { cards ->
                if (cards.isNotEmpty()) {
                    NotificationHelper.showSrsReminder(applicationContext, cards.size)
                }
            }
            Result.success()
        } catch (e: Exception) {
            // Network errors are expected — don't retry aggressively
            Result.success()
        }
    }
}
