package ai.ki_kompetenz_training_org.ui.minigames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class GamePhase { PLAYING, RESULT }

data class MiniGameUiState(
    val phase: GamePhase = GamePhase.PLAYING,
    val currentIndex: Int = 0,
    val answers: List<Boolean> = emptyList(),
    val selectedOption: Int? = null,
    val earnedXp: Int = 0,
    val newBadges: List<String> = emptyList(),
)

class MiniGameViewModel(
    val game: MiniGame,
    private val gamification: GamificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MiniGameUiState())
    val state: StateFlow<MiniGameUiState> = _state

    init {
        viewModelScope.launch {
            // count how many of the 3 games were already played (via badges)
            val unlocked = gamification.observeBadgeState()
            // simplest: track via played games badge presence
        }
    }

    fun selectOption(optionIndex: Int) {
        val s = _state.value
        if (s.phase != GamePhase.PLAYING || s.selectedOption != null) return
        val round = game.rounds.getOrNull(s.currentIndex) ?: return
        _state.value = s.copy(
            selectedOption = optionIndex,
            answers = s.answers + (optionIndex == round.correctIndex),
        )
    }

    fun next() {
        val s = _state.value
        if (s.selectedOption == null) return
        if (s.currentIndex < game.rounds.size - 1) {
            _state.value = s.copy(currentIndex = s.currentIndex + 1, selectedOption = null)
        } else {
            finish()
        }
    }

    fun restart() {
        _state.value = MiniGameUiState()
    }

    private fun finish() {
        val s = _state.value
        val correct = s.answers.count { it }
        // Use difficulty-based XP calculation
        val xp = ai.ki_kompetenz_training_org.data.repo.GamificationRules.miniGameXp(
            correctCount = correct,
            totalQuestions = game.rounds.size,
            difficulty = game.difficulty.name
        )
        _state.value = s.copy(phase = GamePhase.RESULT, earnedXp = xp)
        viewModelScope.launch {
            gamification.onMiniGameFinished(correct, game.rounds.size, gameId = game.id)
        }
    }
}