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
import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.ui.daily.DailyChallengeCard
import ai.ki_kompetenz_training_org.ui.daily.DailyChallengeViewModel
import ai.ki_kompetenz_training_org.ui.kibot.KiBotScene
import ai.ki_kompetenz_training_org.ui.kibot.KiBotState
import ai.ki_kompetenz_training_org.ui.kibot.daysSinceLastCheckIn

@Composable
fun HomeScreen(
    onOpenQuiz: () -> Unit,
    onOpenLessons: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenTeam: () -> Unit,
    onLogin: () -> Unit,
    onOpenMiniGames: () -> Unit,
    onOpenGamification: () -> Unit,
    onOpenSrs: () -> Unit,
    onOpenForKids: () -> Unit,
    onOpenForSeniors: () -> Unit,
    onOpenMiniGame: (String) -> Unit = {},
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: HomeViewModel = viewModel {
        HomeViewModel(app.authRepository, app.premiumRepository, app.teamRepository, app.contentRepository, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()
    
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
                shape = RoundedCornerShape(14.dp),
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
            shape = RoundedCornerShape(14.dp),
            onClick = onOpenGamification,
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${state.level}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("${state.xp} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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
                    Text("🔥 ${state.streak} Tage", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = vm::dailyCheckIn,
                        enabled = !state.checkedInToday,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    ) {
                        Text(if (state.checkedInToday) "✓ Gecheckt" else "Check-in")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Daily Challenge card ──
        val dailyVm: DailyChallengeViewModel = viewModel {
            DailyChallengeViewModel(
                DailyChallengeRepository(
                    app.getSharedPreferences("kikompetenz_gamification", android.content.Context.MODE_PRIVATE)
                )
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
                    title = "KI-Score",
                    subtitle = "Teste dein Wissen",
                    onClick = onOpenQuiz,
                )
            }
            item {
                QuickActionCard(
                    emoji = "🎮",
                    title = "Mini-Spiele",
                    subtitle = "8 kostenlose Spiele",
                    onClick = onOpenMiniGames,
                )
            }
            item {
                QuickActionCard(
                    emoji = "📖",
                    title = "Lektionen",
                    subtitle = "EU AI Act lernen",
                    onClick = onOpenLessons,
                )
            }
            item {
                QuickActionCard(
                    emoji = "🔄",
                    title = "Wiederholen",
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { contentDescription = kibotDescription },
            shape = RoundedCornerShape(20.dp),
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

        // ── "More" section: Kids + Seniors ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                onClick = onOpenForKids,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("👶", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Für Kinder", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("KI spielerisch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                onClick = onOpenForSeniors,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("👴", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Für Senioren", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Phishing, Deepfakes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Premium chip (only if not premium) ──
        if (state.loggedIn && !state.premium) {
            AssistChip(
                onClick = onOpenPremium,
                label = { Text("⭐ Premium freischalten") },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = "Premium", Modifier.size(16.dp))
                },
                modifier = Modifier.semantics { contentDescription = "Premium freischalten" },
            )
        } else if (!state.loggedIn) {
            AssistChip(
                onClick = onLogin,
                label = { Text("Anmelden") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Anmelden", Modifier.size(16.dp)) },
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── DSGVO footer ──
        Text(
            "DSGVO-konform: XP, Level und Serie werden lokal gespeichert — keine Server-Übertragung.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Spacer(Modifier.height(8.dp))
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
        shape = RoundedCornerShape(16.dp),
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
