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

/** Laedt/speichert den lokalen SM-2-Fortschritt (z. B. SharedPreferences-Adapter). */
interface LocalSrsPersistence {
    fun load(): String?
    fun save(json: String)
    /** Outbox offline gespielter Reviews (Server-Replay, v1.11.0). Defaults: keine. */
    fun loadOutbox(): String? = null
    fun saveOutbox(json: String) {}
}

class SrsRepository(private val api: ApiService, private val persistence: LocalSrsPersistence? = null) {

    private fun localState(): Map<String, LocalSrsCard> =
        persistence?.let { LocalSrsDeck.deserializeState(it.load()) } ?: emptyMap()

    /**
     * Offline-Review OHNE API: SM-2-Update auf dem aktuellen (ggf. schon
     * fortgeschrittenen) Zustand anwenden und persistieren. Null, wenn die
     * Karte lokal unbekannt ist. Deckt auch nicht eingeloggte Nutzer ab
     * (rateLocal im ViewModel).
     */
    fun reviewLocalPersisted(cardId: String, quality: Int): LocalSrsCard? {
        val current = LocalSrsDeck.mergeState(localState()).firstOrNull { it.id == cardId } ?: return null
        val updated = LocalSrsDeck.reviewCard(current, SrsQuality.fromValue(quality), System.currentTimeMillis())
        persistLocalReview(cardId, quality, updated)
        return updated
    }

    /** SM-2-Zustand speichern UND Event für den Server-Replay einreihen. */
    private fun persistLocalReview(cardId: String, quality: Int, updated: LocalSrsCard) {
        persistence?.save(LocalSrsDeck.serializeState(localState() + (updated.id to updated)))
        persistence?.saveOutbox(
            LocalSrsDeck.serializeOutbox(
                LocalSrsDeck.deserializeOutbox(persistence.loadOutbox()) +
                    LocalSrsDeck.LocalSrsEvent(cardId, quality),
            ),
        )
    }

    /**
     * Offline gespielte Reviews an den Server nachziehen. Verbrauchte Events
     * (Server-ok oder Server-Ablehnung) fallen aus der Outbox; bei
     * Netzwerkfehlern bleiben sie für den naechsten Versuch.
     */
    private suspend fun flushOutbox() {
        val events = LocalSrsDeck.deserializeOutbox(persistence?.loadOutbox())
        if (events.isEmpty()) return
        val remaining = mutableListOf<LocalSrsDeck.LocalSrsEvent>()
        for (e in events) {
            // Erfolg -> konsumiert; Netzwerkfehler (Exception) -> behalten;
            // Server-Ablehnung (success=false, z. B. unbekannte Karte) -> konsumiert.
            val networkFailure = runCatching {
                api.postReview(SrsReviewRequestDto(cardId = e.cardId, quality = e.quality)).success
            }.isFailure
            if (networkFailure) remaining += e
        }
        persistence?.saveOutbox(LocalSrsDeck.serializeOutbox(remaining))
    }

    /** Offline-Fallback-Schalter: lokale SM-2-Karten statt API bei Fehlern. */
    var localSrsEnabled: Boolean = true

    suspend fun getDueCards(): Result<List<SrsCardDto>> {
        val remote = runCatching { api.getDueCards().cards }
        if (remote.isSuccess) {
            flushOutbox() // offline gespielte Reviews nachziehen (v1.11.0)
            return remote
        }
        // Fallback: API nicht erreichbar -> lokale SM-2-Karten aus dem Deck.
        return if (localSrsEnabled) {
            val deck = LocalSrsDeck.mergeState(localState())
            Result.success(LocalSrsDeck.getDueCards(System.currentTimeMillis(), deck).map { it.toDto() })
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
            val local = LocalSrsDeck.mergeState(localState()).firstOrNull { it.id == cardId }
            if (local != null) {
                // BUGFIX: das SM-2-Update wurde bisher verworfen — Offline-Reviews
                // aenderten die Faelligkeit nie. Jetzt: Update anwenden UND persistieren.
                val updated = LocalSrsDeck.reviewCard(local, SrsQuality.fromValue(quality), System.currentTimeMillis())
                persistLocalReview(cardId, quality, updated)
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