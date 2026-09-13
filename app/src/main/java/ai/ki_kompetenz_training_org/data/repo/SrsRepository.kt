package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.api.SrsReviewRequestDto
import ai.ki_kompetenz_training_org.data.srs.LocalSrsCard
import ai.ki_kompetenz_training_org.data.srs.LocalSrsDeck

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

    /** Offline-Fallback-Schalter: lokale SM-2-Karten statt API bei Fehlern. */
    var localSrsEnabled: Boolean = true

    suspend fun getDueCards(): Result<List<SrsCardDto>> {
        val remote = runCatching { api.getDueCards().cards }
        if (remote.isSuccess) return remote
        // Fallback: API nicht erreichbar -> lokale SM-2-Karten aus dem Deck.
        return if (localSrsEnabled) {
            Result.success(LocalSrsDeck.getDueCards(System.currentTimeMillis()).map { it.toDto() })
        } else {
            remote
        }
    }

    suspend fun postReview(cardId: String, quality: Int): Result<Unit> {
        val remote = runCatching { api.postReview(SrsReviewRequestDto(cardId = cardId, quality = quality)) }
            .mapCatching { if (it.success) Unit else throw IllegalStateException("Review failed") }
        if (remote.isSuccess) return remote
        // Fallback: nur für lokal bekannte Karten sinnvoll (lokales SM-2-Update).
        return if (localSrsEnabled) {
            val local = LocalSrsDeck.BUNDLED_CARDS.firstOrNull { it.id == cardId }
            if (local != null) {
                LocalSrsDeck.reviewCard(local, SrsQuality.fromValue(quality), System.currentTimeMillis())
                Result.success(Unit)
            } else {
                remote
            }
        } else {
            remote
        }
    }
}

/** Lokale Karte als DTO für das UI (Screen erwartet [SrsCardDto]). */
fun LocalSrsCard.toDto(): SrsCardDto =
    SrsCardDto(id = id, lessonId = lessonId, question = question, answer = answer)

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