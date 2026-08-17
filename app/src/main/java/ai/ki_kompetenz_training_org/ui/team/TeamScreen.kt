package ai.ki_kompetenz_training_org.ui.team

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.BuildConfig
import ai.ki_kompetenz_training_org.KiKompetenzApp

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun TeamScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    val context = LocalContext.current
    val app = KiKompetenzApp.from(context)
    val vm: TeamViewModel = viewModel {
        TeamViewModel(app.authRepository, app.teamRepository, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.team_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.team_back))
                    }
                },
            )
        },
    ) { padding ->
        when {
            !state.loggedIn -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.team_login_required), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.team_login_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(Icons.Default.Person, contentDescription = stringResource(R.string.team_login))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.team_login), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val url = "${BuildConfig.API_BASE_URL}/team/new"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.team_create_web))
                }
            }

            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.team?.team == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = stringResource(R.string.team_title), Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.team_no_team), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.team_no_team_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {
                        val url = "${BuildConfig.API_BASE_URL}/team/new"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                ) {
                    Text(stringResource(R.string.team_create_web_btn))
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(state.team!!.team!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.team_members_avg, state.members, state.avgScore),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (state.ownRank != null) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.team_your_rank, state.ownRank ?: 0)) },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = "Punkte", Modifier.size(16.dp)) },
                            )
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.team_ranking), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (state.leaderboard.isEmpty()) {
                    item {
                        Text(stringResource(R.string.team_no_scores), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(state.leaderboard, key = { "${it.rank}-${it.name}" }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (entry.isMe) {
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else CardDefaults.cardColors(),
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    when (entry.rank) {
                                        1 -> "🥇"
                                        2 -> "🥈"
                                        3 -> "🥉"
                                        else -> "#${entry.rank}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.width(36.dp),
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        entry.name ?: stringResource(R.string.team_anon),
                                        fontWeight = if (entry.isMe) FontWeight.Bold else FontWeight.Medium,
                                    )
                                    Text(
                                        entry.tier ?: "–",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    entry.score?.toString() ?: "–",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}