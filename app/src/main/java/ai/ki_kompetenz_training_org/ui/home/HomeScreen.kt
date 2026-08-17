package ai.ki_kompetenz_training_org.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero card (matches web hero)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))))
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤖", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.home_hero_question),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.home_hero_answer),
                            color = Color(0xFFFDE68A),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onOpenQuiz,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1D4ED8)),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.home_quiz_cta), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Gamification card: XP, level, streak, check-in
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = onOpenGamification,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_level, state.level), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        Text(stringResource(R.string.home_xp, state.xp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (state.xpNeeded <= 0) 1f
                            else (state.xpIntoLevel.toFloat() / state.xpNeeded).coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_streak, state.streak), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = vm::dailyCheckIn,
                            enabled = !state.checkedInToday,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Text(if (state.checkedInToday) stringResource(R.string.home_checked_in) else stringResource(R.string.home_checkin))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Premium status chip
            if (state.loggedIn) {
                AssistChip(
                    onClick = { if (!state.premium) onOpenPremium() },
                    label = {
                        Text(
                            if (state.premium) stringResource(R.string.home_premium_active)
                            else stringResource(R.string.home_premium_upsell),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (state.premium) Icons.Default.CheckCircle else Icons.Default.Star,
                            contentDescription = if (state.premium) stringResource(R.string.home_premium_active) else stringResource(R.string.home_premium_upsell),
                            Modifier.size(16.dp),
                        )
                    },
                    modifier = Modifier.semantics { contentDescription = if (state.premium) "Premium aktiv" else "Premium freischalten" },
                )
            } else {
                AssistChip(
                    onClick = onLogin,
                    label = { Text(stringResource(R.string.home_login_cta)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.home_login_cta), Modifier.size(16.dp)) },
                )
            }

            Spacer(Modifier.height(20.dp))

            // Feature grid: Basic vs Premium
            FeatureCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = stringResource(R.string.home_feature_lessons),
                subtitle = stringResource(R.string.home_feature_lessons_desc),
                action = stringResource(R.string.home_feature_lessons_action),
                onClick = onOpenLessons,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.AutoMirrored.Filled.Help,
                title = stringResource(R.string.home_feature_quiz),
                subtitle = stringResource(R.string.home_feature_quiz_desc),
                action = stringResource(R.string.home_feature_quiz_action),
                onClick = onOpenQuiz,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.Default.Star,
                title = stringResource(R.string.home_feature_games),
                subtitle = stringResource(R.string.home_feature_games_desc),
                action = stringResource(R.string.home_feature_games_action),
                onClick = onOpenMiniGames,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = stringResource(R.string.home_feature_srs),
                subtitle = stringResource(R.string.home_feature_srs_desc),
                action = stringResource(R.string.home_feature_srs_action),
                onClick = onOpenSrs,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.home_feature_profile),
                subtitle = stringResource(R.string.home_feature_profile_desc),
                action = stringResource(R.string.home_feature_profile_action),
                onClick = onOpenGamification,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.Default.Star,
                title = "Premium",
                subtitle = "Alle Lektionen · Spaced Repetition · Zertifikat · Team-Ranking",
                action = if (state.premium) stringResource(R.string.home_feature_premium_action_active) else stringResource(R.string.home_feature_premium_action),
                onClick = onOpenPremium,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.Default.Person,
                title = stringResource(R.string.home_feature_team),
                subtitle = stringResource(R.string.home_feature_team_desc),
                action = stringResource(R.string.home_feature_team_action),
                onClick = {
                    if (state.loggedIn) onOpenTeam() else onLogin()
                },
            )

            Spacer(Modifier.height(20.dp))

            // ForKids & ForSeniors
            FeatureCard(
                icon = Icons.Default.Star,
                title = "ForKids",
                subtitle = "KI spielerisch entdecken - COPPA-konform, lokal, sicher",
                action = "Starten",
                onClick = onOpenForKids,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                icon = Icons.Default.Star,
                title = "Fuer Senioren",
                subtitle = "Passwoerter, Phishing, KI-Telefone, Deepfakes",
                action = "Starten",
                onClick = onOpenForSeniors,
            )

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.home_dsgvo_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text(action, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}