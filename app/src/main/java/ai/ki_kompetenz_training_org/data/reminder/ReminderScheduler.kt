package ai.ki_kompetenz_training_org.data.reminder

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.notification.SrsReminderWorker
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily SRS reminder worker at the user-chosen time of day.
 * Respects the opt-out toggle from SettingsStore. DSGVO: purely local,
 * no analytics — same endpoint the in-app SRS screen already uses.
 */
object ReminderScheduler {

    const val UNIQUE_WORK_NAME = "srs_reminders"

    /** Duration from now until the next occurrence of hour:minute (pure, testable). */
    fun nextTriggerDelay(hour: Int, minute: Int, now: ZonedDateTime): Duration {
        var next = now.with(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }

    /** Read settings and (re)schedule or cancel. Safe to call from any coroutine. */
    suspend fun apply(context: Context) {
        val app = context.applicationContext as KiKompetenzApp
        val enabled = app.settingsStore.reminderEnabled.first()
        val (hour, minute) = app.settingsStore.reminderTime.first()
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }
        val delay = nextTriggerDelay(hour, minute, ZonedDateTime.now())
        val request = PeriodicWorkRequestBuilder<SrsReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
