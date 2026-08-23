package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.prefs.TokenStore

/**
 * Auth repository: manages the web-session auth state.
 * - Session exists when the web cookies are present (WebView login).
 * - On 401 Unauthorized from the API, the interceptor calls clearToken()
 *   and signals re-auth so the UI can prompt the user to re-login.
 * - DSGVO: no external reporting, purely local state management.
 */
class AuthRepository(private val tokenStore: TokenStore) {
    fun isLoggedIn(): Boolean = tokenStore.hasSession()
    fun logout() = tokenStore.clearSession()

    /**
     * Called by the 401 interceptor when the server returns 401 Unauthorized.
     * Clears the session so the user is prompted to re-login.
     */
    fun clearToken() {
        tokenStore.clearSession()
    }

    /**
     * Whether a re-authentication is needed after a 401.
     * Checked by the UI layer to show a login prompt.
     */
    var reauthRequired: Boolean = false
        private set

    /** Called by the 401 interceptor to signal that re-login is required. */
    fun signalReAuth() {
        reauthRequired = true
    }

    /** Reset the re-auth flag after the user has re-logged in. */
    fun resetReAuth() {
        reauthRequired = false
    }
}
