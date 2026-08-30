package ai.ki_kompetenz_training_org.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.db.LessonEntity
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import kotlinx.coroutines.launch

data class LessonsUiState(
    val lessons: List<LessonEntity> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

class LessonsViewModel(
    private val contentRepository: ContentRepository,
    val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LessonsUiState())
    val state: StateFlow<LessonsUiState> = _state

    init {
        viewModelScope.launch {
            contentRepository.fetchLessons(currentLang())
            contentRepository.observeLessons()
                .flowOn(Dispatchers.IO)  // Move database access to IO thread
                .collectLatest { lessons ->
                    _state.value = LessonsUiState(lessons = lessons, loading = false)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            contentRepository.fetchLessons(currentLang())
        }
    }
}