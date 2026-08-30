package ai.ki_kompetenz_training_org.ui.minigames

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.minigames.Difficulty
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MiniGamesMenuState(
    val premium: Boolean = false,
    val checked: Boolean = false,
)

class MiniGamesMenuViewModel(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MiniGamesMenuState())
    val state: StateFlow<MiniGamesMenuState> = _state

    init {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                premiumRepository.isPremium().onSuccess { premium ->
                    _state.value = MiniGamesMenuState(premium = premium, checked = true)
                }.onFailure {
                    _state.value = MiniGamesMenuState(checked = true)
                }
            } else {
                _state.value = MiniGamesMenuState(checked = true)
            }
        }
    }
}

@Composable
fun MiniGamesMenuScreen(
    onBack: () -> Unit,
    onOpenGame: (MiniGame) -> Unit,
    onOpenPremium: () -> Unit,
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: MiniGamesMenuViewModel = viewModel {
        MiniGamesMenuViewModel(app.authRepository, app.premiumRepository)
    }
    val state by vm.state.collectAsState()

    if (!state.checked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            item {
                Text(
                    stringResource(R.string.games_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }

            // Free games
            items(MiniGames.FREE, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    locked = false,
                    onClick = { onOpenGame(game) },
                )
            }

            // Premium games
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⭐ Premium-Spiele",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Fortgeschrittene Deep-Dives mit höheren XP-Belohnungen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }
            items(MiniGames.PREMIUM, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    locked = !state.premium,
                    onClick = {
                        if (state.premium) onOpenGame(game) else onOpenPremium()
                    },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.games_dsgvo_note),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        }
}

@Composable
private fun DifficultyBadge(difficulty: Difficulty) {
    val color = when (difficulty) {
        Difficulty.BEGINNER -> Color(0xFF22C55E)
        Difficulty.INTERMEDIATE -> Color(0xFFFFA500)
        Difficulty.EXPERT -> Color(0xFFEF4444)
    }
    val label = when (difficulty) {
        Difficulty.BEGINNER -> stringResource(R.string.difficulty_beginner)
        Difficulty.INTERMEDIATE -> stringResource(R.string.difficulty_intermediate)
        Difficulty.EXPERT -> stringResource(R.string.difficulty_expert)
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        modifier = Modifier.padding(start = 4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun GameCard(
    game: MiniGame,
    locked: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = if (locked) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        } else CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                game.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.alpha(if (locked) 0.4f else 1f),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(game.title(currentLang()), fontWeight = FontWeight.Bold)
                    if (game.premium) {
                        Spacer(Modifier.width(6.dp))
                        if (locked) {
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.games_premium_locked), Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        } else {
                            Text("⭐", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    DifficultyBadge(difficulty = game.difficulty)
                }
                Text(
                    game.description(currentLang()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (game.isArena3D) {
                        stringResource(R.string.games_arena_badge)
                    } else if (game.premium) {
                        if (locked) stringResource(R.string.games_premium_locked, game.rounds.size)
                        else stringResource(R.string.games_premium_round_count, game.rounds.size)
                    } else {
                        stringResource(R.string.games_round_count, game.rounds.size)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (game.premium && locked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
