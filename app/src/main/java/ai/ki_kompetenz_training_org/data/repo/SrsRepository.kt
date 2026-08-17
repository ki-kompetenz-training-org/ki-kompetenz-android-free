package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.api.SrsReviewRequestDto

/** Quality labels (Anki-style 1-5). */
enum class SrsQuality(val value: Int, val emoji: String, val label: String) {
    AGAIN(1, "🔁", "Wieder vergessen"),
    HARD(2, "😓", "Schwer"),
    GOOD(3, "👍", "Gut"),
    EASY(4, "😊", "Leicht"),
    PERFECT(5, "⭐", "Perfekt"),
    ;

    companion object {
        fun fromValue(value: Int): SrsQuality =
            entries.firstOrNull { it.value == value } ?: GOOD
    }
}

class SrsRepository(private val api: ApiService) {

    suspend fun getDueCards(): Result<List<SrsCardDto>> =
        runCatching { api.getDueCards().cards }

    suspend fun postReview(cardId: String, quality: Int): Result<Unit> =
        runCatching { api.postReview(SrsReviewRequestDto(cardId = cardId, quality = quality)) }
            .map { if (it.success) Unit else throw IllegalStateException("Review failed") }
}

/**
 * Pure session logic — unit-testable.
 */
object SrsSession {
    /** Progress in percent through the session queue. */
    fun progress(reviewed: Int, total: Int): Float =
        if (total <= 0) 0f else (reviewed.toFloat() / total).coerceIn(0f, 1f)

    /** True when the session is finished (all cards reviewed). */
    fun isFinished(reviewed: Int, total: Int): Boolean =
        total > 0 && reviewed >= total
}