package ai.ki_kompetenz_training_org.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.db.GamificationEntity
import ai.ki_kompetenz_training_org.data.repo.Badge
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MissionUi(
    val id: String,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
)

data class GamificationUiState(
    val xp: Int = 0,
    val level: Int = 1,
    val xpIntoLevel: Int = 0,
    val xpNeeded: Int = 100,
    val streak: Int = 0,
    val checkedInToday: Boolean = false,
    val freezes: Int = 0,
    val missions: List<MissionUi> = emptyList(),
    val badges: List<Pair<Badge, Boolean>> = emptyList(),
    val lessonProgress: Int = 0,
    val totalLessons: Int = 12,
)

class GamificationViewModel(
    private val gamification: GamificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GamificationUiState())
    val state: StateFlow<GamificationUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                gamification.observe(),
                gamification.observeBadgeState(),
                gamification.observeLessonProgress(),
            ) { entity, badges, lessons ->
                buildState(entity, badges, lessons.size)
            }.collect { _state.value = it }
        }
    }

    fun dailyCheckIn() {
        viewModelScope.launch {
            gamification.dailyCheckIn()
        }
    }

    fun purchaseFreeze() {
        viewModelScope.launch {
            gamification.purchaseFreeze()
        }
    }

    private fun buildState(
        entity: GamificationEntity?,
        badges: List<Pair<Badge, Boolean>>,
        lessonCount: Int,
    ): GamificationUiState {
        val xp = entity?.xp ?: 0
        val level = GamificationRules.levelForXp(xp)
        val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        return GamificationUiState(
            xp = xp,
            level = level,
            xpIntoLevel = GamificationRules.xpIntoLevel(xp),
            xpNeeded = GamificationRules.xpNeededForNextLevel(xp),
            streak = entity?.streak ?: 0,
            checkedInToday = entity?.lastCheckInDay == today,
            freezes = gamification.freezes(),
            missions = readMissions(),
            badges = badges,
            lessonProgress = lessonCount,
        )
    }

    private fun readMissions(): List<MissionUi> {
        val repo = gamification.missions ?: return emptyList()
        val state = repo.current()
        return repo.selectedFor(state.week).map { t ->
            MissionUi(
                id = t.id,
                target = t.target,
                progress = state.progress[t.id] ?: 0,
                completed = t.id in state.completed,
            )
        }
    }
}