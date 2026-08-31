package ai.ki_kompetenz_training_org.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.R
import kotlinx.coroutines.launch

/**
 * 4-step onboarding flow for first-time users.
 * Step 1: Language selection (persisted immediately, steps render localized)
 * Step 2: Welcome - what is KI-Kompetenz
 * Step 3: KiBot intro - your AI companion grows with you
 * Step 4: Start learning - overview of features
 *
 * [onCompleted] receives whether the user chose to start lesson 1 directly.
 */
@Composable
fun OnboardingScreen(
    onCompleted: (startLesson1: Boolean) -> Unit,
    settingsStore: ai.ki_kompetenz_training_org.data.prefs.SettingsStore,
    onLanguageSelected: () -> Unit = {},
) {
    val context = LocalContext.current
    val systemLanguage = LocalConfiguration.current.locales[0].language
    val storedLang by settingsStore.language.collectAsState(
        initial = OnboardingLang.defaultLanguage(systemLanguage)
    )
    // Fresh installs store LANG_SYSTEM; resolve it so English is preselected.
    val selectedLang = when (storedLang) {
        ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_DE ->
            ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_DE
        ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_EN ->
            ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_EN
        else -> OnboardingLang.defaultLanguage(systemLanguage)
    }

    // Language step (step 1): shown until the user picks a language.
    var languageChosen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (!languageChosen) {
        OnboardingLanguageStep(
            selectedLang = selectedLang,
            onSelect = { lang ->
                languageChosen = true
                onLanguageSelected()
                // Persist immediately (DataStore) + mirror to prefs so the
                // next launch boots with the chosen locale.
                scope.launch { settingsStore.setLanguage(lang) }
                context.getSharedPreferences(
                    "kikompetenz_settings", android.content.Context.MODE_PRIVATE,
                ).edit().putString("language", lang).apply()
            },
        )
        return
    }

    // Localized composition for the remaining steps: stringResource follows the
    // selected language without an activity recreate.
    val localizedConfig = remember(selectedLang) {
        android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(java.util.Locale.forLanguageTag(selectedLang))
        }
    }
    val localizedContext = remember(localizedConfig) {
        context.createConfigurationContext(localizedConfig)
    }
    CompositionLocalProvider(
        LocalConfiguration provides localizedConfig,
        LocalContext provides localizedContext,
    ) {
        OnboardingSteps(onCompleted = onCompleted)
    }
}

@Composable
private fun OnboardingLanguageStep(
    selectedLang: String,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface,
                    )
                )
            )
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("\uD83C\uDF10", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.onboarding_language_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        val store = ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_DE
        LanguageChoice(
            emoji = "\uD83C\uDDE9\uD83C\uDDEA",
            label = "Deutsch",
            selected = selectedLang == store,
        ) { onSelect(store) }
        Spacer(Modifier.height(12.dp))
        val storeEn = ai.ki_kompetenz_training_org.data.prefs.SettingsStore.LANG_EN
        LanguageChoice(
            emoji = "\uD83C\uDDEC\uD83C\uDDE7",
            label = "English",
            selected = selectedLang == storeEn,
        ) { onSelect(storeEn) }
    }
}

@Composable
private fun LanguageChoice(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            if (selected) {
                Text(
                    "\u2713",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun OnboardingSteps(onCompleted: (startLesson1: Boolean) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.onboarding_welcome_title),
            subtitle = stringResource(R.string.onboarding_welcome_subtitle),
            body = stringResource(R.string.onboarding_welcome_body),
        ),
        OnboardingPage(
            icon = Icons.Default.SportsEsports,
            title = stringResource(R.string.onboarding_kibot_title),
            subtitle = stringResource(R.string.onboarding_kibot_subtitle),
            body = stringResource(R.string.onboarding_kibot_body),
        ),
        OnboardingPage(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.onboarding_start_title),
            subtitle = stringResource(R.string.onboarding_start_subtitle),
            body = stringResource(R.string.onboarding_start_body),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface,
                    )
                )
            ),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { index ->
                val color = if (index == pagerState.currentPage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pagerState.currentPage < 2) {
                TextButton(onClick = { onCompleted(false) }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            } else {
                TextButton(onClick = { onCompleted(false) }) {
                    Text(stringResource(R.string.onboarding_cta_explore))
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onCompleted(true)
                    }
                },
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    if (pagerState.currentPage < 2) {
                        stringResource(R.string.onboarding_next)
                    } else {
                        stringResource(R.string.onboarding_cta_lesson1)
                    },
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon in a circle
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            page.subtitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            page.body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val body: String,
)
