package ai.ki_kompetenz_training_org

import android.app.Application
import android.content.Context
import android.os.StrictMode
import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.NetworkModule
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.connectivity.ConnectivityObserver
import ai.ki_kompetenz_training_org.data.prefs.TokenStore
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository

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

    companion object {
        fun from(context: Context): KiKompetenzApp =
            context.applicationContext as KiKompetenzApp
    }
}
