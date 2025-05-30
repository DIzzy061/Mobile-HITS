package com.example.codeblockhits.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = TealDarkOnPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    secondary = TealDarkSecondary,
    onSecondary = TealDarkOnSecondary,
    secondaryContainer = TealDarkSecondaryContainer,
    onSecondaryContainer = TealDarkOnSecondaryContainer,
    tertiary = TealDarkTertiary,
    onTertiary = TealDarkOnTertiary,
    tertiaryContainer = TealDarkTertiaryContainer,
    onTertiaryContainer = TealDarkOnTertiaryContainer,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = TealDarkSurfaceVariant,
    onSurface = TealDarkOnPrimaryContainer,
    onSurfaceVariant = TealDarkOnPrimaryContainer,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    outline = TealDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TealLightPrimary,
    onPrimary = TealLightOnPrimary,
    primaryContainer = TealLightPrimaryContainer,
    onPrimaryContainer = TealLightOnPrimaryContainer,
    secondary = TealLightSecondary,
    onSecondary = TealLightOnSecondary,
    secondaryContainer = TealLightSecondaryContainer,
    onSecondaryContainer = TealLightOnSecondaryContainer,
    tertiary = TealLightTertiary,
    onTertiary = TealLightOnTertiary,
    tertiaryContainer = TealLightTertiaryContainer,
    onTertiaryContainer = TealLightOnTertiaryContainer,
    background = Color(0xFFFBFFFF),
    surface = Color(0xFFF0FDF4),
    surfaceVariant = TealLightSurfaceVariant,
    onSurface = TealLightOnPrimaryContainer,
    onSurfaceVariant = TealLightOnPrimaryContainer,
    error = Color(0xFFB00020),
    onError = Color.White,
    outline = TealLightOutline
)

@Composable
fun CodeBlockHITSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}