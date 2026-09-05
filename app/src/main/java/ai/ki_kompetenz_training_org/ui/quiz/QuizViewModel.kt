package ai.ki_kompetenz_training_org.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.api.KiScoreDataDto
import ai.ki_kompetenz_training_org.data.api.KiScoreQuestionDto
import ai.ki_kompetenz_training_org.data.api.KiScoreTierDto
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.QuizResultEntity
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.QuizScoring
import ai.ki_kompetenz_training_org.ui.common.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class QuizPhase { LOADING, ERROR, INTRO, PLAYING, RESULT }

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.LOADING,
    val questions: List<KiScoreQuestionDto> = emptyList(),
    val tiers: List<KiScoreTierDto> = emptyList(),
    val sharePrefix: String = "",
    val currentIndex: Int = 0,
    val answers: List<Boolean> = emptyList(),
    val selectedOption: Int? = null,
    val error: UiError? = null,
    // Gamification
    val lives: Int = QuizConstants.MAX_LIVES,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val timeLeft: Int = QuizConstants.ROUND_SECONDS,
    val scorePoints: Int = 0,
) {
    val score: Int get() = scorePoints
    val tier: KiScoreTierDto? get() = QuizScoring.tierFor(scorePoints, tiers)
}

class QuizViewModel(
    private val contentRepository: ContentRepository,
    private val db: AppDatabase,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state
    
    // Timer for countdown
    private var timerJob: Job? = null

    init {
        load()
    }

    fun load() {
        // Sofort LOADING zeigen — sonst bleibt waehrend des Retry die alte
        // Fehlermeldung stehen und der Button wirkt tot (BUG 2026-09-05).
        _state.value = _state.value.copy(phase = QuizPhase.LOADING, error = null)
        viewModelScope.launch {
            contentRepository.fetchKiScoreData().onSuccess { data ->
                // Fragepool: 10 zufällige Fragen aus dem Pool (analog zur Website)
                val questions = data.questions.shuffled().take(10)
                _state.value = QuizUiState(phase = QuizPhase.INTRO, questions = questions, tiers = data.tiers, sharePrefix = data.share?.prefix ?: "")
            }.onFailure {
                _state.value = QuizUiState(phase = QuizPhase.ERROR, error = UiError.QUIZ_LOAD)
            }
        }
    }

    fun start() {
        _state.value = _state.value.copy(
            phase = QuizPhase.PLAYING,
            currentIndex = 0,
            answers = emptyList(),
            selectedOption = null,
            lives = QuizConstants.MAX_LIVES,
            combo = 0,
            maxCombo = 0,
            timeLeft = QuizConstants.ROUND_SECONDS,
            scorePoints = 0,
        )
        startTimer()
    }

    fun selectOption(optionIndex: Int) {
        val s = _state.value
        if (s.phase != QuizPhase.PLAYING || s.selectedOption != null) return
        val question = s.questions.getOrNull(s.currentIndex) ?: return
        val isCorrect = optionIndex == question.correct
        
        // Cancel timer and stop countdown
        cancelTimer()
        
        // Calculate points and update gamification state
        val points = if (isCorrect) {
            QuizConstants.pointsForCorrectAnswer(s.timeLeft, s.combo)
        } else {
            0
        }
        
        val newCombo = if (isCorrect) s.combo + 1 else 0
        val newLives = if (!isCorrect) s.lives - 1 else s.lives
        
        _state.value = s.copy(
            selectedOption = optionIndex,
            answers = s.answers + isCorrect,
            combo = newCombo,
            maxCombo = maxOf(s.maxCombo, newCombo),
            lives = newLives.coerceAtLeast(0),  // Ensure lives >= 0
            scorePoints = s.scorePoints + points,
        )
        
        // If no lives left or time ran out, proceed to next (or finish)
        if (s.lives <= 1 && !isCorrect) {
            // Small delay before moving to next question result
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                next()
            }
        }
    }

    fun next() {
        val s = _state.value
        if (s.selectedOption == null) return
        cancelTimer()
        if (s.currentIndex < s.questions.size - 1) {
            _state.value = s.copy(
                currentIndex = s.currentIndex + 1,
                selectedOption = null,
                timeLeft = QuizConstants.ROUND_SECONDS
            )
            startTimer()
        } else {
            finish()
        }
    }

    fun restart() {
        _state.value = _state.value.copy(phase = QuizPhase.INTRO)
    }

    private fun startTimer() {
        cancelTimer()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            var currentTime = QuizConstants.ROUND_SECONDS
            while (currentTime > 0) {
                delay(QuizConstants.TIMER_TICK_MS)
                currentTime--
                _state.value = _state.value.copy(timeLeft = currentTime)
                
                if (currentTime <= 0) {
                    // Time's up! Auto-select (wrong answer) and move to next
                    val s = _state.value
                    if (s.phase == QuizPhase.PLAYING && s.selectedOption == null) {
                        selectOption(-1) // Force wrong answer
                    }
                    break
                }
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun finish() {
        val s = _state.value
        val score = s.score
        val tier = s.tier
        _state.value = s.copy(phase = QuizPhase.RESULT)
        viewModelScope.launch {
            db.quizResultDao().insert(
                QuizResultEntity(
                    score = score,
                    tierTitle = tier?.title ?: "",
                    correctCount = s.answers.count { it },
                    totalQuestions = s.questions.size,
                )
            )
            gamificationRepository.onQuizFinished(
                correctCount = s.answers.count { it },
                totalQuestions = s.questions.size,
                score = score,
            )
        }
    }
}