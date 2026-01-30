package com.mtislab.core.designsystem.components.switchers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.theme.extended

@Composable
fun CelvoTabSwitcher(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.extended.inputBackground
    val borderColor = MaterialTheme.colorScheme.outline

    val selectedTabColor = MaterialTheme.colorScheme.primary
    val selectedTextColor = MaterialTheme.colorScheme.onSecondary
    val unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(0.5.dp, borderColor, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEachIndexed { index, text ->
            TabButton(
                text = text,
                isSelected = index == selectedIndex,
                selectedColor = selectedTabColor,
                selectedTextColor = selectedTextColor,
                unselectedTextColor = unselectedTextColor,
                modifier = Modifier.weight(1f),
                onClick = { onOptionSelected(index) }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val animationSpec = spring<Color>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = animationSpec,
        label = "TabBackground"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedTextColor else unselectedTextColor,
        animationSpec = animationSpec,
        label = "TabText"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}