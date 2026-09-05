package ai.ki_kompetenz_training_org.ui.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ai.ki_kompetenz_training_org.BuildConfig
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.prefs.TokenStore

/**
 * WebView-based OAuth login (mirrors the web SSO flow).
 * After the user logs in, the session cookies (kkt_access/kkt_refresh) are
 * extracted from the CookieManager and stored securely; all API calls then
 * carry them like the browser does.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun AuthScreen(onBack: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val tokenStore = app.tokenStore
    val context = LocalContext.current
    var loggedIn by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(false) }
    var webError by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Poll the CookieManager for the session cookie during the OAuth flow.
    LaunchedEffect(Unit) {
        while (!loggedIn) {
            kotlinx.coroutines.delay(800)
            val cookies = CookieManager.getInstance().getCookie(BuildConfig.API_BASE_URL) ?: ""
            val kktAccess = cookies.split("; ").firstOrNull { it.startsWith("kkt_access=") }
            if (kktAccess != null) {
                val value = kktAccess.removePrefix("kkt_access=")
                if (value.isNotBlank()) {
                    tokenStore.setCookie("kkt_access", value)
                    cookies.split("; ").firstOrNull { it.startsWith("kkt_refresh=") }
                        ?.removePrefix("kkt_refresh=")
                        ?.let { tokenStore.setCookie("kkt_refresh", it) }
                    loggedIn = true
                }
            }
        }
    }

    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            kotlinx.coroutines.delay(600)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auth_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.auth_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webView = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                progress = true
                                webError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                progress = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?,
                            ) {
                                progress = false
                                // Ohne Netz blieb der Screen vorher einfach weiss
                                // (BUG-Report 2026-09-05) — jetzt Fehlerzustand + Retry.
                                webError = true
                            }
                        }
                        loadUrl("${BuildConfig.API_BASE_URL}/login")
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (webError) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            stringResource(R.string.common_error),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            webError = false
                            progress = true
                            webView?.loadUrl("${BuildConfig.API_BASE_URL}/login")
                        }) {
                            Text(stringResource(R.string.common_retry))
                        }
                    }
                }
            }

            if (progress) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            if (loggedIn) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        stringResource(R.string.auth_logged_in),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}