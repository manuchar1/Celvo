package com.mtislab.core.designsystem.components.switchers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val selectedTextColor = MaterialTheme.colorScheme.onPrimary
    val unselectedTextColor = MaterialTheme.colorScheme.extended.textSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(0.5.dp, borderColor, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex

            TabButton(
                text = text,
                isSelected = isSelected,
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
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "TabBackground"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedTextColor else unselectedTextColor,
        animationSpec = tween(durationMillis = 200),
        label = "TabText"
    )

    val shadowModifier = if (isSelected) {
        Modifier.shadow(2.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(shadowModifier)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(interactionSource = null, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        )
    }
}