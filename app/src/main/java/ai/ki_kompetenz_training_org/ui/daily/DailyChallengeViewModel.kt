package ai.ki_kompetenz_training_org.ui.daily

import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyChallengeUiState(
    val challenge: MiniGame? = null,
    val isCompleted: Boolean = false,
    val streak: Int = 0,
    val xpPreview: Int = 0,
    val lastAwardedXp: Int = 0,
)

class DailyChallengeViewModel(
    private val repository: DailyChallengeRepository,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Main,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val _state = MutableStateFlow(DailyChallengeUiState())
    val state: StateFlow<DailyChallengeUiState> = _state.asStateFlow()

    init {
        loadToday()
    }

    private fun loadToday() {
        val challenge = repository.getTodayChallenge(today, MiniGames.ALL)
        val isCompleted = repository.isCompletedToday(today)
        val streak = repository.getStreak()
        val xpPreview = repository.calculateXpPreview(perfect = false)
        _state.value = DailyChallengeUiState(
            challenge = challenge,
            isCompleted = isCompleted,
            streak = streak,
            xpPreview = xpPreview,
        )
    }

    fun completeChallenge(perfect: Boolean) {
        viewModelScope.launch(dispatcher) {
            val xp = repository.completeChallenge(today, perfect)
            val streak = repository.getStreak()
            _state.value = _state.value.copy(
                isCompleted = true,
                streak = streak,
                lastAwardedXp = xp,
            )
        }
    }

    /** Reload daily challenge state from storage. Call when returning to home screen. */
    fun refresh() {
        loadToday()
    }
}
