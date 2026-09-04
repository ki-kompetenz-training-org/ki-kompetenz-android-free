package ai.ki_kompetenz_training_org.ui.minigames

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.CompetencyRepository
import ai.ki_kompetenz_training_org.ui.common.Haptics
import ai.ki_kompetenz_training_org.ui.rewards.RewardDialogHost

/**
 * Adaptive retrieval-practice screen: card-based Fakt/Risiko classification
 * with immediate feedback and explanation. No 3D, no timer, no health.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveQuizScreen(game: MiniGame, onBack: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val prefs = remember {
        app.getSharedPreferences("kikompetenz_gamification", android.content.Context.MODE_PRIVATE)
    }
    val dailyRepo = remember { DailyChallengeRepository(prefs) }
    val vm: AdaptiveQuizViewModel = viewModel(key = game.id) {
        val mastery = MasteryTracker(prefs)
        val competencyRepo = CompetencyRepository(
            snapshotDao = app.db.competencySnapshotDao(),
            tracker = mastery,
            prefs = prefs,
            gamification = app.gamificationRepository,
        )
        AdaptiveQuizViewModel(game, app.gamificationRepository, mastery, dailyRepo, competencyRepository = competencyRepo)
    }
    val state by vm.state.collectAsState()
    val lang = currentLang()
    RewardDialogHost(rewardCenter = app.rewardCenter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${game.emoji} ${game.title(lang)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.games_back))
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            AdaptivePhase.PLAYING -> AdaptivePlayingContent(
                modifier = Modifier.padding(padding),
                state = state,
                lang = lang,
                onSelect = vm::selectAnswer,
                onNext = vm::next,
            )
            AdaptivePhase.RESULT -> AdaptiveResultContent(
                modifier = Modifier.padding(padding),
                game = game,
                state = state,
                lang = lang,
                onRestart = vm::restart,
            )
        }
    }
}

@Composable
private fun AdaptivePlayingContent(
    modifier: Modifier,
    state: AdaptiveQuizUiState,
    lang: String,
    onSelect: (Boolean) -> Unit,
    onNext: () -> Unit,
) {
    val round = state.rounds.getOrNull(state.currentIndex)
    if (round == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.games_adaptive_no_statements))
        }
        return
    }
    val revealed = state.selectedAnswer != null
    val progress = (state.currentIndex + 1).toFloat() / state.rounds.size.coerceAtLeast(1)
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // ── Progress ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.game_round, state.currentIndex + 1, state.rounds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.game_correct_count, state.answers.count { it }, state.rounds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // ── Statement card ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                // Domain chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        round.domain,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    round.statement.text(lang),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                )

                Spacer(Modifier.height(20.dp))

                // ── Fakt / Risiko buttons ──
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ClassifyButton(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.games_adaptive_fact),
                        isCorrectChoice = !round.isRisk,
                        revealed = revealed,
                        selected = revealed && state.selectedAnswer == false,
                        enabled = !revealed,
                        color = Color(0xFF22C55E),
                        onClick = {
                            Haptics.answerTap(view)
                            onSelect(false)
                        },
                    )
                    ClassifyButton(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.games_adaptive_risk),
                        isCorrectChoice = round.isRisk,
                        revealed = revealed,
                        selected = revealed && state.selectedAnswer == true,
                        enabled = !revealed,
                        color = Color(0xFFEF4444),
                        onClick = {
                            Haptics.answerTap(view)
                            onSelect(true)
                        },
                    )
                }

                // ── Explanation after answer ──
                if (revealed) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    val correct = state.selectedAnswer == round.isRisk
                    Text(
                        if (correct) stringResource(R.string.game_correct_feedback) else stringResource(R.string.games_adaptive_wrong),
                        fontWeight = FontWeight.SemiBold,
                        color = if (correct) Color(0xFF16A34A) else Color(0xFFDC2626),
                    )
                    if (round.statement.explanation(lang).isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            round.statement.explanation(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Next button ──
        if (revealed) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    if (state.currentIndex < state.rounds.size - 1)
                        stringResource(R.string.game_next_round)
                    else
                        stringResource(R.string.game_view_result),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ClassifyButton(
    modifier: Modifier,
    label: String,
    isCorrectChoice: Boolean,
    revealed: Boolean,
    selected: Boolean,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            !revealed && pressed -> color.copy(alpha = 0.5f)
            !revealed -> color.copy(alpha = 0.3f)
            isCorrectChoice -> color
            selected -> color
            else -> color.copy(alpha = 0.2f)
        },
        label = "classifyBorder",
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            !revealed && pressed -> color.copy(alpha = 0.08f)
            revealed && isCorrectChoice -> color.copy(alpha = 0.12f)
            revealed && selected -> color.copy(alpha = 0.08f)
            else -> Color.Unspecified
        },
        label = "classifyBg",
    )

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interaction,
                indication = null,
            ),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    revealed && isCorrectChoice -> color
                    revealed && selected -> color
                    !revealed -> color
                    else -> color.copy(alpha = 0.4f)
                },
            )
        }
    }
}

@Composable
private fun AdaptiveResultContent(
    modifier: Modifier,
    game: MiniGame,
    state: AdaptiveQuizUiState,
    lang: String,
    onRestart: () -> Unit,
) {
    val correct = state.answers.count { it }
    val total = state.rounds.size
    val ratio = correct.toFloat() / total.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(game.emoji, style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.game_result),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "$correct/$total",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = stringResource(R.string.game_xp_earned, state.earnedXp),
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFD97706),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "+${state.earnedXp} XP",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                        )
                    }
                }
                if (ratio == 1f) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                    ) {
                        Text(
                            stringResource(R.string.game_perfect),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D),
                        )
                    }
                }
                if (state.kiki > 0) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.kiki_label, state.kiki.toString()),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val prev = state.previousKiki
                        if (prev != null) {
                            val delta = state.kiki - prev
                            if (delta != 0) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (delta > 0) "+$delta" else "$delta",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (delta > 0) Color(0xFF15803D) else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            game.description(lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // ── Weak domains ──
        if (state.weakDomains.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.games_adaptive_weak),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(Modifier.height(8.dp))
                    state.weakDomains.forEach { domain ->
                        Text(
                            "  • $domain",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                stringResource(R.string.game_play_again),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
