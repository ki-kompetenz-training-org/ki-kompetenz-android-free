package ai.ki_kompetenz_training_org.ui.minigames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyStatement
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

/** Phases of an adaptive retrieval-practice session. */
enum class AdaptivePhase { PLAYING, RESULT }

/** One adaptive round: classify a statement as FAKT or RISIKO. */
data class AdaptiveRound(
    val statement: LiteracyStatement,
    val domain: String,
    val isRisk: Boolean,
)

data class AdaptiveQuizUiState(
    val phase: AdaptivePhase = AdaptivePhase.PLAYING,
    val currentIndex: Int = 0,
    val rounds: List<AdaptiveRound> = emptyList(),
    val selectedAnswer: Boolean? = null,
    val answers: List<Boolean> = emptyList(),
    val earnedXp: Int = 0,
    val dailyChallengeBonusXp: Int = 0,
    val weakDomains: List<String> = emptyList(),
)

/**
 * Adaptive retrieval-practice quiz (wissenschaftlich fundiert):
 * - Retrieval Practice / Testing Effect (Roediger & Karpicke, 2006)
 * - Adaptive Interleaving (Bjork, desirable difficulties)
 * - Sofortiges ausgearbeitetes Feedback (Hattie & Timperley, 2007)
 * - Keine extraneous cognitive load (Sweller)
 */
class AdaptiveQuizViewModel(
    val game: MiniGame,
    private val gamification: GamificationRepository,
    private val mastery: MasteryTracker,
    private val dailyChallengeRepository: DailyChallengeRepository? = null,
    private val rng: Random = Random.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(AdaptiveQuizUiState())
    val state: StateFlow<AdaptiveQuizUiState> = _state

    init {
        startSession()
    }

    private fun startSession() {
        val allowed = game.domainFilter ?: LiteracyBank.DOMAINS
        val rounds = mutableListOf<AdaptiveRound>()
        val usedTexts = mutableSetOf<String>()
        var attempts = 0
        while (rounds.size < SESSION_SIZE && attempts < MAX_ATTEMPTS) {
            attempts++
            val domain = pickDomain(allowed)
            val isRisk = rng.nextBoolean()
            var pool = if (isRisk) LiteracyBank.risks(domain) else LiteracyBank.facts(domain)
            var actualIsRisk = isRisk
            if (pool.isEmpty()) {
                pool = if (isRisk) LiteracyBank.facts(domain) else LiteracyBank.risks(domain)
                actualIsRisk = !isRisk
            }
            if (pool.isEmpty()) continue
            val stmt = pool[rng.nextInt(pool.size)]
            if (!usedTexts.add(stmt.textDe)) continue
            rounds += AdaptiveRound(statement = stmt, domain = domain, isRisk = actualIsRisk)
        }
        _state.value = AdaptiveQuizUiState(rounds = rounds)
    }

    private fun pickDomain(allowed: List<String>): String {
        val weights = allowed.associateWith { d ->
            val m = mastery.getMastery(d)
            when {
                m.total == 0 -> 3.0
                m.total < MIN_ATTEMPTS_FOR_ADAPTATION -> 2.0
                else -> (2.0 - 1.7 * m.correct.toDouble() / m.total.toDouble()).coerceIn(0.3, 2.0)
            }
        }
        var roll = rng.nextDouble() * weights.values.sum()
        for ((domain, weight) in weights) {
            roll -= weight
            if (roll <= 0) return domain
        }
        return allowed.last()
    }

    fun selectAnswer(isRisk: Boolean) {
        val s = _state.value
        if (s.phase != AdaptivePhase.PLAYING || s.selectedAnswer != null) return
        val round = s.rounds.getOrNull(s.currentIndex) ?: return
        _state.value = s.copy(
            selectedAnswer = isRisk,
            answers = s.answers + (isRisk == round.isRisk),
        )
    }

    fun next() {
        val s = _state.value
        if (s.selectedAnswer == null) return
        if (s.currentIndex < s.rounds.size - 1) {
            _state.value = s.copy(currentIndex = s.currentIndex + 1, selectedAnswer = null)
        } else {
            finish()
        }
    }

    fun restart() = startSession()

    private fun finish() {
        val s = _state.value
        val correct = s.answers.count { it }
        val total = s.rounds.size
        if (total == 0) {
            _state.value = s.copy(phase = AdaptivePhase.RESULT, weakDomains = mastery.weakDomains().map { it.domain })
            return
        }
        val logs = s.rounds.mapIndexed { i, round ->
            ClassifyLog(domain = round.domain, correct = s.answers[i], statement = round.statement)
        }
        mastery.recordClassifications(logs)
        val xp = GamificationRules.miniGameXp(
            correctCount = correct,
            totalQuestions = total,
            difficulty = game.difficulty.name,
        )
        _state.value = s.copy(
            phase = AdaptivePhase.RESULT,
            earnedXp = xp,
            weakDomains = mastery.weakDomains().map { it.domain },
        )
        viewModelScope.launch {
            gamification.onMiniGameFinished(correct, total, gameId = game.id)
            val dailyRepo = dailyChallengeRepository ?: return@launch
            val today = LocalDate.now()
            val todaysChallenge = dailyRepo.getTodayChallenge(today, MiniGames.ALL)
            if (todaysChallenge?.id == game.id && !dailyRepo.isCompletedToday(today)) {
                val perfect = correct == total
                val bonusXp = dailyRepo.completeChallenge(today, perfect)
                if (bonusXp > 0) {
                    gamification.addXp(bonusXp)
                    _state.value = _state.value.copy(dailyChallengeBonusXp = bonusXp)
                }
            }
        }
    }

    companion object {
        const val SESSION_SIZE = 10
        private const val MAX_ATTEMPTS = 40
        private const val MIN_ATTEMPTS_FOR_ADAPTATION = 3
    }
}
