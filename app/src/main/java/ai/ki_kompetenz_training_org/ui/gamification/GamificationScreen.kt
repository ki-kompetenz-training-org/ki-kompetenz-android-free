package ai.ki_kompetenz_training_org.ui.gamification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = KiKompetenzApp.from(context)
    val vm: GamificationViewModel = viewModel {
        GamificationViewModel(app.gamificationRepository)
    }
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Level card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.profile_level, state.level), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Spacer(Modifier.weight(1f))
                            Text(stringResource(R.string.profile_xp, state.xp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (state.xpNeeded <= 0) 1f
                                else (state.xpIntoLevel.toFloat() / state.xpNeeded).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.profile_xp_to_next, state.xpIntoLevel, state.xpNeeded, state.level + 1),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // Streak + check-in
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_day_streak), fontWeight = FontWeight.Bold)
                            Text(
                                if (state.streak > 0) stringResource(R.string.profile_streak_days, state.streak) else stringResource(R.string.profile_no_streak),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = vm::dailyCheckIn,
                            enabled = !state.checkedInToday,
                        ) {
                            Text(if (state.checkedInToday) stringResource(R.string.profile_checked_in) else stringResource(R.string.profile_checkin))
                        }
                    }
                    // Streak freezes: balance + weekly-granted cap note + purchase
                    Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("❄\uFE0F", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_freezes), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                stringResource(R.string.freeze_explain),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.profile_freezes_count, state.freezes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(12.dp))
                        val canBuy = state.freezes < 2 && state.xp >= 100
                        Button(
                            onClick = vm::purchaseFreeze,
                            enabled = canBuy,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                when {
                                    state.freezes >= 2 -> stringResource(R.string.freeze_buy_cap)
                                    state.xp < 100 -> stringResource(R.string.freeze_buy_no_xp)
                                    else -> stringResource(R.string.freeze_buy, 100)
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            // Weekly missions
            if (state.missions.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.mission_section_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.weight(1f))
                                val done = state.missions.count { it.completed }
                                if (done == state.missions.size) {
                                    Text(stringResource(R.string.mission_bonus, 50), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("$done/${state.missions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            state.missions.forEach { m ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            missionTitle(m.id),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (m.completed) FontWeight.Medium else FontWeight.Normal,
                                            color = if (m.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (m.progress.toFloat() / m.target).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    if (m.completed) {
                                        Text(stringResource(R.string.mission_complete_tag), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(
                                            "${m.progress}/${m.target}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            // Progress
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📚", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(stringResource(R.string.profile_lessons), fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.profile_lessons_progress, state.lessonProgress, state.totalLessons),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        LinearProgressIndicator(
                            progress = { state.lessonProgress.toFloat() / state.totalLessons },
                            modifier = Modifier.width(100.dp),
                        )
                    }
                }
            }

            // Badges
            item {
                Text(stringResource(R.string.profile_badges, state.badges.count { it.second }, state.badges.size), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(state.badges, key = { it.first.id }) { (badge, unlocked) ->
                Card(
                    colors = if (unlocked) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    } else CardDefaults.cardColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            badge.emoji,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.alpha(if (unlocked) 1f else 0.3f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                badge.title,
                                fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Medium,
                                color = if (unlocked) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (!unlocked) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = badge.title,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }

            item {
                val lang by app.settingsStore.language.collectAsState(initial = SettingsStore.LANG_SYSTEM)
                val scope = rememberCoroutineScope()
                LanguageSection(
                    currentLang = lang,
                    onLanguageChange = { selected ->
                        scope.launch { app.settingsStore.setLanguage(selected) }
                    },
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.profile_dsgvo),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        }
    }

/** Localized title for a mission id (titles live in strings.xml, not in code data). */
@Composable
fun missionTitle(id: String): String = stringResource(
    when (id) {
        "quiz_play" -> R.string.mission_title_quiz_play
        "quiz_good" -> R.string.mission_title_quiz_good
        "minigame_play" -> R.string.mission_title_minigame_play
        "srs_cards" -> R.string.mission_title_srs_cards
        "lessons" -> R.string.mission_title_lessons
        else -> R.string.mission_title_daily
    }
)
