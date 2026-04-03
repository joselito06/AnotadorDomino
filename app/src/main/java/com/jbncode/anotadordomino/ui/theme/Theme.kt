package com.jbncode.anotadordomino.ui.theme

import android.app.Activity
import android.os.Build
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

private val DarkColorScheme = darkColorScheme(
    // Brand
    primary          = NeonGreen,
    onPrimary        = Dark_Background,
    primaryContainer = NeonGreenDark,
    onPrimaryContainer = Dark_Background,

    secondary        = CyanAccent,
    onSecondary      = Dark_Background,
    secondaryContainer = CyanAccentDark,
    onSecondaryContainer = Dark_Background,

    tertiary         = PurpleAccent,
    onTertiary       = Color.White,
    tertiaryContainer = PurpleAccent.copy(alpha = 0.25f),
    onTertiaryContainer = PurpleAccent,

    // Surfaces
    background       = Dark_Background,
    onBackground     = Dark_OnBackground,

    surface          = Dark_Surface,
    onSurface        = Dark_OnSurface,

    surfaceVariant   = Dark_SurfaceVariant,
    onSurfaceVariant = Dark_OnSurfaceVariant,

    surfaceContainer        = Dark_SurfaceContainer,
    surfaceContainerHigh    = Dark_SurfaceVariant,
    surfaceContainerHighest = Dark_Outline,

    // Outline & dividers
    outline        = Dark_Outline,
    outlineVariant = Dark_Outline.copy(alpha = 0.5f),

    // Error
    error          = ErrorRed,
    onError        = Color.White,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color.White,

    // Inverse (for snackbars, tooltips)
    inverseSurface    = Dark_OnSurface,
    inverseOnSurface  = Dark_Surface,
    inversePrimary    = NeonGreenDark,

    scrim = Color.Black.copy(alpha = 0.7f),
)

private val LightColorScheme = lightColorScheme(
    // Brand — same accent colors, lighter surfaces
    primary          = NeonGreenDark,       // slightly darker so it has contrast on white
    onPrimary        = Color.White,
    primaryContainer = NeonGreen.copy(alpha = 0.18f),
    onPrimaryContainer = NeonGreenDark,

    secondary        = CyanAccentDark,
    onSecondary      = Color.White,
    secondaryContainer = CyanAccent.copy(alpha = 0.18f),
    onSecondaryContainer = CyanAccentDark,

    tertiary         = PurpleAccent,
    onTertiary       = Color.White,
    tertiaryContainer = PurpleAccent.copy(alpha = 0.12f),
    onTertiaryContainer = PurpleAccent,

    // Surfaces
    background       = Light_Background,
    onBackground     = Light_OnBackground,

    surface          = Light_Surface,
    onSurface        = Light_OnSurface,

    surfaceVariant   = Light_SurfaceVariant,
    onSurfaceVariant = Light_OnSurfaceVariant,

    surfaceContainer        = Light_SurfaceContainer,
    surfaceContainerHigh    = Light_SurfaceVariant,
    surfaceContainerHighest = Light_Outline,

    // Outline & dividers
    outline        = Light_Outline,
    outlineVariant = Light_Outline.copy(alpha = 0.5f),

    // Error
    error          = ErrorRed,
    onError        = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.12f),
    onErrorContainer = ErrorRedDark,

    // Inverse
    inverseSurface   = Light_OnSurface,
    inverseOnSurface = Light_Surface,
    inversePrimary   = NeonGreen,

    scrim = Color.Black.copy(alpha = 0.4f),
)

@Composable
fun AnotadorDominoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    val kineticColors = if (darkTheme) DarkKineticColors else LightKineticColors

    // Tint the system status bar to match the theme background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalKineticColors provides kineticColors){
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KineticTypography,
            content = content
        )
    }

}

data class KineticColors(
    val neonGreen: Color,
    val neonGreenDark: Color,
    val cyanAccent: Color,
    val cyanAccentDark: Color,
    val purpleAccent: Color,
    /** Semi-transparent surface used for player cards, dialogs, etc. */
    val surfaceDark: Color,
    val dialogBackground: Color,
)

private val DarkKineticColors = KineticColors(
    neonGreen       = NeonGreen,
    neonGreenDark   = NeonGreenDark,
    cyanAccent      = CyanAccent,
    cyanAccentDark  = CyanAccentDark,
    purpleAccent    = PurpleAccent,
    surfaceDark     = Dark_SurfaceVariant,
    dialogBackground= Dark_Surface,
)

private val LightKineticColors = KineticColors(
    neonGreen       = NeonGreenDark,
    neonGreenDark   = NeonGreenDark,
    cyanAccent      = CyanAccentDark,
    cyanAccentDark  = CyanAccentDark,
    purpleAccent    = PurpleAccent,
    surfaceDark     = Light_SurfaceContainer,
    dialogBackground= Light_Surface,
)

val LocalKineticColors = staticCompositionLocalOf { DarkKineticColors }

/** Shortcut: MaterialTheme.kineticColors.neonGreen */
val MaterialTheme.kineticColors: KineticColors
    @Composable get() = LocalKineticColors.current