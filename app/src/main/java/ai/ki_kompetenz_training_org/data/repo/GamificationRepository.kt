package ai.ki_kompetenz_training_org.data.repo

import android.content.Context
import android.content.SharedPreferences
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.GamificationEntity
import ai.ki_kompetenz_training_org.data.db.LessonProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Pure, unit-testable logic ───────────────────────────────────────────────

/** Pure, unit-testable gamification logic separated from Android dependencies.
 *
 * Contains XP calculations, level progression, streak tracking, and badge definitions.
 * All functions are deterministic and side-effect free.
 */
object GamificationRules {
    /** XP needed to REACH level n: 50 * n * (n-1) → L2=100, L3=300, L4=600, L5=1000 … */
    fun xpForLevel(level: Int): Int = 50 * level * (level - 1)

    fun levelForXp(xp: Int): Int {
        var level = 1
        while (xpForLevel(level + 1) <= xp) level++
        return level
    }

    fun xpIntoLevel(xp: Int): Int {
        val level = levelForXp(xp)
        return xp - xpForLevel(level)
    }

    fun xpNeededForNextLevel(xp: Int): Int {
        val level = levelForXp(xp)
        return xpForLevel(level + 1) - xpForLevel(level)
    }

    /**
     * Streak update for a daily check-in.
     * - lastDay == yesterday → streak + 1
     * - lastDay == today → no change (already checked in)
     * - otherwise → streak resets to 1
     */
    fun nextStreak(lastDay: String?, today: LocalDate = LocalDate.now()): Int {
        if (lastDay == null) return 1
        val yesterday = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return when (lastDay) {
            yesterday -> 0 // incremented by caller (checked in today)
            todayStr -> -1 // already checked in today
            else -> 1
        }
    }

    /** Check-in XP: 5 × current streak day, capped at 30. */
    fun checkInXp(streakDay: Int): Int = minOf(5 * streakDay, 30)

    // ── Streak freezes ──────────────────────────────────────────────────────
    const val freezePriceXp = 100
    /** Hard cap on the total freeze balance (weekly grants and purchases combined). */
    const val maxFreezes = 2

    enum class StreakOutcome { CONTINUE, CONSUME_FREEZE, RESET }

    /** Stable ISO week key ("2026-W35"), independent of device locale/timezone. */
    fun isoWeekKey(date: LocalDate): String {
        val year = date.get(java.time.temporal.WeekFields.ISO.weekBasedYear())
        val week = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        return "%04d-W%02d".format(year, week)
    }

    /** A new free freeze SHALL be granted when the last grant was a different ISO week. */
    fun shouldGrantWeeklyFreeze(lastGrantWeek: String?, today: LocalDate): Boolean =
        lastGrantWeek == null || lastGrantWeek != isoWeekKey(today)

    /**
     * Streak outcome for a check-in based on the gap (days) since the last activity.
     * - gap == 1 (yesterday): continue without touching the freeze balance
     * - gap == 2 (exactly one missed day) + freeze available: consume freeze, continue
     * - otherwise: reset to 1 (freeze bridges exactly one day)
     */
    fun streakOutcome(gapDays: Int, freezes: Int): StreakOutcome = when {
        gapDays <= 1 -> StreakOutcome.CONTINUE
        gapDays == 2 && freezes > 0 -> StreakOutcome.CONSUME_FREEZE
        else -> StreakOutcome.RESET
    }

    /** A freeze purchase is allowed below the cap and with enough XP. */
    fun canPurchaseFreeze(freezes: Int, xp: Int): Boolean =
        freezes < maxFreezes && xp >= freezePriceXp


    val xpPerCorrectQuizAnswer = 10
    val perfectQuizBonus = 50
    val xpPerPremiumCorrectAnswer = 15
    val premiumPerfectBonus = 75
    val xpPerCompletedLesson = 25
    val xpPerTeamJoin = 20
    val xpPerMiniGameWin = 10
    val xpPerMiniGameWinBeginner = 15
    val xpPerMiniGameWinIntermediate = 20
    val xpPerMiniGameWinExpert = 25
    val xpPerSrsReview = 5
    val srsSessionBonus = 20

    fun quizXp(correctCount: Int, totalQuestions: Int, premium: Boolean = false): Int =
        correctCount * (if (premium) xpPerPremiumCorrectAnswer else xpPerCorrectQuizAnswer) +
            (if (correctCount == totalQuestions) (if (premium) premiumPerfectBonus else perfectQuizBonus) else 0)

    /** Calculate XP for mini-game completion based on difficulty */
    fun miniGameXp(correctCount: Int, totalQuestions: Int, difficulty: String): Int {
        val baseXp = when (difficulty) {
            "BEGINNER" -> xpPerMiniGameWinBeginner
            "INTERMEDIATE" -> xpPerMiniGameWinIntermediate
            "EXPERT" -> xpPerMiniGameWinExpert
            else -> xpPerMiniGameWin
        }
        val correctRatio = correctCount.toFloat() / totalQuestions.coerceAtLeast(1)
        val base = (baseXp * correctRatio).toInt()
        val bonus = if (correctCount == totalQuestions) 25 else 0
        return base + bonus
    }
}

// ── Badge catalog ───────────────────────────────────────────────────────────

data class Badge(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
)

object Badges {
    private data class BadgeText(val de: String, val en: String, val fr: String, val zh: String)

    private val TEXTS: Map<String, BadgeText> = mapOf(
        "first_score" to BadgeText("Erster Schritt", "First Steps", "Premier pas", "第一步"),
        "perfect_score" to BadgeText("Perfektion", "Perfection", "Perfection", "完美"),
        "streak_3" to BadgeText("Serien-Tier", "Streak Beast", "Bête de série", "连击高手"),
        "streak_7" to BadgeText("Wochen-Serie", "Week Streak", "Série hebdo", "一周连击"),
        "lesson_first" to BadgeText("Erste Lektion", "First Lesson", "Première leçon", "第一课"),
        "lesson_all" to BadgeText("KI-Profi", "AI Professional", "Pro de l'IA", "AI 专家"),
        "team_player" to BadgeText("Team-Player", "Team Player", "Esprit d'équipe", "团队之星"),
        "mini_game" to BadgeText("Spieler", "Player", "Joueur", "玩家"),
        "mini_game_all" to BadgeText("Meister aller Spiele", "Master of All Games", "Maître des jeux", "全能游戏大师"),
        "fake_or_real" to BadgeText("Detektiv", "Detective", "Détective", "侦探"),
        "visionary" to BadgeText("KI-Visionär", "AI Visionary", "Visionnaire IA", "AI 远见者"),
    )

    private val DESCRIPTIONS: Map<String, BadgeText> = mapOf(
        "first_score" to BadgeText("Erziele deinen ersten KI-Score", "Achieve your first AI score", "Obtenez votre premier score IA", "获得你的第一个 AI 评分"),
        "perfect_score" to BadgeText("Erziele 10/10 im KI-Score", "Score 10/10 in the AI test", "Obtenez 10/10 au test IA", "在 AI 测试中获得 10/10"),
        "streak_3" to BadgeText("3 Tage in Folge aktiv", "Active 3 days in a row", "Actif 3 jours de suite", "连续活跃 3 天"),
        "streak_7" to BadgeText("7 Tage in Folge aktiv", "Active 7 days in a row", "Actif 7 jours de suite", "连续活跃 7 天"),
        "lesson_first" to BadgeText("Schließe deine erste Lektion ab", "Complete your first lesson", "Terminez votre première leçon", "完成你的第一课"),
        "lesson_all" to BadgeText("Schließe alle 12 Lektionen ab", "Complete all 12 lessons", "Terminez les 12 leçons", "完成全部 12 课"),
        "team_player" to BadgeText("Tritt einem Team bei", "Join a team", "Rejoignez une équipe", "加入一个团队"),
        "mini_game" to BadgeText("Spiele dein erstes KI-Mini-Spiel", "Play your first AI mini-game", "Jouez à votre premier mini-jeu IA", "玩第一个 AI 小游戏"),
        "mini_game_all" to BadgeText("Spiele alle KI-Mini-Spiele", "Play all AI mini-games", "Jouez à tous les mini-jeux IA", "玩遍所有 AI 小游戏"),
        "fake_or_real" to BadgeText("Erkenne 10/10 Texte richtig", "Identify 10/10 texts correctly", "Identifiez 10/10 textes", "正确识别 10/10 文本"),
        "visionary" to BadgeText("Erziele 81+ Punkte im KI-Score", "Score 81+ in the AI test", "Obtenez 81+ au test IA", "在 AI 测试中获得 81 分以上"),
    )

    private val ALL_BADGES: List<Pair<String, String>> = listOf(
        "first_score" to "🎯",
        "perfect_score" to "💯",
        "streak_3" to "🔥",
        "streak_7" to "⚡",
        "lesson_first" to "📖",
        "lesson_all" to "🎓",
        "team_player" to "👥",
        "mini_game" to "🎮",
        "mini_game_all" to "🏆",
        "fake_or_real" to "🕵️",
        "visionary" to "🚀",
    )

    /** Badges in der Sprache des Geräts (de/en/fr/zh, Fallback en). */
    fun all(locale: String = java.util.Locale.getDefault().language): List<Badge> {
        val lang = if (locale in setOf("de", "en", "fr", "zh")) locale else "en"
        fun pick(t: BadgeText): String = when (lang) {
            "de" -> t.de
            "fr" -> t.fr
            "zh" -> t.zh
            else -> t.en
        }
        return ALL_BADGES.map { (id, emoji) ->
            Badge(
                id = id,
                emoji = emoji,
                title = pick(TEXTS[id] ?: BadgeText(id, id, id, id)),
                description = pick(DESCRIPTIONS[id] ?: BadgeText("", "", "", "")),
            )
        }
    }
}

// ── Repository (persists locally, DSGVO: no server) ─────────────────────────

/** Persistent gamification state backed by SharedPreferences + Room database.
 *
 * Stores XP, level, streak, badges, and lesson progress locally on device.
 * DSGVO-compliant: no server transmission of gamification data.
 */
class GamificationRepository(
    private val db: AppDatabase,
    context: Context? = null,
) {
    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("kikompetenz_gamification", Context.MODE_PRIVATE)

    private companion object PrefKeys {
        const val KEY_FREEZES = "freezes"
        const val KEY_LAST_FREEZE_WEEK = "last_freeze_week"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val badgesSerializer = ListSerializer(String.serializer())

    /** Weekly-mission hook; null in tests/standalone use. Set once at app start. */
    var missions: ai.ki_kompetenz_training_org.data.missions.WeeklyMissionsRepository? = null

    fun observe(): Flow<GamificationEntity?> =
        db.gamificationDao().observe().flowOn(Dispatchers.IO)

    fun observeBadges(): Flow<List<Badge>> =
        db.gamificationDao().observe().map { entity ->
            val unlocked = parseBadges(entity?.badgesJson)
            Badges.all().map { it to (it.id in unlocked) }
        }.map { pairs -> pairs.map { it.first } }
            .flowOn(Dispatchers.IO) // placeholder — real UI uses observeBadgeState

    fun observeBadgeState(): Flow<List<Pair<Badge, Boolean>>> =
        db.gamificationDao().observe().map { entity ->
            val unlocked = parseBadges(entity?.badgesJson)
            Badges.all().map { it to (it.id in unlocked) }
        }.flowOn(Dispatchers.IO)

    fun observeLessonProgress(): Flow<List<LessonProgressEntity>> =
        db.gamificationDao().observeLessonProgress().flowOn(Dispatchers.IO)

    suspend fun addXp(points: Int) {
        if (points <= 0) return
        val current = db.gamificationDao().get() ?: GamificationEntity()
        db.gamificationDao().upsert(
            current.copy(
                xp = current.xp + points,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    /** Current freeze balance (default 0). */
    fun freezes(): Int = prefs?.getInt(KEY_FREEZES, 0) ?: 0

    private fun grantFreeze(today: LocalDate) {
        prefs?.edit()
            ?.putInt(KEY_FREEZES, minOf(GamificationRules.maxFreezes, freezes() + 1))
            ?.putString(KEY_LAST_FREEZE_WEEK, GamificationRules.isoWeekKey(today))
            ?.apply()
    }

    private fun consumeFreeze() {
        prefs?.edit()?.putInt(KEY_FREEZES, (freezes() - 1).coerceAtLeast(0))?.apply()
    }

    /** Purchase a freeze for [GamificationRules.freezePriceXp] XP; returns success. */
    suspend fun purchaseFreeze(): Boolean {
        val current = db.gamificationDao().get() ?: GamificationEntity()
        if (!GamificationRules.canPurchaseFreeze(freezes(), current.xp)) return false
        prefs?.edit()?.putInt(KEY_FREEZES, freezes() + 1)?.apply()
        db.gamificationDao().upsert(
            current.copy(
                xp = current.xp - GamificationRules.freezePriceXp,
                updatedAt = System.currentTimeMillis(),
            )
        )
        return true
    }

    /** Daily check-in; returns streak day after update (0 = already checked in). */
    suspend fun dailyCheckIn(): Int {
        val now = LocalDate.now()
        val current = db.gamificationDao().get() ?: GamificationEntity()
        val todayStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (current.lastCheckInDay == todayStr) return 0

        // Weekly free freeze grant (clamped at the cap; the week is marked granted either way).
        val lastWeek = prefs?.getString(KEY_LAST_FREEZE_WEEK, null)
        if (GamificationRules.shouldGrantWeeklyFreeze(lastWeek, now)) grantFreeze(now)

        // Gap in days since the last activity; null on first-ever check-in → streak 1.
        val gapDays = current.lastCheckInDay?.let {
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(it), now).toInt()
        } ?: 1
        val freezes = freezes()
        val outcome = GamificationRules.streakOutcome(gapDays, freezes)
        if (outcome == GamificationRules.StreakOutcome.CONSUME_FREEZE) consumeFreeze()
        val streak = when (outcome) {
            GamificationRules.StreakOutcome.CONTINUE -> current.streak + 1
            GamificationRules.StreakOutcome.CONSUME_FREEZE -> current.streak + 1
            GamificationRules.StreakOutcome.RESET -> 1
        }
        val xp = GamificationRules.checkInXp(streak)
        db.gamificationDao().upsert(
            current.copy(
                streak = streak,
                lastCheckInDay = todayStr,
                lastCheckInAt = System.currentTimeMillis(),
                xp = current.xp + xp,
                updatedAt = System.currentTimeMillis(),
            )
        )
        unlockBadgeIfNeeded("streak_3", streak >= 3)
        unlockBadgeIfNeeded("streak_7", streak >= 7)
        return streak
    }

    suspend fun unlockBadge(id: String) {
        val current = db.gamificationDao().get() ?: GamificationEntity()
        val unlocked = parseBadges(current.badgesJson)
        if (id in unlocked) return
        db.gamificationDao().upsert(
            current.copy(
                badgesJson = json.encodeToString(badgesSerializer, unlocked + id),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun unlockBadgeIfNeeded(id: String, condition: Boolean) {
        if (condition) unlockBadge(id)
    }

    suspend fun markLessonCompleted(slug: String) {
        val completed = db.gamificationDao().completedLessonCount()
        db.gamificationDao().markLessonCompleted(slug)
        addXp(GamificationRules.xpPerCompletedLesson)
        unlockBadgeIfNeeded("lesson_first", completed == 0)
        unlockBadgeIfNeeded("lesson_all", completed + 1 >= 12)
        missions?.record(ai.ki_kompetenz_training_org.data.missions.MissionMetric.LESSON_COMPLETED)
    }

    suspend fun onQuizFinished(correctCount: Int, totalQuestions: Int, score: Int) {
        addXp(GamificationRules.quizXp(correctCount, totalQuestions))
        unlockBadge("first_score")
        unlockBadgeIfNeeded("perfect_score", correctCount == totalQuestions)
        unlockBadgeIfNeeded("visionary", score >= 81)
        missions?.record(ai.ki_kompetenz_training_org.data.missions.MissionMetric.QUIZ_PLAYED)
        if (totalQuestions > 0 && correctCount.toFloat() / totalQuestions >= 0.8f) {
            missions?.record(ai.ki_kompetenz_training_org.data.missions.MissionMetric.QUIZ_GOOD)
        }
    }

    suspend fun onMiniGameFinished(correctCount: Int, totalQuestions: Int, gameId: String) {
        addXp(GamificationRules.quizXp(correctCount, totalQuestions))
        unlockBadge("mini_game")
        val played = playedGames()
        if (gameId !in played) {
            played.add(gameId)
            prefs?.edit()?.putStringSet("played_games", played)?.apply()
        }
        unlockBadgeIfNeeded("mini_game_all", played.size >= ai.ki_kompetenz_training_org.data.minigames.MiniGames.ALL.size)
        unlockBadgeIfNeeded("perfect_score", correctCount == totalQuestions)
        if (gameId == "fake_or_real") {
            unlockBadgeIfNeeded("fake_or_real", correctCount == totalQuestions && totalQuestions == 10)
        }
        missions?.record(ai.ki_kompetenz_training_org.data.missions.MissionMetric.MINIGAME_PLAYED)
    }

    /** IDs der bereits gespielten Mini-Games (SharedPreferences, lokal). */
    private fun playedGames(): MutableSet<String> =
        (prefs?.getStringSet("played_games", emptySet()) ?: emptySet()).toMutableSet()

    suspend fun onTeamJoined() {
        addXp(GamificationRules.xpPerTeamJoin)
        unlockBadge("team_player")
    }

    /** XP per SRS card review; bonus when a session of >= 5 cards is finished. */
    suspend fun onSrsReview(sessionFinished: Boolean, sessionSize: Int) {
        addXp(GamificationRules.xpPerSrsReview)
        missions?.record(ai.ki_kompetenz_training_org.data.missions.MissionMetric.SRS_CARDS)
        if (sessionFinished && sessionSize >= 5) {
            addXp(GamificationRules.srsSessionBonus)
        }
    }

    /** Public read-only access to the unlocked badge ids (for reward flow). */
    fun badgeIds(badgesJson: String?): List<String> = parseBadges(badgesJson)

    private fun parseBadges(badgesJson: String?): List<String> {
        if (badgesJson.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(badgesSerializer, badgesJson)
        } catch (_: Exception) {
            emptyList()
        }
    }
}