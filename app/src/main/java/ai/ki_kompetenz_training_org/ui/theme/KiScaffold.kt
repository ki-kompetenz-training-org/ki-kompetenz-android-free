package ai.ki_kompetenz_training_org.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Standardized Scaffold configuration for the KI-Kompetenz app.
 *
 * Ensures consistent:
 * - Window inset handling (edge-to-edge safety)
 * - TopAppBar scroll behavior
 * - Content padding
 *
 * Usage:
 * ```
 * KiScaffold(
 *     topBar = { TopAppBar(title = { Text("Title") }) },
 * ) { padding ->
 *     LazyColumn(modifier = Modifier.padding(padding)) { ... }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (padding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content,
    )
}
