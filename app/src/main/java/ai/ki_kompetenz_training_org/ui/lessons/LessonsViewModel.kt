package ai.ki_kompetenz_training_org.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.db.LessonEntity
import ai.ki_kompetenz_training_org.data.lessons.BundledLessons
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

data class LessonsUiState(
    val lessons: List<LessonEntity> = emptyList(),
    val loading: Boolean = true,
    val completedSlugs: Set<String> = emptySet(),
    /** Last opened but not-yet-completed lesson (shown as "in progress"). */
    val lastOpenedSlug: String? = null,
    /** True when the first load failed (offline, no cache). */
    val loadFailed: Boolean = false,
)

class LessonsViewModel(
    private val contentRepository: ContentRepository,
    val premiumRepository: PremiumRepository,
    private val gamificationRepository: GamificationRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LessonsUiState())
    val state: StateFlow<LessonsUiState> = _state

    init {
        load()
    }

    /** Retry after a failed first load. */
    fun retry() {
        _state.value = _state.value.copy(loading = true, loadFailed = false)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // Fetch zuerst abschließen (Suspension), DANN collecten — sonst
            // emittiert der DAO-Flow (leerer Cache) VOR Fetch-Abschluss und der
            // Offline-Fallback greift nie (Race, BUG 2026-09-01).
            val fetchResult = contentRepository.fetchLessons(currentLang())
            contentRepository.observeLessons()
                .flowOn(Dispatchers.IO)  // Move database access to IO thread
                .collectLatest { lessons ->
                    if (lessons.isEmpty() && fetchResult.isFailure) {
                        // FIX (BUG 2026-09-01): Offline-Fallback auf gebündelte
                        // Lektionen statt Fehlermeldung — die Inhalte sind
                        // komplett lokal verfügbar (BundledLessons).
                        _state.value = _state.value.copy(
                            lessons = BundledLessons.asEntities(),
                            loading = false,
                            loadFailed = false,
                        )
                    } else {
                        _state.value = _state.value.copy(
                            lessons = lessons,
                            loading = false,
                            loadFailed = false,
                        )
                    }
                }
        }
        viewModelScope.launch {
            combine(
                gamificationRepository.observeLessonProgress(),
                settingsStore.lastLesson,
            ) { completed, last ->
                val completedSlugs = completed.map { it.slug }.toSet()
                completedSlugs to last
            }.collectLatest { (completedSlugs, last) ->
                _state.value = _state.value.copy(
                    completedSlugs = completedSlugs,
                    lastOpenedSlug = last?.slug?.takeUnless { it in completedSlugs },
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            contentRepository.fetchLessons(currentLang())
        }
    }
}
