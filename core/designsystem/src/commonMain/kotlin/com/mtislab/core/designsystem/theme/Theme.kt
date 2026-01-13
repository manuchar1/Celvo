package com.mtislab.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


val DarkColorScheme = darkColorScheme(
    primary = CelvoPurple500,
    onPrimary = CelvoWhite,
    primaryContainer = CelvoPurple900,
    onPrimaryContainer = CelvoPurple300,
    secondary = CelvoGreen500,
    onSecondary = CelvoDark900,
    background = CelvoDark900,
    onBackground = CelvoWhite,
    surface = CelvoDark900,
    onSurface = CelvoWhite,

    // Surface Variant - Figma: Dark800 (#272930) - ქარდები და ინფუთები
    surfaceVariant = CelvoDark800,
    onSurfaceVariant = CelvoDark300, // მონაცრისფრო ტექსტი

    error = CelvoRose500,
    onError = CelvoDark900,

    outline = CelvoDark600
)

val LightColorScheme = lightColorScheme(
    primary = CelvoPurple700,
    onPrimary = CelvoWhite,
    primaryContainer = CelvoPurpleTint, // ტაბების უკანა ფონი
    onPrimaryContainer = CelvoPurple1000,

    secondary = CelvoGreen700,
    onSecondary = CelvoWhite,

    background = CelvoWhite,
    onBackground = CelvoLight900, // მუქი ტექსტი (#515354)

    surface = CelvoWhite,
    onSurface = CelvoLight900,

    // Light Inputs/Search bar background (#F5F7F8)
    surfaceVariant = CelvoLightSurface,
    onSurfaceVariant = CelvoLight700,

    error = CelvoRose700,
    onError = CelvoWhite,

    outline = CelvoLight300
)

// --- Extended Colors (Custom System) ---
// აქ დავტოვე მხოლოდ ის, რაც რეალურად გვჭირდება და გავასუფთავე Chirp-ის ნაგავი.

@Immutable
data class ExtendedColors(
    // Semantic Colors
    val success: Color,
    val warning: Color, // Discount badge
    val destructive: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnColored: Color,
    // Containers
    val cardBackground: Color,
    val cardBorder: Color,
    val cardShadow: Color,
    val inputBackground: Color,
    val divider: Color,
    val gradientStart: Color,


    )

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @ReadOnlyComposable
    @Composable
    get() = LocalExtendedColors.current

val DarkExtendedColors = ExtendedColors(
    success = CelvoGreen500,
    warning = CelvoYellow,
    destructive = CelvoRose500,

    textPrimary = CelvoWhite,
    textSecondary = CelvoLight300,

    textTertiary = CelvoDark500,
    textOnColored = CelvoDark900,

    cardBackground = CelvoGlassWhite,
    cardBorder = CelvoBorderTransparent,
    cardShadow = Color.Transparent,
    inputBackground = CelvoDark800,
    divider = CelvoDark700,
    gradientStart = CelvoGradientBase.copy(alpha = 0.2f)
)

val LightExtendedColors = ExtendedColors(
    success = CelvoGreen700,
    warning = CelvoYellow,
    destructive = CelvoRose700,

    textPrimary = CelvoDark900,
    textSecondary = CelvoLight500,

    textTertiary = CelvoLight400,
    textOnColored = CelvoLight900,

    cardBackground = CelvoWhite,
    cardBorder = CelvoLight300,
    cardShadow = CelvoShadow,
    inputBackground = CelvoLightSurface,
    divider = CelvoLight300,
    gradientStart = CelvoGradientBase.copy(alpha = 0.2f)
)