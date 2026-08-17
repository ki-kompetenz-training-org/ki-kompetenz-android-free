package ai.ki_kompetenz_training_org.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loggedIn: Boolean = false,
    val premium: Boolean = false,
    val premiumChecked: Boolean = false,
    val myTeam: MyTeamResponseDto? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val xpIntoLevel: Int = 0,
    val xpNeeded: Int = 100,
    val streak: Int = 0,
    val checkedInToday: Boolean = false,
    val lastCheckInDay: String? = null,
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val teamRepository: TeamRepository,
    private val contentRepository: ContentRepository,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(loggedIn = authRepository.isLoggedIn()))
    val state: StateFlow<HomeUiState> = _state

    init {
        refresh()
        viewModelScope.launch {
            gamificationRepository.observe().collect { entity ->
                val xp = entity?.xp ?: 0
                _state.value = _state.value.copy(
                    xp = xp,
                    level = ai.ki_kompetenz_training_org.data.repo.GamificationRules.levelForXp(xp),
                    xpIntoLevel = ai.ki_kompetenz_training_org.data.repo.GamificationRules.xpIntoLevel(xp),
                    xpNeeded = ai.ki_kompetenz_training_org.data.repo.GamificationRules.xpNeededForNextLevel(xp),
                    streak = entity?.streak ?: 0,
                    checkedInToday = entity?.lastCheckInDay == java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                    lastCheckInDay = entity?.lastCheckInDay,
                )
            }
        }
    }

    fun dailyCheckIn() {
        viewModelScope.launch {
            gamificationRepository.dailyCheckIn()
        }
    }

    fun refresh() {
        val loggedIn = authRepository.isLoggedIn()
        _state.value = _state.value.copy(loggedIn = loggedIn)
        viewModelScope.launch {
            // Warm the offline cache in the background
            contentRepository.fetchLessons()
            if (loggedIn) {
                premiumRepository.isPremium().onSuccess { premium ->
                    _state.value = _state.value.copy(premium = premium, premiumChecked = true)
                }.onFailure {
                    _state.value = _state.value.copy(premiumChecked = true)
                }
                teamRepository.getMyTeam().onSuccess { team ->
                    _state.value = _state.value.copy(myTeam = team)
                }
            } else {
                _state.value = _state.value.copy(premiumChecked = true)
            }
        }
    }
}