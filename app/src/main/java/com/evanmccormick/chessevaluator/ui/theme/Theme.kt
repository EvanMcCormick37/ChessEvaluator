package com.evanmccormick.chessevaluator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Chess Evaluator custom colors
val DarkTeal = Color(0xFF004D40)
val MediumTeal = Color(0xFF00796B)
val LightTeal = Color(0xFF26A69A)
val AccentTeal = Color(0xFF1DE9B6)

val DarkPurple = Color(0xFF4A148C)
val MediumPurple = Color(0xFF7B1FA2)
val LightPurple = Color(0xFF9C27B0)

val DarkAccent = Color(0xFF880E4F)
val LightAccent = Color(0xFFD81B60)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val LightGray = Color(0xFFF0F0F0)
val DarkGray = Color(0xFF424242)


val LightSquare = Color(0xffe9f2f2)
val DarkSquare = Color(0xff0f7d7d)

// Leaderboard specific colors
val LeaderboardHeaderBg = DarkTeal
val LeaderboardGold = Color(0xFFFFD700)
val LeaderboardSilver = Color(0xFFC0C0C0)
val LeaderboardBronze = Color(0xFFCD7F32)
val DarkRowBg = Color(0xFF2C2C2C)
val DarkerRowBg = Color(0xFF1E1E1E)

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = MediumTeal,
    onPrimary = White,
    primaryContainer = DarkTeal,
    onPrimaryContainer = AccentTeal,
    secondary = LightPurple,
    onSecondary = White,
    secondaryContainer = DarkPurple,
    onSecondaryContainer = Color.White,
    tertiary = DarkAccent,
    onTertiary = White,
    tertiaryContainer = DarkAccent,
    onTertiaryContainer = White,
    background = DarkGray,
    onBackground = White,
    surface = Color(0xFF121212),
    onSurface = White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = LightGray,
    error = Color(0xFFCF6679),
    onError = Black
)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = MediumTeal,
    onPrimary = White,
    primaryContainer = LightTeal,
    onPrimaryContainer = DarkTeal,
    secondary = MediumPurple,
    onSecondary = AccentTeal,
    secondaryContainer = LightPurple,
    onSecondaryContainer = White,
    tertiary = LightAccent,
    onTertiary = White,
    tertiaryContainer = LightAccent,
    onTertiaryContainer = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    error = Color(0xFFB00020),
    onError = White
)

// Custom extended colors that aren't part of Material3 ColorScheme
data class ExtendedColors(
    val chessBlack: Color = LightSquare,
    val chessWhite: Color = DarkSquare,
    val navBarColor: Color = DarkTeal,
    val evaluationSliderThumb: Color = MediumPurple,
    val evaluationBlack: Color = Black,
    val evaluationWhite: Color = White,
    val tagBackground: Color = DarkAccent,
    // Leaderboard specific colors
    val leaderboardHeaderBg: Color = LeaderboardHeaderBg,
    val goldMedal: Color = LeaderboardGold,
    val silverMedal: Color = LeaderboardSilver,
    val bronzeMedal: Color = LeaderboardBronze,
    val rowBackgroundEven: Color = DarkRowBg,
    val rowBackgroundOdd: Color = DarkerRowBg
)

// Local composition to provide the extended colors
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChessEvaluatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Extended colors based on theme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            navBarColor = DarkTeal,
            tagBackground = DarkAccent,
            rowBackgroundEven = DarkRowBg,
            rowBackgroundOdd = DarkerRowBg
        )
    } else {
        ExtendedColors(
            navBarColor = MediumTeal,
            tagBackground = LightAccent,
            rowBackgroundEven = LightGray,
            rowBackgroundOdd = White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension to easily access extended colors
object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}