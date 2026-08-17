package ai.ki_kompetenz_training_org.ui.premium

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=ai.ki_kompetenz_training_org"

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun PremiumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.premium_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(R.string.premium_plan_name), Modifier.size(48.dp), tint = Color(0xFFD97706))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.premium_plan_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.premium_price),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            FeatureRow(stringResource(R.string.premium_f1), stringResource(R.string.premium_f1_desc))
            FeatureRow(stringResource(R.string.premium_f2), stringResource(R.string.premium_f2_desc))
            FeatureRow(stringResource(R.string.premium_f3), stringResource(R.string.premium_f3_desc))
            FeatureRow(stringResource(R.string.premium_f4), stringResource(R.string.premium_f4_desc))
            FeatureRow(stringResource(R.string.premium_f5), stringResource(R.string.premium_f5_desc))
            FeatureRow(stringResource(R.string.premium_f6), stringResource(R.string.premium_f6_desc))

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    // Free Edition: Premium-Inhalte gibt es in der Google-Play-Version
                    // (ai.ki_kompetenz_training_org) mit allen 16 Spielen + 14 Lektionen.
                    val url = PLAY_STORE_URL
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            ) {
                Text(stringResource(R.string.premium_cta), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.premium_trust),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeatureRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = title, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp).padding(2.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}