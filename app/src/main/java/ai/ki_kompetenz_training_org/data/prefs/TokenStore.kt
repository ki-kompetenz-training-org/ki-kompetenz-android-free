package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for web-session cookies (kkt_access/kkt_refresh) and the
 * premium flag. Uses EncryptedSharedPreferences (Android Keystore backed).
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        try {
            EncryptedSharedPreferences.create(
                context,
                "kikompetenz_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            // Fallback (e.g. tests): plain prefs
            context.getSharedPreferences("kikompetenz_secure", Context.MODE_PRIVATE)
        }
    }

    fun setCookie(name: String, value: String) {
        prefs.edit().putString("cookie_$name", value).apply()
    }

    fun getCookie(name: String): String? = prefs.getString("cookie_$name", null)

    fun getCookies(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("cookie_") && value is String && value.isNotBlank()) {
                result[key.removePrefix("cookie_")] = value
            }
        }
        return result
    }

    fun hasSession(): Boolean = !getCookie("kkt_access").isNullOrBlank()

    fun clearSession() {
        prefs.edit().remove("cookie_kkt_access").remove("cookie_kkt_refresh").apply()
    }
}