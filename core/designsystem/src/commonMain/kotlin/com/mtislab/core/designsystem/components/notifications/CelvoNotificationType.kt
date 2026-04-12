package com.mtislab.core.designsystem.components.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.mtislab.core.designsystem.theme.LocalExtendedColors

/**
 * Defines the semantic type of a [CelvoTopNotification].
 *
 * Each type provides its own [glowColor] and [iconTint] that are resolved
 * at composition time from [ExtendedColors], ensuring correct light/dark theming.
 */
@Immutable
sealed interface CelvoNotificationType {

    /** Resolved glow color for the banner card. */
    @Composable
    fun glowColor(): Color

    /** Resolved tint for the leading icon background. */
    @Composable
    fun iconTint(): Color

    data object Success : CelvoNotificationType {
        @Composable
        override fun glowColor(): Color = notificationColors().success

        @Composable
        override fun iconTint(): Color = notificationColors().success
    }

    data object Error : CelvoNotificationType {
        @Composable
        override fun glowColor(): Color = notificationColors().destructive

        @Composable
        override fun iconTint(): Color = notificationColors().destructive
    }

    data object Warning : CelvoNotificationType {
        @Composable
        override fun glowColor(): Color = notificationColors().warning

        @Composable
        override fun iconTint(): Color = notificationColors().warning
    }

    data object Info : CelvoNotificationType {
        @Composable
        override fun glowColor(): Color = notificationColors().primary

        @Composable
        override fun iconTint(): Color = notificationColors().primary
    }

    /**
     * Escape hatch for one-off banners that don't fit a semantic category.
     * Prefer the named types above for consistency.
     */
    data class Custom(private val color: Color) : CelvoNotificationType {
        @Composable
        override fun glowColor(): Color = color

        @Composable
        override fun iconTint(): Color = color
    }
}



@Immutable
internal data class ResolvedNotificationColors(
    val success: Color,
    val destructive: Color,
    val warning: Color,
    val primary: Color,
)

@Composable
internal fun notificationColors(): ResolvedNotificationColors {
    val extended = androidx.compose.material3.MaterialTheme.colorScheme
        .let { LocalExtendedColors.current }
    val primary = androidx.compose.material3.MaterialTheme.colorScheme.primary

    return ResolvedNotificationColors(
        success = extended.success,
        destructive = extended.destructive,
        warning = extended.warning,
        primary = primary,
    )
}