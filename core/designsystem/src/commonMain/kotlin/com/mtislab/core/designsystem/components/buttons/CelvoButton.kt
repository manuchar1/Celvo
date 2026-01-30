package com.mtislab.core.designsystem.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended

@Composable
fun CelvoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.extended.inputBackground,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {

    val finalContentColor = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant

    val finalBorder = if (borderColor != null) {
        BorderStroke(0.5.dp, borderColor)
    } else {
        BorderStroke(0.dp, Color.Transparent)
    }

    CelvoCard(
        onClick = if (enabled && !isLoading) onClick else null,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(48.dp),
        border = finalBorder,
        enabled = enabled,
        containerColor = containerColor,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null && !isLoading) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (isLoading) {
                Text(
                    text = "იტვირთება...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = finalContentColor,
                    fontFamily = PlusJakartaSans
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PlusJakartaSans
                    ),
                    color = finalContentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}