package ai.ki_kompetenz_training_org.ui.srs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.repo.SrsQuality

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SrsScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: SrsViewModel = viewModel {
        SrsViewModel(app.authRepository, app.srsRepository, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.srs_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.srs_back))
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            SrsPhase.LOADING -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            SrsPhase.NOT_LOGGED_IN -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.srs_login_required), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.srs_login_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = stringResource(R.string.srs_login))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.srs_login), fontWeight = FontWeight.Bold)
                }
            }

            SrsPhase.NO_CARDS -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🎉", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.srs_all_done_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.srs_all_done_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SrsPhase.ERROR -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error ?: stringResource(R.string.common_error), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = vm::load) { Text(stringResource(R.string.common_retry)) }
            }

            SrsPhase.REVIEW -> ReviewContent(
                modifier = Modifier.padding(padding),
                state = state,
                onReveal = vm::revealAnswer,
                onRate = vm::rate,
            )

            SrsPhase.FINISHED -> FinishedContent(
                modifier = Modifier.padding(padding),
                state = state,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun ReviewContent(
    modifier: Modifier,
    state: SrsUiState,
    onReveal: () -> Unit,
    onRate: (Int) -> Unit,
) {
    val card = state.currentCard
    if (card == null) {
        Box(modifier, contentAlignment = Alignment.Center) { Text(stringResource(R.string.srs_no_cards)) }
        return
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // Progress
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.srs_card_progress, state.currentIndex + 1, state.cards.size),
                style = MaterialTheme.typography.labelMedium,
            )
            Text("+${state.earnedXp} XP", style = MaterialTheme.typography.labelMedium, color = Color(0xFFF59E0B))
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = {
                ai.ki_kompetenz_training_org.data.repo.SrsSession.progress(state.reviewsDone + if (state.showAnswer) 0 else 0, state.cards.size)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Lektion ${card.lessonId.takeLast(2)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    card.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!state.showAnswer) {
            Button(
                onClick = onReveal,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(R.string.srs_reveal_answer), fontWeight = FontWeight.Bold)
            }
        } else {
            // Answer card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.srs_answer), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        card.answer,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Rating buttons (Anki-style 1-5)
            Text(stringResource(R.string.srs_rating_question), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            SrsQuality.entries.forEach { quality ->
                OutlinedButton(
                    onClick = { onRate(quality.value) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "${quality.emoji} ${quality.label}",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FinishedContent(modifier: Modifier, state: SrsUiState, onBack: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎓", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.srs_finished_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.srs_finished_count, state.reviewsDone),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.srs_xp_earned, state.earnedXp)) },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = stringResource(R.string.srs_xp_earned, state.earnedXp), Modifier.size(16.dp), tint = Color(0xFFF59E0B)) },
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text(stringResource(R.string.srs_done), fontWeight = FontWeight.Bold)
        }
    }
}