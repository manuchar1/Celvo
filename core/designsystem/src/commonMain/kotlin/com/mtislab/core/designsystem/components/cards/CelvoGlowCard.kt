package com.mtislab.core.designsystem.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.components.effects.CelvoGlow

@Composable
fun CelvoGlowCard(
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    glowAlignment: Alignment = Alignment.TopEnd,
    glowOffsetX: Dp = 0.dp,
    glowOffsetY: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    CelvoCard(
        modifier = modifier,
        onClick = onClick,
        border = border,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            CelvoGlow(
                color = glowColor,
                modifier = Modifier
                    .align(glowAlignment)
                    .offset(x = glowOffsetX, y = glowOffsetY)
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}