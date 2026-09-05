package ai.ki_kompetenz_training_org.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.ui.theme.KiTokens
import ai.ki_kompetenz_training_org.data.repo.RewardFormat
import ai.ki_kompetenz_training_org.ui.common.SkeletonCard
import ai.ki_kompetenz_training_org.ui.common.SkeletonVisibility
import ai.ki_kompetenz_training_org.ui.theme.LocalAudienceMode
import ai.ki_kompetenz_training_org.ui.rewards.RewardDialogHost
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.KikiGuidance
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.ui.gamification.parseDomainScores
import ai.ki_kompetenz_training_org.data.repo.CompetencyRepository
import ai.ki_kompetenz_training_org.ui.daily.DailyChallengeCard
import ai.ki_kompetenz_training_org.ui.daily.DailyChallengeViewModel
import ai.ki_kompetenz_training_org.ui.home.KikiSparklineCard
import ai.ki_kompetenz_training_org.ui.kibot.KiBotScene
import ai.ki_kompetenz_training_org.ui.kibot.KiBotState
import ai.ki_kompetenz_training_org.ui.kibot.daysSinceLastCheckIn

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenQuiz: () -> Unit,
    onOpenLessons: () -> Unit,
    onOpenLesson: (String) -> Unit = {},
    onOpenPremium: () -> Unit,
    onOpenTeam: () -> Unit,
    onLogin: () -> Unit,
    onOpenMiniGames: () -> Unit,
    onOpenGamification: () -> Unit,
    onOpenSrs: () -> Unit,
    onOpenForKids: () -> Unit,
    onOpenForSeniors: () -> Unit,
    onOpenAbout: () -> Unit = {},
    onOpenMiniGame: (String) -> Unit = {},
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: HomeViewModel = viewModel {
        HomeViewModel(app.authRepository, app.premiumRepository, app.teamRepository, app.contentRepository, app.gamificationRepository, app.settingsStore)
    }
    val state by vm.state.collectAsState()
    RewardDialogHost(rewardCenter = app.rewardCenter)

    // First-load skeleton (calm placeholders, no shimmer loop)
    if (SkeletonVisibility.shouldShow(loading = state.loading, items = if (state.lessonProgress > 0) 1 else 0)) {
        Column(Modifier.fillMaxSize().padding(top = 48.dp)) {
            SkeletonCard()
            SkeletonCard()
            SkeletonCard()
            SkeletonCard()
        }
        return
    }
    
    // ── Connectivity state ──
    val isOnline by app.connectivityObserver.isOnline.collectAsState(initial = true)

    // KiBot hello dialog (first launch) ──
    val settingsStore = KiKompetenzApp.from(LocalContext.current).settingsStore
    val kibotHelloShown by settingsStore.kibotHelloShown.collectAsState(initial = true)
    var showHelloDialog by remember { mutableStateOf(!kibotHelloShown) }
    val scope = rememberCoroutineScope()
    if (showHelloDialog) {
        AlertDialog(
            onDismissRequest = {
                showHelloDialog = false
                scope.launch { settingsStore.markKibotHelloShown() }
            },
            title = { Text(stringResource(R.string.kibot_hello_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.kibot_hello_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showHelloDialog = false
                    scope.launch { settingsStore.markKibotHelloShown() }
                }) { Text(stringResource(R.string.kibot_hello_cta)) }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Offline banner
        if (!isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(KiTokens.CardRadiusCompact),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(
                    text = stringResource(R.string.error_offline_title),
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Gamification summary bar ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KiTokens.CardRadiusCompact),
            onClick = onOpenGamification,
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.home_level, state.level.toString()), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(stringResource(R.string.home_xp, state.xp.toString()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        if (state.xpNeeded <= 0) 1f
                        else (state.xpIntoLevel.toFloat() / state.xpNeeded).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.home_streak, state.streak.toString()), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    // Brief emphasis pulse right after a successful check-in;
                    // skipped entirely when system animations are turned off.
                    val checkInScale = remember { Animatable(1f) }
                    val prevCheckedIn = remember { mutableStateOf(state.checkedInToday) }
                    val resolver = LocalContext.current.contentResolver
                    LaunchedEffect(state.checkedInToday) {
                        if (state.checkedInToday && !prevCheckedIn.value) {
                            val dur = RewardFormat.checkInAnimationMs(
                                Settings.Global.getFloat(
                                    resolver,
                                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f,
                                )
                            )
                            if (dur > 0) {
                                checkInScale.animateTo(1.06f, tween(dur / 2))
                                checkInScale.animateTo(1f, tween(dur / 2))
                            }
                        }
                        prevCheckedIn.value = state.checkedInToday
                    }
                    Button(
                        onClick = vm::dailyCheckIn,
                        enabled = !state.checkedInToday,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.heightIn(min = LocalAudienceMode.current.minTouchTargetDp.dp).graphicsLayer {
                            scaleX = checkInScale.value
                            scaleY = checkInScale.value
                        },
                    ) {
                        Text(if (state.checkedInToday) stringResource(R.string.home_checked_in) else stringResource(R.string.home_checkin))
                    }
                }
                if (state.missions.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    val done = state.missions.count { it.completed }
                    Text(
                        if (done == state.missions.size)
                            stringResource(R.string.home_missions_chip_done)
                        else
                            stringResource(R.string.home_missions_chip, done, state.missions.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Lektionen: zentrale Lern-Einstieg — immer im Vordergrund ──
        // Resume state: zeigt die zuletzt geöffnete (unvollendete) Lektion.
        val resume = state.lastLesson
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KiTokens.CardRadiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            onClick = { if (resume != null) onOpenLesson(resume.slug) else onOpenLessons() },
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            resume?.title ?: stringResource(R.string.home_lessons_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            if (resume != null) stringResource(R.string.home_lessons_resume_lesson, resume.index, state.totalLessons)
                            else stringResource(R.string.home_lessons_progress, state.lessonProgress, state.totalLessons),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        if (resume != null) stringResource(R.string.home_lessons_cta_resume) else stringResource(R.string.home_lessons_cta),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (state.lessonProgress.toFloat() / state.totalLessons.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Daily Challenge card ──
        val dailyVm: DailyChallengeViewModel = viewModel {
            DailyChallengeViewModel(
                DailyChallengeRepository(
                    app.getSharedPreferences("kikompetenz_gamification", android.content.Context.MODE_PRIVATE)
                ),
                onDailyCompleted = {
                    app.gamificationRepository.missions?.record(
                        ai.ki_kompetenz_training_org.data.missions.MissionMetric.DAILY_COMPLETED
                    )
                },
            )
        }
        val dailyState by dailyVm.state.collectAsState()

        // Refresh daily challenge state when returning to home screen
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    dailyVm.refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        DailyChallengeCard(
            challenge = dailyState.challenge,
            isCompleted = dailyState.isCompleted,
            streak = dailyState.streak,
            xpPreview = dailyState.xpPreview,
            onStart = {
                dailyState.challenge?.let { game ->
                    onOpenMiniGame(game.id)
                }
            },
        )

        Spacer(Modifier.height(16.dp))

        // ── KIKI-Sparkline card (woechentlicher KI-Kompetenz-Index-Trend) ──
        val kikiSnapshots by remember {
            val prefs = app.getSharedPreferences("kikompetenz_gamification", android.content.Context.MODE_PRIVATE)
            CompetencyRepository(
                snapshotDao = app.db.competencySnapshotDao(),
                tracker = MasteryTracker(prefs),
                prefs = prefs,
                gamification = app.gamificationRepository,
            ).observeSnapshots()
        }.collectAsState(initial = emptyList())

        // ── KIKI-Guidance (openspec add-kiki-guidance): schwächste Domäne → Übung ──
        val kikiGuidance = remember(kikiSnapshots) {
            val latest = kikiSnapshots.maxByOrNull { it.weekKey }
            if (latest == null) {
                null
            } else {
                val scoreList = parseDomainScores(latest.perDomainJson, LiteracyBank.DOMAINS.size)
                val scores = LiteracyBank.DOMAINS.indices.associate { i ->
                    LiteracyBank.DOMAINS[i] to (scoreList.getOrNull(i) ?: 0)
                }
                KikiGuidance.guidanceFor(scores, System.currentTimeMillis(), latest.createdAt)
            }
        }
        val guidanceGameId = kikiGuidance?.domain?.let { domain ->
            KikiGuidance.matchingGame(domain, MiniGames.ALL)?.id
        }
        KikiSparklineCard(
            snapshots = kikiSnapshots,
            guidance = kikiGuidance,
            practiceGameId = guidanceGameId,
            onPractice = onOpenMiniGame,
        )

        Spacer(Modifier.height(16.dp))

        // ── Quick-action grid (2×2) ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                QuickActionCard(
                    emoji = "🤖",
                    title = stringResource(R.string.home_quick_quiz),
                    subtitle = stringResource(R.string.home_quick_quiz_desc),
                    onClick = onOpenQuiz,
                )
            }
            item {
                QuickActionCard(
                    emoji = "🎮",
                    title = stringResource(R.string.home_quick_games),
                    subtitle = stringResource(R.string.home_quick_games_desc),
                    onClick = onOpenMiniGames,
                )
            }
            item {
                QuickActionCard(
                    emoji = "📖",
                    title = stringResource(R.string.home_quick_lessons),
                    subtitle = stringResource(R.string.home_quick_lessons_desc),
                    onClick = onOpenLessons,
                )
            }
            item {
                QuickActionCard(
                    emoji = "🔄",
                    title = stringResource(R.string.home_quick_srs),
                    subtitle = stringResource(R.string.home_quick_srs_desc),
                    onClick = onOpenSrs,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── KiBot ──
        val kibotState = KiBotState.from(
            level = state.level,
            xp = state.xp,
            xpIntoLevel = state.xpIntoLevel,
            xpNeeded = state.xpNeeded,
            streak = state.streak,
            daysSinceCheckIn = daysSinceLastCheckIn(state.lastCheckInDay),
            checkedInToday = state.checkedInToday,
        )
        val kibotDescription = stringResource(R.string.kibot_level, state.level)
        var showKibotTips by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { contentDescription = kibotDescription },
            shape = RoundedCornerShape(20.dp),
            onClick = { showKibotTips = true },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                KiBotScene(
                    state = kibotState,
                    modifier = Modifier.size(180.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── KiBot tips bottom sheet ──
        if (showKibotTips) {
            ModalBottomSheet(onDismissRequest = { showKibotTips = false }) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(
                        stringResource(R.string.kibot_tips_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.kibot_hello_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.kibot_sleepy_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── "More" section: Kids + Seniors (mode-aware, card count never grows) ──
        val audienceMode = LocalAudienceMode.current
        val minCardHeight = audienceMode.minTouchTargetDp.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (cardId in audienceMode.homeCardIds()) {
                val (emoji, titleRes, descRes, onClick) = when (cardId) {
                    "forkids" -> Quad("👶", R.string.home_more_kids, R.string.home_more_kids_desc, onOpenForKids)
                    else -> Quad("👴", R.string.home_more_seniors, R.string.home_more_seniors_desc, onOpenForSeniors)
                }
                Card(
                    modifier = Modifier.weight(1f).heightIn(min = minCardHeight),
                    shape = RoundedCornerShape(KiTokens.CardRadiusCompact),
                    onClick = onClick,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(titleRes), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(descRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Premium chip (only if not premium) ──
        if (state.loggedIn && !state.premium) {
            val premiumCta = stringResource(R.string.home_premium_chip)
            AssistChip(
                onClick = onOpenPremium,
                label = { Text(premiumCta) },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = premiumCta, Modifier.size(16.dp))
                },
                modifier = Modifier.semantics { contentDescription = premiumCta },
            )
        } else if (!state.loggedIn) {
            val loginCta = stringResource(R.string.home_login_cta)
            AssistChip(
                onClick = onLogin,
                label = { Text(loginCta) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = loginCta, Modifier.size(16.dp)) },
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── DSGVO footer ──
        Text(
            stringResource(R.string.home_dsgvo_local),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Spacer(Modifier.height(8.dp))

        // ── About / Disclaimer link ──
        val aboutDesc = stringResource(R.string.menu_about)
        TextButton(
            onClick = onOpenAbout,
            modifier = Modifier.semantics { contentDescription = aboutDesc },
        ) {
            Text(
                stringResource(R.string.menu_about),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title. $subtitle"
            },
        shape = RoundedCornerShape(KiTokens.CardRadiusLarge),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


/** Minimal 4-tuple used by the mode-aware home cards. */
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
