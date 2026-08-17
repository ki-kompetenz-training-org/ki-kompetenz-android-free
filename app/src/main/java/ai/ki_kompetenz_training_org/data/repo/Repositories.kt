package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto
import ai.ki_kompetenz_training_org.data.api.SubscriptionStatusDto
import ai.ki_kompetenz_training_org.data.api.TeamStatsResponseDto
import ai.ki_kompetenz_training_org.data.prefs.TokenStore

/**
 * Auth + premium + team repositories.
 * - Auth: session exists when the web cookies are present (WebView login).
 * - Premium: server-checked subscription status (mirrors web /api/store/subscription-status).
 * - Team: own team + ranking — DSGVO: API returns names/scores only, never emails.
 */
class AuthRepository(private val tokenStore: TokenStore) {
    fun isLoggedIn(): Boolean = tokenStore.hasSession()
    fun logout() = tokenStore.clearSession()
}

class PremiumRepository(private val api: ApiService) {
    /** Free preview lessons: every odd lesson is free (50% free content). */
    suspend fun isPremium(): Result<Boolean> =
        runCatching { api.getSubscriptionStatus().subscribed }

    fun isPremiumLesson(lessonNumber: Int?): Boolean {
        // Mirrors the web: lessons 1-8 are free, lessons 9-14 are Premium.
        val n = lessonNumber ?: 0
        return n >= 9
    }
}

class TeamRepository(private val api: ApiService) {
    suspend fun getMyTeam(): Result<MyTeamResponseDto> =
        runCatching { api.getMyTeam() }

    suspend fun getTeamStats(teamId: String): Result<TeamStatsResponseDto> =
        runCatching { api.getTeamStats(teamId) }
}