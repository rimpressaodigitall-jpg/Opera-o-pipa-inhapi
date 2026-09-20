package com.example.operacaopipa.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandLightBlue,
    onPrimaryContainer = BrandNavyDark,
    secondary = BrandBlue,
    onSecondary = SurfaceWhite,
    tertiary = BrandTeal,
    background = NeutralLight,
    surface = SurfaceWhite,
    onBackground = NeutralDark,
    onSurface = NeutralDark,
    outline = BorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = BrandNavyDark,
    primaryContainer = BrandNavy,
    onPrimaryContainer = BrandLightBlue,
    secondary = BrandTeal,
    onSecondary = SurfaceWhite,
    tertiary = BrandBlue,
    background = BrandNavyDark,
    surface = Color(0xFF132F5B),
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite
)

@Composable
fun OperacaoPipaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
