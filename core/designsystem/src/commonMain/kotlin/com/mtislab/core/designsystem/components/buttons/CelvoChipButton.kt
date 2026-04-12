package com.mtislab.core.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun CelvoChipButton(
    iconRes: DrawableResource,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.extended.cardBackground
    val contentColor = MaterialTheme.colorScheme.extended.textPrimary

    CelvoCard(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = CircleShape,
        containerColor = backgroundColor,
        border = null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    // fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = contentColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp),
                tint = null
            )
        }
    }
}