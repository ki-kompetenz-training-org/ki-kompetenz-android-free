package ai.ki_kompetenz_training_org.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.ui.common.SkeletonList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun LessonsScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenPremium: () -> Unit,
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: LessonsViewModel = viewModel {
        LessonsViewModel(app.contentRepository, app.premiumRepository, app.gamificationRepository, app.settingsStore)
    }
    val state by vm.state.collectAsState()

    when {
            state.loading && state.lessons.isEmpty() -> SkeletonList(rows = 6)
            state.loadFailed && state.lessons.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.lessons_load_error),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.retry() }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }
            state.lessons.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.common_no_data))
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.lessons_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(state.lessons, key = { it.slug }) { lesson ->
                    val isPremium = vm.premiumRepository.isPremiumLesson(lesson.lessonNumber)
                    val isCompleted = lesson.slug in state.completedSlugs
                    val isInProgress = lesson.slug == state.lastOpenedSlug
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenLesson(lesson.slug) },
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                when {
                                    isCompleted -> Icons.Default.CheckCircle
                                    isPremium -> Icons.Default.Lock
                                    else -> Icons.AutoMirrored.Filled.List
                                },
                                stringResource(R.string.lessons_completed_cd).takeIf { isCompleted },
                                tint = when {
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    isPremium -> MaterialTheme.colorScheme.outline
                                    else -> MaterialTheme.colorScheme.primary
                                },
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${lesson.lessonNumber ?: "?"}. ${lesson.title}",
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    if (isInProgress) stringResource(R.string.lessons_in_progress)
                                    else lesson.duration ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isPremium) {
                                Text(
                                    stringResource(R.string.lessons_premium),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenPremium,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    ) {
                        Text(stringResource(R.string.lessons_unlock_cta), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
