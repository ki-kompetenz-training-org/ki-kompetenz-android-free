package ai.ki_kompetenz_training_org.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Purple/blue brand gradient (matches the web theme)
private val BrandPrimary = Color(0xFF4F46E5)      // indigo-600
private val BrandPrimaryDark = Color(0xFF7C3AED)   // violet-600
private val BrandBackground = Color(0xFFF9FAFB)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandPrimaryDark,
    background = Color(0xFFF0F2F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8ECF0),
    onSurfaceVariant = Color(0xFF44474E),
    outlineVariant = Color(0xFFC4C9D0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFFA78BFA),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF1A2035),
    surfaceVariant = Color(0xFF252D44),
    onSurfaceVariant = Color(0xFFB8BCC9),
    outlineVariant = Color(0xFF3A4260),
)

@Composable
fun KiKompetenzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}