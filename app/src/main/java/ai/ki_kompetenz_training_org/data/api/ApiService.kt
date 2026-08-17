package ai.ki_kompetenz_training_org.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * REST client for the ki-kompetenz-training.org Next.js API.
 * Public content endpoints + authenticated endpoints (cookies are attached
 * automatically by the CookieJar, mirroring the web session flow).
 */
interface ApiService {

    // ── Public content API (for the app) ─────────────────────────────────────
    @GET("api/content/lessons")
    suspend fun getLessons(): LessonsResponseDto

    @GET("api/content/lessons/{slug}")
    suspend fun getLesson(@Path("slug") slug: String): LessonDetailDto

    @GET("api/content/ki-score")
    suspend fun getKiScoreData(): KiScoreDataDto

    // ── Auth (session restore) ───────────────────────────────────────────────
    @GET("api/auth/me")
    suspend fun getMe(): MeResponseDto

    // ── Team / ranking (auth) ────────────────────────────────────────────────
    @GET("api/team/me")
    suspend fun getMyTeam(): MyTeamResponseDto

    @GET("api/team/{id}/stats")
    suspend fun getTeamStats(@Path("id") id: String): TeamStatsResponseDto

    // ── Store / premium (auth) ───────────────────────────────────────────────
    @GET("api/store/subscription-status")
    suspend fun getSubscriptionStatus(): SubscriptionStatusDto

    // ── SRS (auth) ────────────────────────────────────────────────────────────
    @GET("api/srs/due")
    suspend fun getDueCards(): SrsDueResponseDto

    @POST("api/srs/review")
    suspend fun postReview(@Body body: SrsReviewRequestDto): SrsReviewResponseDto
}

@kotlinx.serialization.Serializable
data class MeResponseDto(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
)

@kotlinx.serialization.Serializable
data class SubscriptionStatusDto(
    val subscribed: Boolean = false,
    val plan: String? = null,
)

// ── SRS (Spaced Repetition) ─────────────────────────────────────────────────

@kotlinx.serialization.Serializable
data class SrsCardDto(
    val id: String = "",
    val lessonId: String = "",
    val question: String = "",
    val answer: String = "",
    val interval: Int = 0,
    val repetitions: Int = 0,
    val stability: Double = 1.0,
    val difficulty: Double = 5.0,
    val nextReview: String? = null,
    val reviewCount: Int = 0,
)

@kotlinx.serialization.Serializable
data class SrsDueResponseDto(
    val cards: List<SrsCardDto> = emptyList(),
    val count: Int = 0,
)

@kotlinx.serialization.Serializable
data class SrsReviewRequestDto(
    val cardId: String,
    val quality: Int,
)

@kotlinx.serialization.Serializable
data class SrsReviewResponseDto(
    val success: Boolean = false,
    val result: SrsReviewResultDto? = null,
)

@kotlinx.serialization.Serializable
data class SrsReviewResultDto(
    val cardId: String = "",
    val interval: Int = 0,
    val nextReview: String = "",
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val repetitions: Int = 0,
)