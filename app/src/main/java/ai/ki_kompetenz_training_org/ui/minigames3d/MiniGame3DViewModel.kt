package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog
import ai.ki_kompetenz_training_org.data.minigames3d.EndReason
import ai.ki_kompetenz_training_org.data.minigames3d.GameEngine
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.InputState
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryBankContent
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ArenaPhase { COUNTDOWN, PLAYING, RESULT }

/** HUD snapshot published to the UI at ~10Hz (matches web, avoids per-frame recomposition). */
data class ArenaHud(
    val score: Int = 0,
    val timeLeft: Int = 0,
    val health: Int = 0,
    val maxHealth: Int = 3,
    val target: Int = 0,
    val streak: Int = 0,
)

data class ArenaResult(
    val won: Boolean,
    val score: Int,
    val target: Int,
    val reason: EndReason?,
    val correct: Int,
    val total: Int,
    val earnedXp: Int,
    val weakDomains: List<String>,
    val preferredDomains: List<String>,
)

data class ArenaUiState(
    val phase: ArenaPhase = ArenaPhase.COUNTDOWN,
    val hud: ArenaHud = ArenaHud(),
    val result: ArenaResult? = null,
    /** Statement currently scannable (orbHunt) or pending decision (mazeRun). */
    val scannedText: String? = null,
    val scannedIsRisk: Boolean? = null,
    val scannedDomain: String? = null,
    val scannedExplanation: String? = null,
    /** True while a maze decision timer is running. */
    val decisionTimer: Double? = null,
    /** "PRESS FIRE" hint for truthSnipe after items drift nearby. */
    val fireHint: Boolean = false,
)

/**
 * Real-time arena minigame state machine.
 *
 * The engine [GameState] is mutated each frame (60Hz); the UI reads it directly
 * during draw and receives a throttled [ArenaHud] + statement snapshot here.
 *
 * Individualization: on game end, all classifications are recorded into the
 * [MasteryTracker] (weights future content toward weak domains), and XP is
 * awarded based on score + streak via [GamificationRules].
 */
class MiniGame3DViewModel(
    val mode: GameMode,
    private val gamification: GamificationRepository,
    private val mastery: MasteryTracker,
    private val dailyChallengeRepository: DailyChallengeRepository? = null,
    private val onContent: (() -> LiteracyBank) ? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaUiState())
    val state: StateFlow<ArenaUiState> = _state

    /** The live engine state, mutated by [step]. Read-only from the UI. */
    @Volatile
    var game: GameState? = null
        private set

    private val content = MasteryBankContent(mastery)
    private var hudAccum = 0.0
    private var endedHandled = false
    private var lang: String = "de"

    fun setLang(value: String) {
        lang = if (value == "de") "de" else "en"
    }

    fun start() {
        game = GameEngine.createState(mode, content)
        endedHandled = false
        hudAccum = 0.0
        _state.value = ArenaUiState(phase = ArenaPhase.PLAYING)
    }

    /**
     * Advance the simulation by [dt] seconds with the current [input].
     * Called from a LaunchedEffect frame loop. Updates HUD at ~10Hz and
     * publishes the scanned statement so the UI can show the learning moment.
     */
    fun step(input: InputState, dt: Double) {
        val s = game ?: return
        if (s.ended) {
            handleEnd(s)
            return
        }
        GameEngine.stepGame(s, input, content = content, dt = dt)

        hudAccum += dt
        if (hudAccum >= 0.1 || s.justScored || s.justHit) {
            hudAccum = 0.0
            _state.value = _state.value.copy(
                hud = ArenaHud(
                    score = s.score,
                    timeLeft = kotlin.math.max(0, kotlin.math.ceil(s.timeLeft).toInt()),
                    health = s.health,
                    maxHealth = s.maxHealth,
                    target = s.target,
                    streak = s.classifyStreak,
                ),
                scannedText = null,
                scannedIsRisk = null,
                scannedDomain = null,
                scannedExplanation = null,
                decisionTimer = s.pendingDecision?.timer,
                fireHint = s.mode == GameMode.TRUTH_SNIPE && s.bullets.isNotEmpty(),
            )
        }

        // Publish scannable statement (orbHunt) or pending decision (mazeRun)
        if (s.mode == GameMode.ORB_HUNT && s.scannedIndex >= 0) {
            val item = scannableItem(s) ?: return@step
            val stmt = item.statement ?: return@step
            _state.value = _state.value.copy(
                scannedText = stmt.text(lang),
                scannedIsRisk = stmt.isRisk,
                scannedDomain = stmt.domain,
                scannedExplanation = stmt.explanation(lang),
            )
        } else if (s.pendingDecision != null) {
            val pd = s.pendingDecision!!
            _state.value = _state.value.copy(
                scannedText = pd.statement.text(lang),
                scannedIsRisk = pd.statement.isRisk,
                scannedDomain = pd.statement.domain,
                scannedExplanation = pd.statement.explanation(lang),
                decisionTimer = pd.timer,
            )
        }
    }

    private fun scannableItem(s: GameState) =
        (s.collectibles + s.hazards).getOrNull(s.scannedIndex)

    /** Runs once when the simulator reports the game ended. */
    private fun handleEnd(s: GameState) {
        if (endedHandled) return
        endedHandled = true

        val logs = s.classifications.toList()
        lectureWeak(logs)
        mastery.recordClassifications(logs)

        val correct = logs.count { it.correct }
        val total = logs.size
        val xp = computeXp(s, correct, total)

        val weak = mastery.weakDomains().map { it.domain }

        _state.value = _state.value.copy(
            phase = ArenaPhase.RESULT,
            result = ArenaResult(
                won = s.won,
                score = s.score,
                target = s.target,
                reason = s.endReason,
                correct = correct,
                total = total,
                earnedXp = xp,
                weakDomains = weak,
                preferredDomains = weak,
            ),
        )
        viewModelScope.launch {
            gamification.addXp(xp)
            gamification.onMiniGameFinished(
                correctCount = correct,
                totalQuestions = total.coerceAtLeast(1),
                gameId = mode.name.lowercase(),
            )
            // Daily challenge participation (arena games are part of the 11-game rotation)
            val dailyRepo = dailyChallengeRepository ?: return@launch
            val today = java.time.LocalDate.now()
            val todaysChallenge = dailyRepo.getTodayChallenge(today, ai.ki_kompetenz_training_org.data.minigames.MiniGames.ALL)
            if (todaysChallenge?.id == mode.name.lowercase() && !dailyRepo.isCompletedToday(today)) {
                val perfect = correct > 0 && correct == total
                val bonusXp = dailyRepo.completeChallenge(today, perfect)
                if (bonusXp > 0) {
                    gamification.addXp(bonusXp)
                }
            }
        }
    }

    /** Record a weak-domain success/failure into the classifier log (feed-forward to content). */
    private fun lectureWeak(logs: List<ClassifyLog>) {
        // pass-through hook: future SRS integration can enqueue weak-domain statements here
    }

    /**
     * XP for a real-time arena session.
     * Base by mastery share + streak reward + win bonus. Kept conservative
     * (stoic): no inflation, only solid learning payoff.
     */
    fun computeXp(s: GameState, correct: Int, total: Int): Int {
        val masteryShare = if (total > 0) correct.toDouble() / total.toDouble() else 0.0
        val base = (GamificationRules.xpPerMiniGameWinBeginner * 2 * masteryShare).toInt()
        val streakBonus = minOf(s.classifyStreak * 2, 20)
        val winBonus = if (s.won) 15 else 0
        return (base + streakBonus + winBonus).coerceAtLeast(10)
    }
}
