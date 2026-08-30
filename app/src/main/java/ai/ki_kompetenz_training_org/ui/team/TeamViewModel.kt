package ai.ki_kompetenz_training_org.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.api.LeaderboardEntryDto
import ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository
import ai.ki_kompetenz_training_org.ui.common.UiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeamUiState(
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val team: MyTeamResponseDto? = null,
    val leaderboard: List<LeaderboardEntryDto> = emptyList(),
    val avgScore: Int = 0,
    val members: Int = 0,
    val ownRank: Int? = null,
    val error: UiError? = null,
)

class TeamViewModel(
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val gamificationRepository: ai.ki_kompetenz_training_org.data.repo.GamificationRepository,
) : ViewModel() {

    private var teamJoinBadgeGranted = false

    private val _state = MutableStateFlow(TeamUiState(loggedIn = authRepository.isLoggedIn()))
    val state: StateFlow<TeamUiState> = _state

    init {
        if (_state.value.loggedIn) load()
    }

    fun load() {
        if (!authRepository.isLoggedIn()) {
            _state.value = _state.value.copy(loggedIn = false, error = null)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            teamRepository.getMyTeam().onSuccess { myTeam ->
                val teamId = myTeam.team?.id
                if (teamId != null && !teamJoinBadgeGranted) {
                    teamJoinBadgeGranted = true
                    gamificationRepository.onTeamJoined()
                }
                if (teamId != null) {
                    teamRepository.getTeamStats(teamId).onSuccess { stats ->
                        _state.value = TeamUiState(
                            loggedIn = true,
                            loading = false,
                            team = myTeam,
                            leaderboard = stats.leaderboard,
                            avgScore = stats.avgScore,
                            members = stats.members,
                            ownRank = stats.ownRank,
                        )
                    }.onFailure {
                        _state.value = _state.value.copy(loading = false, error = UiError.TEAM_LOAD)
                    }
                } else {
                    _state.value = TeamUiState(loggedIn = true, loading = false, team = null)
                }
            }.onFailure {
                _state.value = _state.value.copy(loading = false, error = UiError.TEAM_LOAD)
            }
        }
    }
}