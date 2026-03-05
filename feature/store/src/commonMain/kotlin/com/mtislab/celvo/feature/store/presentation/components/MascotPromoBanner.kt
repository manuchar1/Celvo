package com.mtislab.celvo.feature.store.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import kotlinx.coroutines.delay

@Composable
fun MascotPromoBanner(
    banner: MarketingBanner,
    isClaimed: Boolean,
    onClaimClicked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // 1. Interaction State (For the "Squish" effect when pressed)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bannerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bannerScale"
    )

    // 2. Color Transition (Transitions to a Success Green when claimed)
    val currentBgColor by animateColorAsState(
        targetValue = if (isClaimed) Color(0xFFE8F5E9) else banner.backgroundColor,
        animationSpec = tween(durationMillis = 400),
        label = "bgColor"
    )
    val currentTextColor = if (isClaimed) Color(0xFF2E7D32) else banner.textColor

    // 3. Mascot Animation Triggers (The "Jump & Wiggle")
    var triggerMascotAnim by remember { mutableStateOf(false) }

    // Fire the mascot animation only when it shifts to the claimed state
    LaunchedEffect(isClaimed) {
        if (isClaimed) {
            triggerMascotAnim = true
            delay(500) // Let the jump finish before resetting
            triggerMascotAnim = false
        }
    }

    val mascotOffsetY by animateDpAsState(
        targetValue = if (triggerMascotAnim) (-24).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "mascotY"
    )

    val mascotRotation by animateFloatAsState(
        targetValue = if (triggerMascotAnim) 15f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mascotRot"
    )

    // Layout
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp) // Critical: Leave room for the mascot to pop out!
            .graphicsLayer {
                scaleX = bannerScale
                scaleY = bannerScale
            }
    ) {
        // Main Banner Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(currentBgColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Disable default ripple so the scale animation shines
                    enabled = !isClaimed
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClaimClicked()
                }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isClaimed) (banner.claimedTitle ?: "✅ Claimed!") else banner.title,
                    color = currentTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val descText = if (isClaimed) banner.claimedDescription else banner.description
                if (!descText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = descText,
                        color = currentTextColor.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            // Spacer to ensure text doesn't overlap the floating mascot
            Spacer(modifier = Modifier.width(60.dp))
        }

        // The Floating Mascot (Loaded via Coil3)
        Image(
            painter = rememberAsyncImagePainter(model = banner.imageUrl),
            contentDescription = "Celvo Mascot",
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                // Hangs halfway out of the card to create depth
                .offset(x = (-8).dp, y = (-36).dp + mascotOffsetY)
                .graphicsLayer {
                    rotationZ = mascotRotation
                }
        )
    }
}