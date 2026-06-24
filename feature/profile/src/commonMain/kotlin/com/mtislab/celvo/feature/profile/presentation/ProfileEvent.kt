package com.mtislab.celvo.feature.profile.presentation

import com.mtislab.core.designsystem.components.notifications.CelvoNotificationType
import org.jetbrains.compose.resources.StringResource

sealed interface ProfileEvent {
    data class ShowNotification(
        val messageRes: StringResource,
        val type: CelvoNotificationType
    ) : ProfileEvent
}
