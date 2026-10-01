package org.xuan.dynamis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF205866),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C9EAD),
    onPrimaryContainer = Color(0xFF102832),
    background = Color(0xFFA7C0CD),
    onBackground = Color(0xFF102832),
    surface = Color(0xFFECF4F6),
    onSurface = Color(0xFF102832),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA1D4E0),
    onPrimary = Color(0xFF06333D),
    primaryContainer = Color(0xFF1E404C),
    onPrimaryContainer = Color(0xFFEDF5F7),
    background = Color(0xFF0D2029),
    onBackground = Color(0xFFEDF5F7),
    surface = Color(0xFF132C37),
    onSurface = Color(0xFFEDF5F7),
)

@Composable
fun DynamisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}
