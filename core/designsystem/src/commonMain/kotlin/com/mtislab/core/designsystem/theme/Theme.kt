package com.mtislab.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @ReadOnlyComposable
    @Composable
    get() = LocalExtendedColors.current

@Immutable
data class ExtendedColors(
    // Button states
    val primaryHover: Color,
    val destructiveHover: Color,
    val destructiveSecondaryOutline: Color,
    val disabledOutline: Color,
    val disabledFill: Color,
    val successOutline: Color,
    val success: Color,
    val onSuccess: Color,
    val secondaryFill: Color,

    // Text variants
    val textPrimary: Color,
    val textTertiary: Color,
    val textSecondary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,

    // Surface variants
    val surfaceLower: Color,
    val surfaceHigher: Color,
    val surfaceOutline: Color,
    val overlay: Color,

    // Accent colors
    val accentBlue: Color,
    val accentPurple: Color,
    val accentViolet: Color,
    val accentPink: Color,
    val accentOrange: Color,
    val accentYellow: Color,
    val accentGreen: Color,
    val accentTeal: Color,
    val accentLightBlue: Color,
    val accentGrey: Color,

    // Cake colors for chat bubbles
    val cakeViolet: Color,
    val cakeGreen: Color,
    val cakeBlue: Color,
    val cakePink: Color,
    val cakeOrange: Color,
    val cakeYellow: Color,
    val cakeTeal: Color,
    val cakePurple: Color,
    val cakeRed: Color,
    val cakeMint: Color,
)

val LightExtendedColors = ExtendedColors(
    primaryHover = CelvoBrand700,
    destructiveHover = ChirpRed600,
    destructiveSecondaryOutline = ChirpRed200,
    disabledOutline = ChirpBase200,
    disabledFill = ChirpBase150,
    successOutline = ChirpBrand100,
    success = CelvoBrand700,
    onSuccess = ChirpBase0,
    secondaryFill = ChirpBase100,

    textPrimary = CelvoBase1000,
    textTertiary = ChirpBase800,
    textSecondary = ChirpBase900,
    textPlaceholder = ChirpBase700,
    textDisabled = ChirpBase400,

    surfaceLower = ChirpBase100,
    surfaceHigher = ChirpBase100,
    surfaceOutline = ChirpBase1000Alpha14,
    overlay = ChirpBase1000Alpha80,

    accentBlue = ChirpBlue,
    accentPurple = ChirpPurple,
    accentViolet = ChirpViolet,
    accentPink = ChirpPink,
    accentOrange = ChirpOrange,
    accentYellow = ChirpYellow,
    accentGreen = ChirpGreen,
    accentTeal = ChirpTeal,
    accentLightBlue = ChirpLightBlue,
    accentGrey = ChirpGrey,

    cakeViolet = ChirpCakeLightViolet,
    cakeGreen = ChirpCakeLightGreen,
    cakeBlue = ChirpCakeLightBlue,
    cakePink = ChirpCakeLightPink,
    cakeOrange = ChirpCakeLightOrange,
    cakeYellow = ChirpCakeLightYellow,
    cakeTeal = ChirpCakeLightTeal,
    cakePurple = ChirpCakeLightPurple,
    cakeRed = ChirpCakeLightRed,
    cakeMint = ChirpCakeLightMint,
)

val DarkExtendedColors = ExtendedColors(
    primaryHover = CelvoBrand700,
    destructiveHover = ChirpRed600,
    destructiveSecondaryOutline = ChirpRed200,
    disabledOutline = ChirpBase900,
    disabledFill = CelvoBase1000,
    successOutline = ChirpBrand500Alpha40,
    success = CelvoBrand500,
    onSuccess = CelvoBase1000,
    secondaryFill = ChirpBase900,

    textPrimary = ChirpBase0,
    textTertiary = ChirpBase200,
    textSecondary = ChirpBase150,
    textPlaceholder = ChirpBase400,
    textDisabled = ChirpBase500,

    surfaceLower = CelvoBase1000,
    surfaceHigher = ChirpBase900,
    surfaceOutline = ChirpBase100Alpha10Alt,
    overlay = ChirpBase1000Alpha80,

    accentBlue = ChirpBlue,
    accentPurple = ChirpPurple,
    accentViolet = ChirpViolet,
    accentPink = ChirpPink,
    accentOrange = ChirpOrange,
    accentYellow = ChirpYellow,
    accentGreen = ChirpGreen,
    accentTeal = ChirpTeal,
    accentLightBlue = ChirpLightBlue,
    accentGrey = ChirpGrey,

    cakeViolet = ChirpCakeDarkViolet,
    cakeGreen = ChirpCakeDarkGreen,
    cakeBlue = ChirpCakeDarkBlue,
    cakePink = ChirpCakeDarkPink,
    cakeOrange = ChirpCakeDarkOrange,
    cakeYellow = ChirpCakeDarkYellow,
    cakeTeal = ChirpCakeDarkTeal,
    cakePurple = ChirpCakeDarkPurple,
    cakeRed = ChirpCakeDarkRed,
    cakeMint = ChirpCakeDarkMint,
)

val LightColorScheme = lightColorScheme(
    primary = CelvoBrand500,
    onPrimary = CelvoBrand1000,
    primaryContainer = ChirpBrand100,
    onPrimaryContainer = CelvoBrand900,

    secondary = ChirpBase700,
    onSecondary = ChirpBase0,
    secondaryContainer = ChirpBase100,
    onSecondaryContainer = ChirpBase900,

    tertiary = CelvoBrand900,
    onTertiary = ChirpBase0,
    tertiaryContainer = ChirpBrand100,
    onTertiaryContainer = CelvoBrand1000,

    error = ChirpRed500,
    onError = ChirpBase0,
    errorContainer = ChirpRed200,
    onErrorContainer = ChirpRed600,

    background = CelvoBrand1000,
    onBackground = ChirpBase0,
    surface = ChirpBase0,
    onSurface = CelvoBase1000,
    surfaceVariant = ChirpBase100,
    onSurfaceVariant = ChirpBase900,

    outline = ChirpBase1000Alpha8,
    outlineVariant = ChirpBase200,
)

val DarkColorScheme = darkColorScheme(
    primary = CelvoBrand500,
    onPrimary = CelvoBrand1000,
    primaryContainer = CelvoBrand900,
    onPrimaryContainer = CelvoBrand500,

    secondary = ChirpBase400,
    onSecondary = CelvoBase1000,
    secondaryContainer = ChirpBase900,
    onSecondaryContainer = ChirpBase150,

    tertiary = CelvoBrand500,
    onTertiary = CelvoBase1000,
    tertiaryContainer = CelvoBrand900,
    onTertiaryContainer = CelvoBrand500,

    error = ChirpRed500,
    onError = ChirpBase0,
    errorContainer = ChirpRed600,
    onErrorContainer = ChirpRed200,

    background = CelvoBase1000,
    onBackground = ChirpBase0,
    surface = ChirpBase950,
    onSurface = ChirpBase0,
    surfaceVariant = ChirpBase900,
    onSurfaceVariant = ChirpBase150,

    outline = ChirpBase100Alpha10,
    outlineVariant = ChirpBase800,
)