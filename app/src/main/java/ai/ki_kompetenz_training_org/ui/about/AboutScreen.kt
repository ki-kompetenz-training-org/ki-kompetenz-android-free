package ai.ki_kompetenz_training_org.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.R

/**
 * About / Disclaimer screen.
 *
 * Shows a clear disclaimer that this app is NOT an official government app,
 * links to the official EU AI Act source (eur-lex.europa.eu),
 * and states that all information is provided without guarantee.
 *
 * Required by Google Play policy: "Irreführende Behauptungen" (Misleading Claims).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val sourceUrl = stringResource(R.string.disclaimer_source_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.disclaimer_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Disclaimer body
            Text(
                text = stringResource(R.string.disclaimer_body),
                style = MaterialTheme.typography.bodyMedium,
            )

            // No warranty
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Text(
                    text = stringResource(R.string.disclaimer_no_warranty),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Official source
            Text(
                text = stringResource(R.string.disclaimer_source),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )

            // Source URL button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    // a11y: dekorativ — der Text daneben beschreibt die Aktion
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.disclaimer_open_source))
            }

            Text(
                text = sourceUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // App info
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "https://ki-kompetenz-training.org",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "https://ki-kompetenz-training.org/datenschutz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
