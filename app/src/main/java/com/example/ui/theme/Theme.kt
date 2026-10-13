package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = CardWhite,
    primaryContainer = InkBlack,
    onPrimaryContainer = CardWhite,
    secondary = TextSecondary,
    onSecondary = CardWhite,
    background = ScreenBackground,
    onBackground = InkBlack,
    surface = CardWhite,
    onSurface = InkBlack,
    surfaceVariant = ScreenBackground,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTextPrimary,
    onPrimary = DarkScreenBackground,
    primaryContainer = DarkBubbleSent,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextSecondary,
    onSecondary = DarkScreenBackground,
    background = DarkScreenBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkChatBackground,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
