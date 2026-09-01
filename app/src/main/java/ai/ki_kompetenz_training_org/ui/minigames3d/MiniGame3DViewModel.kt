/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native MiniGame3DViewModel - will replace MiniGame3DViewModel.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.EndReason
import ai.ki_kompetenz_training_org.data.minigames3d.GameAction
import ai.ki_kompetenz_training_org.data.minigames3d.GameEngine
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.TouchTuning
import ai.ki_kompetenz_training_org.data.minigames3d.GameRules
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog
import ai.ki_kompetenz_training_org.data.minigames3d.Direction
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
import kotlin.math.ceil
import kotlin.math.max

/** HUD snapshot published to the UI at ~10Hz */
data class ArenaHudNew(
    val score: Int = 0,
    val timeLeft: Int = 0,
    val health: Int = 0,
    val maxHealth: Int = 3,
    val target: Int = 0,
    val streak: Int = 0,
)

/** Result data for end-of-game display */
data class ArenaResultNew(
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

/** UI state for the touch-native arena */
data class ArenaUiStateNew(
    val phase: ArenaPhase = ArenaPhase.COUNTDOWN,
    val hud: ArenaHudNew = ArenaHudNew(),
    val result: ArenaResultNew? = null,
    val scannedText: String? = null,
    val scannedIsRisk: Boolean? = null,
    val scannedDomain: String? = null,
    val scannedExplanation: String? = null,
    val decisionTimer: Double? = null,
)

/**
 * Touch-native arena minigame state machine.
 * 
 * Uses GameEngine with touch actions (tap, dash, classify) instead of
 * joystick-based InputState. Entities freeze when pendingDecision != null.
 */
class MiniGame3DViewModel(
    val mode: GameMode,
    private val gamification: GamificationRepository,
    private val mastery: MasteryTracker,
    private val dailyChallengeRepository: DailyChallengeRepository? = null,
    private val onContent: (() -> LiteracyBank)? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaUiStateNew())
    val state: StateFlow<ArenaUiStateNew> = _state

    /** The live touch-native engine state, mutated by step. Read-only from the UI. */
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
        game = GameEngine.createState(mode, content, { Math.random() })
        endedHandled = false
        hudAccum = 0.0
        _state.value = ArenaUiStateNew(phase = ArenaPhase.PLAYING)
    }

    /**
     * Advance the simulation by dt seconds.
     * Called from frame loop. Updates HUD at ~10Hz.
     */
    fun step(dt: Double) {
        val s = game ?: return
        if (s.ended) {
            handleEnd(s)
            return
        }
        GameEngine.stepGame(s, content, TouchTuning.STANDARD, { Math.random() }, dt)

        // HUD throttling
        hudAccum += dt
        if (hudAccum >= 0.1 || s.justScored || s.justHit) {
            hudAccum = 0.0
            _state.value = _state.value.copy(
                hud = ArenaHudNew(
                    score = s.score,
                    timeLeft = max(0, ceil(s.timeLeft).toInt()),
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
            )
        }

        // Publish pending decision text
        if (s.pendingDecision != null) {
            val pd = s.pendingDecision!!
            _state.value = _state.value.copy(
                scannedText = pd.statement.text(lang),
                scannedIsRisk = pd.statement.isRisk,
                scannedDomain = pd.statement.domain,
                scannedExplanation = pd.statement.explanation(lang),
                decisionTimer = pd.timer,
            )
        } else {
            // Clear scanned text when no decision pending (touch-native: no scan during movement)
            if (_state.value.scannedText != null) {
                _state.value = _state.value.copy(
                    scannedText = null,
                    scannedIsRisk = null,
                    scannedDomain = null,
                    scannedExplanation = null,
                    decisionTimer = null,
                )
            }
        }
    }

    /** Handle touch-native actions */
    fun onAction(action: GameAction) {
        val s = game ?: return
        GameEngine.onAction(s, action, content, { Math.random() }, TouchTuning.STANDARD)
    }

    /**
     * Touch-to-classify action for tap on entity.
     * Disk index is determined by hit testing in the UI.
     */
    fun onTapEntity(diskIndex: Int) {
        onAction(GameAction.TapEntity(diskIndex))
    }

    /** Swipe-to-dash action */
    fun onDash(direction: Direction) {
        onAction(GameAction.Dash(direction))
    }

    /** Classify action from UI buttons */
    fun onClassify(action: ClassifyAction) {
        onAction(GameAction.Classify(action))
    }

    /** Runs once when the simulator reports the game ended. */
    private fun handleEnd(s: GameState) {
        if (endedHandled) return
        endedHandled = true

        val logs = s.classifications.map { ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog(it.domain, it.correct, it.statement) }
        mastery.recordClassifications(logs)

        val correct = logs.count { it.correct }
        val total = logs.size
        val xp = computeXp(s, correct, total)

        val weak = mastery.weakDomains().map { it.domain }

        _state.value = _state.value.copy(
            phase = ArenaPhase.RESULT,
            result = ArenaResultNew(
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
            val dailyRepo = dailyChallengeRepository ?: return@launch
            val today = java.time.LocalDate.now()
            val todaysChallenge = dailyRepo.getTodayChallenge(
                today, 
                ai.ki_kompetenz_training_org.data.minigames.MiniGames.ALL
            )
            if (todaysChallenge?.id == mode.name.lowercase() && !dailyRepo.isCompletedToday(today)) {
                val perfect = correct > 0 && correct == total
                val bonusXp = dailyRepo.completeChallenge(today, perfect)
                if (bonusXp > 0) {
                    gamification.addXp(bonusXp)
                }
            }
        }
    }

    /**
     * XP for a touch-native arena session.
     * Base by mastery share + streak reward + win bonus.
     */
    fun computeXp(s: GameState, correct: Int, total: Int): Int {
        val masteryShare = if (total > 0) correct.toDouble() / total.toDouble() else 0.0
        val base = (GamificationRules.xpPerMiniGameWinBeginner * 2 * masteryShare).toInt()
        val streakBonus = minOf(s.classifyStreak * 2, 20)
        val winBonus = if (s.won) 15 else 0
        return (base + streakBonus + winBonus).coerceAtLeast(10)
    }
}
