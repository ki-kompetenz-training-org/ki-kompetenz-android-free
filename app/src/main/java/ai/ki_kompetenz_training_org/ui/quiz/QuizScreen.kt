package ai.ki_kompetenz_training_org.ui.quiz

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.ui.quiz.QuizConstants
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun QuizScreen(onBack: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: QuizViewModel = viewModel {
        QuizViewModel(app.contentRepository, app.db, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quiz_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.quiz_back))
                    }
                },
            )
        },
    ) { padding ->
        when (state.phase) {
            QuizPhase.LOADING -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            QuizPhase.INTRO -> IntroContent(
                modifier = Modifier.padding(padding),
                questionCount = state.questions.size,
                onStart = vm::start,
            )

            QuizPhase.PLAYING -> PlayingContent(
                modifier = Modifier.padding(padding),
                state = state,
                onSelect = vm::selectOption,
                onNext = vm::next,
            )

            QuizPhase.RESULT -> ResultContent(
                modifier = Modifier.padding(padding),
                state = state,
                onRestart = vm::restart,
            )
        }
    }
}

@Composable
private fun IntroContent(modifier: Modifier, questionCount: Int, onStart: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF7C3AED)))).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", style = MaterialTheme.typography.displaySmall)
                    Text("KI-Score", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Wie KI-fit bist du?", color = Color(0xFFDBEAFE), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatChip("$questionCount", stringResource(R.string.quiz_stats_questions))
                        StatChip("~3", stringResource(R.string.quiz_stats_minutes))
                        StatChip("5", stringResource(R.string.quiz_stats_levels))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Teste dein Wissen zu Künstlicher Intelligenz, EU AI Act und KI im Arbeitsalltag. Am Ende erhältst du deinen persönlichen KI-Score zum Teilen!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(stringResource(R.string.quiz_start), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFBFDBFE), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PlayingContent(
    modifier: Modifier,
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val question = state.questions.getOrNull(state.currentIndex)
    if (question == null) {
        Box(modifier, contentAlignment = Alignment.Center) { Text(stringResource(R.string.quiz_no_questions)) }
        return
    }
    val revealed = state.selectedOption != null
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // HUD: Herz-System (Lives), Timer, Combo, Punkte
        HUD(state = state)
        
        Spacer(Modifier.height(8.dp))
        
        // Progress
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.quiz_question_count, state.currentIndex + 1, state.questions.size), style = MaterialTheme.typography.labelMedium)
            Text(stringResource(R.string.quiz_correct_count, state.answers.count { it }, state.questions.size), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("${question.emoji}  ${question.text}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                question.options.forEachIndexed { index, option ->
                    val selected = state.selectedOption == index
                    val isCorrect = index == question.correct
                    val borderColor = when {
                        !revealed -> MaterialTheme.colorScheme.outlineVariant
                        isCorrect -> Color(0xFF22C55E)
                        selected && !isCorrect -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    OutlinedButton(
                        onClick = { onSelect(index) },
                        enabled = !revealed,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (revealed && (isCorrect || selected)) {
                                if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                            } else MaterialTheme.colorScheme.surface,
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                ('A'.code + index).toChar().toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(option, Modifier.weight(1f))
                            if (revealed && isCorrect) Text("✓", color = Color(0xFF16A34A))
                            if (revealed && selected && !isCorrect) Text("✗", color = Color(0xFFDC2626))
                        }
                    }
                }

                if (revealed) {
                    Spacer(Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            buildString {
                                append(if (state.selectedOption == question.correct) "✅ Richtig! " else "💡 Gut zu wissen: ")
                                append(question.explanation)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }

        if (state.selectedOption != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    if (state.currentIndex < state.questions.size - 1) "Nächste Frage" else "Mein Ergebnis",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ResultContent(modifier: Modifier, state: QuizUiState, onRestart: () -> Unit) {
    val tier = state.tier
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(horizontal = 24.dp, vertical = 28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(tier?.emoji ?: "\uD83E\uDD16", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.quiz_your_score), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(
                    "${state.score}/100",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (tier?.title != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(tier!!.title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                if (tier?.description != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(tier!!.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox("${state.answers.count { it }}/${state.questions.size}", "Richtig", Modifier.weight(1f))
            StatBox("${state.score}", "Punkte", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox("${state.maxCombo}×", "Max Combo", Modifier.weight(1f))
            StatBox("${state.lives}/3", "Leben übrig", Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val text = buildString {
                    append(state.sharePrefix.replace("{score}", state.score.toString())
                        .replace("{emoji}", tier?.emoji ?: "")
                        .replace("{tier}", tier?.title ?: ""))
                    append("\n\n")
                    append("https://ki-kompetenz-training.org/ki-score")
                }
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Ergebnis teilen"))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2)),
        ) {
            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.quiz_share))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.quiz_share), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.quiz_play_again))
        }
    }
}

@Composable
private fun HUD(state: QuizUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leverkusen (Punkte)
        ScoreDisplay(score = state.scorePoints)
        
        // Timer (Countdown)
        TimerDisplay(timeLeft = state.timeLeft)
        
        // combo (Multiplikator)
        ComboDisplay(combo = state.combo)
        
        // Herzen (Lives)
        LivesDisplay(lives = state.lives)
    }
}

@Composable
private fun ScoreDisplay(score: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🏆", modifier = Modifier.padding(end = 4.dp))
        Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TimerDisplay(timeLeft: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⏳", modifier = Modifier.padding(end = 4.dp))
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((timeLeft.toFloat() / QuizConstants.ROUND_SECONDS))
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ComposeColor(0xFF22C55E),
                                ComposeColor(0xFF16A34A),
                            )
                        )
                    )
            )
        }
        Text("$timeLeft", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ComboDisplay(combo: Int) {
    if (combo >= 2) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", modifier = Modifier.padding(end = 4.dp))
            Text("${combo}×", style = MaterialTheme.typography.labelMedium, color = ComposeColor(0xFFF97316))
        }
    }
}

@Composable
private fun LivesDisplay(lives: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(QuizConstants.MAX_LIVES) { index ->
            val heartColor = if (index < lives) ComposeColor(0xFFEF4444) else ComposeColor(0xFF7F1D1D)
            Text("❤️", color = heartColor, modifier = Modifier.padding(1.dp))
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}