package ai.ki_kompetenz_training_org.ui.minigames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeSelector
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGameRound
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

enum class GamePhase { PLAYING, RESULT }

data class MiniGameUiState(
    val phase: GamePhase = GamePhase.PLAYING,
    val currentIndex: Int = 0,
    val answers: List<Boolean> = emptyList(),
    val selectedOption: Int? = null,
    val earnedXp: Int = 0,
    val newBadges: List<String> = emptyList(),
    val dailyChallengeBonusXp: Int = 0,
)

class MiniGameViewModel(
    val game: MiniGame,
    private val gamification: GamificationRepository,
    private val dailyChallengeRepository: DailyChallengeRepository? = null,
    private val rng: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(MiniGameUiState())
    val state: StateFlow<MiniGameUiState> = _state

    /** Rounds for this session: Fake-or-Echt draws a random 10 from the bank, others use the full list. */
    val sessionRounds: List<MiniGameRound> =
        if (game.isFakeOrReal) {
            game.rounds.shuffled(rng).take(ai.ki_kompetenz_training_org.data.minigames.TextGameBank.SESSION_SIZE)
        } else {
            game.rounds
        }

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
        val round = sessionRounds.getOrNull(s.currentIndex) ?: return
        _state.value = s.copy(
            selectedOption = optionIndex,
            answers = s.answers + (optionIndex == round.correctIndex),
        )
    }

    fun next() {
        val s = _state.value
        if (s.selectedOption == null) return
        if (s.currentIndex < sessionRounds.size - 1) {
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
            totalQuestions = sessionRounds.size,
            difficulty = game.difficulty.name
        )
        _state.value = s.copy(phase = GamePhase.RESULT, earnedXp = xp)
        viewModelScope.launch {
            gamification.onMiniGameFinished(correct, sessionRounds.size, gameId = game.id)
            // Daily challenge bonus XP
            val dailyRepo = dailyChallengeRepository ?: return@launch
            val today = LocalDate.now()
            val todaysChallenge = dailyRepo.getTodayChallenge(today, MiniGames.ALL)
            if (todaysChallenge?.id == game.id && !dailyRepo.isCompletedToday(today)) {
                val perfect = correct == sessionRounds.size
                val bonusXp = dailyRepo.completeChallenge(today, perfect)
                if (bonusXp > 0) {
                    gamification.addXp(bonusXp)
                    _state.value = _state.value.copy(dailyChallengeBonusXp = bonusXp)
                }
            }
        }
    }
}