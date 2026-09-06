package ai.ki_kompetenz_training_org.ui.srs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
import ai.ki_kompetenz_training_org.data.repo.SrsSession
import ai.ki_kompetenz_training_org.ui.common.UiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SrsPhase { LOADING, REVIEW, NO_CARDS, FINISHED, ERROR, NOT_LOGGED_IN }

data class SrsUiState(
    val phase: SrsPhase = SrsPhase.LOADING,
    val cards: List<SrsCardDto> = emptyList(),
    val currentIndex: Int = 0,
    val showAnswer: Boolean = false,
    val reviewsDone: Int = 0,
    val earnedXp: Int = 0,
    val error: UiError? = null,
) {
    val currentCard: SrsCardDto? get() = cards.getOrNull(currentIndex)
}

class SrsViewModel(
    private val authRepository: AuthRepository,
    private val srsRepository: SrsRepository,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SrsUiState())
    val state: StateFlow<SrsUiState> = _state

    init {
        load()
    }

    fun load() {
        if (!authRepository.isLoggedIn()) {
            _state.value = SrsUiState(phase = SrsPhase.NOT_LOGGED_IN)
            return
        }
        viewModelScope.launch {
            _state.value = SrsUiState(phase = SrsPhase.LOADING)
            srsRepository.getDueCards().onSuccess { cards ->
                _state.value = if (cards.isEmpty()) {
                    SrsUiState(phase = SrsPhase.NO_CARDS)
                } else {
                    SrsUiState(phase = SrsPhase.REVIEW, cards = cards)
                }
            }.onFailure {
                _state.value = SrsUiState(phase = SrsPhase.ERROR, error = UiError.SRS_LOAD)
            }
        }
    }

    fun revealAnswer() {
        if (_state.value.showAnswer) return
        _state.value = _state.value.copy(showAnswer = true)
    }

    fun rate(quality: Int) {
        val s = _state.value
        // Phase-Guard (BUG-Härtung 2026-09-05): Nach FINISHED darf keine
        // weitere Bewertung XP und Session-Bonus auslösen — im UI wäre der
        // Pfad unerreichbar, aber reveal()+rate() konnte zuvor eine zweite
        // Session simulieren. SrsViewModelDetailTest verriegelt den Guard.
        if (s.phase != SrsPhase.REVIEW) return
        val card = s.currentCard ?: return
        if (!s.showAnswer) return
        viewModelScope.launch {
            srsRepository.postReview(card.id, quality).onSuccess {
                val reviewsDone = s.reviewsDone + 1
                val sessionFinished = SrsSession.isFinished(reviewsDone, s.cards.size)
                gamificationRepository.onSrsReview(sessionFinished, s.cards.size)
                val earnedXp = s.earnedXp +
                    ai.ki_kompetenz_training_org.data.repo.GamificationRules.xpPerSrsReview +
                    (if (sessionFinished && s.cards.size >= 5) ai.ki_kompetenz_training_org.data.repo.GamificationRules.srsSessionBonus else 0)
                _state.value = if (sessionFinished) {
                    SrsUiState(
                        phase = SrsPhase.FINISHED,
                        cards = s.cards,
                        reviewsDone = reviewsDone,
                        earnedXp = earnedXp,
                    )
                } else {
                    s.copy(
                        currentIndex = s.currentIndex + 1,
                        showAnswer = false,
                        reviewsDone = reviewsDone,
                        earnedXp = earnedXp,
                    )
                }
            }.onFailure {
                _state.value = s.copy(error = UiError.SRS_SAVE)
            }
        }
    }
}