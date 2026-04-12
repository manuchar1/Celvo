package com.mtislab.core.designsystem.components.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.components.cards.CelvoGlowCard
import com.mtislab.core.designsystem.theme.extended
import kotlinx.coroutines.delay


/**
 * Global notification host that observes [CelvoNotificationState]
 * and renders an animated, auto-dismissing banner from the top edge.
 */
@Composable
fun CelvoNotificationHost(
    state: CelvoNotificationState,
    modifier: Modifier = Modifier,
) {
    val data = state.currentData

    // Key on `data?.id` so the LaunchedEffect restarts for every
    // unique notification, even if the message text is identical.
    LaunchedEffect(data?.id) {
        if (data != null) {
            delay(data.durationMillis)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = data != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 28.dp)
    ) {
        // Capture a non-null snapshot for the exit animation frame.
        // During exit, `data` may already be null, so we keep the
        // last-displayed value alive until the animation completes.
        val displayData = data ?: return@AnimatedVisibility

        NotificationBanner(
            data = displayData,
            onDismiss = { state.dismissImmediate() },
        )
    }
}

// ────────────────────────────────────────────────────────────
// Internal banner composable
// ────────────────────────────────────────────────────────────

@Composable
private fun NotificationBanner(
    data: CelvoNotificationData,
    onDismiss: () -> Unit,
) {
    val glowColor = data.type.glowColor()
    val iconTint = data.type.iconTint()
    val extended = MaterialTheme.colorScheme.extended

    CelvoGlowCard(
        glowColor = glowColor,
        glowOffsetX = 48.dp,
        glowOffsetY = (-10).dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
                dismiss { onDismiss(); true }
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Leading icon circle ──
            NotificationIcon(
                type = data.type,
                tint = iconTint,
            )

            // ── Text content ──
            Column(
                modifier = Modifier.weight(1f),
                //verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = data.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textPrimary,
                    maxLines = 2,
                )
                if (data.description != null) {
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary,
                        maxLines = 2,
                    )
                }
            }

            // ── Dismiss action ──
/*            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = extended.textSecondary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "დახურვა",
                    modifier = Modifier.size(18.dp),
                )
            }*/
        }
    }
}



@Composable
private fun NotificationIcon(
    type: CelvoNotificationType,
    tint: Color,
) {
    val icon: ImageVector = when (type) {
        is CelvoNotificationType.Success -> Icons.Rounded.Check
        is CelvoNotificationType.Error -> Icons.Rounded.Close
        is CelvoNotificationType.Warning -> Icons.Rounded.Warning
        is CelvoNotificationType.Info -> Icons.Rounded.Info
        is CelvoNotificationType.Custom -> Icons.Rounded.Info
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
    }
}