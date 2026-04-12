package com.mtislab.core.designsystem.components.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CelvoGlow(
    color: Color,
    modifier: Modifier = Modifier,
    radius: Dp = 160.dp,
    alpha: Float = 0.9f
) {
    Box(
        modifier = modifier
            .size(radius)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}