package com.mtislab.core.designsystem.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.theme.extended


private val CardShape = RoundedCornerShape(26.dp)


@Composable
fun CelvoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = CardShape,
    enabled: Boolean = true,
    border: BorderStroke? = null,
    containerColor: Color = MaterialTheme.colorScheme.extended.cardBackground,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme.extended

    val finalBorder = border ?: BorderStroke(0.5.dp, colors.cardBorder)

    val shadowModifier = if (colors.cardShadow != Color.Transparent) {
        Modifier.shadow(
            elevation = 2.dp,
            shape = shape,
            spotColor = colors.cardShadow,
            ambientColor = colors.cardShadow,
            clip = false
        )
    } else {
        Modifier
    }

    Surface(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        modifier = modifier
            .fillMaxWidth()
            .then(shadowModifier),
        shape = shape,
        color = containerColor,
        border = finalBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}