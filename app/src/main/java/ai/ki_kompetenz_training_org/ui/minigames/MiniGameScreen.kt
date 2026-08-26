package ai.ki_kompetenz_training_org.ui.minigames

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.currentLang

/**
 * MiniGame play and result screens.
 * Zen design: calm colors, generous whitespace, tactile option cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGameScreen(game: MiniGame, onBack: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val dailyRepo = remember {
        DailyChallengeRepository(
            app.getSharedPreferences("kikompetenz_gamification", android.content.Context.MODE_PRIVATE)
        )
    }
    val vm: MiniGameViewModel = viewModel(key = game.id) {
        MiniGameViewModel(game, app.gamificationRepository, dailyRepo)
    }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${game.emoji} ${game.title(currentLang())}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.games_back))
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            GamePhase.PLAYING -> PlayingContent(
                modifier = Modifier.padding(padding),
                game = game,
                state = state,
                onSelect = vm::selectOption,
                onNext = vm::next,
            )
            GamePhase.RESULT -> ResultContent(
                modifier = Modifier.padding(padding),
                game = game,
                state = state,
                onRestart = vm::restart,
            )
        }
    }
}

@Composable
private fun PlayingContent(
    modifier: Modifier,
    game: MiniGame,
    state: MiniGameUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val round = game.rounds.getOrNull(state.currentIndex)
    if (round == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.game_no_rounds))
        }
        return
    }
    val revealed = state.selectedOption != null
    val progress = (state.currentIndex + 1).toFloat() / game.rounds.size
    val lang = currentLang()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // ── Progress: thin, quiet ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.game_round, state.currentIndex + 1, game.rounds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.game_correct_count, state.answers.count { it }, game.rounds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // ── Question card ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    round.prompt(lang),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                )

                Spacer(Modifier.height(16.dp))

                // ── Options: tactile cards ──
                round.options(lang).forEachIndexed { index, option ->
                    val selected = state.selectedOption == index
                    val isCorrect = index == round.correctIndex
                    val label = ('A'.code + index).toChar().toString()

                    OptionCard(
                        label = label,
                        text = option,
                        enabled = !revealed,
                        selected = selected,
                        isCorrect = isCorrect,
                        revealed = revealed,
                        onClick = { onSelect(index) },
                    )
                    if (index < round.options(lang).lastIndex) Spacer(Modifier.height(10.dp))
                }

                // ── Explanation: slides in quietly ──
                if (revealed) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        round.explanation(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    )
                }
            }
        }

        // ── Next button ──
        if (revealed) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    if (state.currentIndex < game.rounds.size - 1)
                        stringResource(R.string.game_next_round)
                    else
                        stringResource(R.string.game_view_result),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * A single option displayed as a calm, tappable card.
 * Pressed state gives subtle visual feedback before selection.
 */
@Composable
private fun OptionCard(
    label: String,
    text: String,
    enabled: Boolean,
    selected: Boolean,
    isCorrect: Boolean,
    revealed: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            !revealed && pressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            !revealed -> MaterialTheme.colorScheme.outlineVariant
            isCorrect -> Color(0xFF22C55E)
            selected -> Color(0xFFEF4444)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        label = "borderColor",
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            !revealed && pressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            revealed && isCorrect -> Color(0xFFF0FDF4)
            revealed && selected -> Color(0xFFFEF2F2)
            else -> Color.Unspecified // use surface default
        },
        label = "bgColor",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interaction,
                indication = null,
            ),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Letter badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    revealed && isCorrect -> Color(0xFF22C55E).copy(alpha = 0.15f)
                    revealed && selected -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        revealed && isCorrect -> Color(0xFF16A34A)
                        revealed && selected -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.primary
                    },
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (revealed && isCorrect) {
                Text(" ✓", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
            }
            if (revealed && selected && !isCorrect) {
                Text(" ✗", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResultContent(
    modifier: Modifier,
    game: MiniGame,
    state: MiniGameUiState,
    onRestart: () -> Unit,
) {
    val correct = state.answers.count { it }
    val total = game.rounds.size
    val ratio = correct.toFloat() / total.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Result card ──
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

                // Score: large, calm
                Text(
                    "$correct/$total",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(8.dp))

                // XP chip
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

                // Perfect badge
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
            }
        }

        Spacer(Modifier.height(16.dp))

        // Description — subdued
        Text(
            game.description(currentLang()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        // Restart
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                stringResource(R.string.game_play_again),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
