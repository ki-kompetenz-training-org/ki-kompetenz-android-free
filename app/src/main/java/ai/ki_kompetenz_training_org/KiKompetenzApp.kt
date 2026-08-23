package ai.ki_kompetenz_training_org

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.NetworkModule
import ai.ki_kompetenz_training_org.data.connectivity.ConnectivityObserver
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.prefs.TokenStore
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository
import ai.ki_kompetenz_training_org.notification.NotificationHelper
import ai.ki_kompetenz_training_org.notification.SrsReminderWorker
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Application class with service locator.
 * Production hardening:
 * - StrictMode in DEBUG for catching accidental disk/network I/O on main thread
 */
class KiKompetenzApp : Application() {

    lateinit var api: ApiService
        private set
    lateinit var db: AppDatabase
        private set
    lateinit var tokenStore: TokenStore
        private set
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var connectivityObserver: ConnectivityObserver
        private set
    lateinit var contentRepository: ContentRepository
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var premiumRepository: PremiumRepository
        private set
    lateinit var teamRepository: TeamRepository
        private set
    lateinit var gamificationRepository: GamificationRepository
        private set
    lateinit var srsRepository: SrsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        // Set up crash protection - DSGVO compliant: log to local file only, no external reporting
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))

        db = AppDatabase.get(this)
        tokenStore = TokenStore(this)
        settingsStore = SettingsStore(this)
        connectivityObserver = ConnectivityObserver(this)
        api = NetworkModule.createApiService(this)
        contentRepository = ContentRepository(api, db)
        authRepository = AuthRepository(tokenStore)
        premiumRepository = PremiumRepository(api)
        teamRepository = TeamRepository(api)
        gamificationRepository = GamificationRepository(db, this)
        srsRepository = SrsRepository(api)

        // Create notification channels early
        NotificationHelper.createChannels(this)

        // Schedule daily SRS reminder check (only when network is available)
        scheduleSrsReminders()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    private fun scheduleSrsReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SrsReminderWorker>(
            24, TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setInitialDelay(6, TimeUnit.HOURS) // First check after 6h
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "srs_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    /**
     * Crash handler that logs uncaught exceptions to a local file.
     * DSGVO compliant: NO external reporting, all data stays on device.
     */
    private class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

        private val TAG = "CrashHandler"
        private val CRASH_LOG_DIR = "crash_logs"
        private val CRASH_LOG_FILE = "crash_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.log"
        private val maxLogs = 5

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            try {
                // Log to Android logging system
                Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)

                // Write crash log to local file
                writeCrashLog(thread, throwable)

                // Clean up old logs
                cleanupOldLogs()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write crash log", e)
            }

            // Chain to default handler to ensure app terminates properly
            // But we don't want to use the default handler as it may show system dialog
            // Instead, we just terminate the app gracefully
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }

        private fun writeCrashLog(thread: Thread, throwable: Throwable) {
            val logDir = File(context.filesDir, CRASH_LOG_DIR)
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            val logFile = File(logDir, CRASH_LOG_FILE)

            try {
                BufferedWriter(FileWriter(logFile, true)).use { writer ->
                    // Write header
                    writer.write("=== CRASH REPORT ===\n")
                    writer.write("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                    writer.write("App: ${context.packageName}\n")
                    writer.write("Android: ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})\n")
                    writer.write("Device: ${android.os.Build.MODEL} (${android.os.Build.MANUFACTURER})\n")
                    writer.write("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
                    writer.write("Thread: ${thread.name} (id=${thread.id}, priority=${thread.priority})\n")
                    writer.write("\n")

                    // Write stack trace
                    writer.write("=== STACK TRACE ===\n")
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    throwable.printStackTrace(pw)
                    writer.write(sw.toString())
                    writer.write("\n")

                    // Write cause if available
                    if (throwable.cause != null) {
                        writer.write("=== CAUSED BY ===\n")
                        throwable.cause?.printStackTrace(pw)
                        writer.write(sw.toString())
                        writer.write("\n")
                    }

                    writer.write("=== END CRASH REPORT ===\n")
                }

                Log.i(TAG, "Crash log written to: ${logFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write crash log to file", e)
            }
        }

        private fun cleanupOldLogs() {
            val logDir = File(context.filesDir, CRASH_LOG_DIR)
            if (logDir.exists() && logDir.isDirectory) {
                val logs = logDir.listFiles()?.sortedBy { it.lastModified() } ?: return
                if (logs.size > maxLogs) {
                    (0 until logs.size - maxLogs).forEach { index ->
                        logs[index].delete()
                    }
                }
            }
        }
    }

    companion object {
        fun from(context: Context): KiKompetenzApp =
            context.applicationContext as KiKompetenzApp
    }
}
