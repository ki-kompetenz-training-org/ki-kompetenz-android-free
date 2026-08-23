package ai.ki_kompetenz_training_org.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ai.ki_kompetenz_training_org.MainActivity
import ai.ki_kompetenz_training_org.R

/**
 * Creates notification channels and builds SRS reminder notifications.
 * No analytics, no tracking — purely local notifications (DSGVO-compliant).
 */
object NotificationHelper {

    const val CHANNEL_SRS_REMINDERS = "srs_reminders"
    const val CHANNEL_GENERAL = "general"
    const val NOTIF_SRS_REMINDER_ID = 1001

    /**
     * Create notification channels. Safe to call multiple times.
     * Must be called before posting notifications (API 26+).
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SRS_REMINDERS,
                context.getString(R.string.notif_channel_srs),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_srs_desc)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notif_channel_general),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notif_channel_general_desc)
            }
        )
    }

    /**
     * Show an SRS reminder notification: "You have N cards to review."
     */
    fun showSrsReminder(context: Context, dueCount: Int) {
        createChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openSrs", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = context.getString(R.string.notif_srs_title)
        val text = context.getString(R.string.notif_srs_body, dueCount)

        val notification = NotificationCompat.Builder(context, CHANNEL_SRS_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_SRS_REMINDER_ID, notification)
    }
}
