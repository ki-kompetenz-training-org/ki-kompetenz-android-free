package ai.ki_kompetenz_training_org.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp

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
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: HomeViewModel = viewModel {
        HomeViewModel(app.authRepository, app.premiumRepository, app.teamRepository, app.contentRepository, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── KiBot placeholder (Task 3 replaces with real 3D) ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* KiBot tap reaction — wired in Task 4 */ },
            shape = RoundedCornerShape(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    )
                    .padding(vertical = 24.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "KiBot",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Level ${state.level}",
                        color = Color(0xFFFDE68A),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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

        // ── Quick-action grid (2×2) ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
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
                    subtitle = "Spaced Repetition",
                    onClick = onOpenSrs,
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
