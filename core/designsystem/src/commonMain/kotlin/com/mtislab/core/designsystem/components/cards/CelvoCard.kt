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

// Shape მაინც შეგვიძლია აქ დავტოვოთ ან Theme-ის Shapes.kt-ში გავიტანოთ.
// ამ ეტაპზე აქ დატოვება მისაღებია, რადგან ეს მხოლოდ ამ ქარდს ეხება.
private val CardShape = RoundedCornerShape(26.dp)

// commonMain/.../components/cards/CelvoCard.kt
@Composable
fun CelvoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = CardShape, // CardShape ზემოთ განსაზღვრული (26.dp)
    enabled: Boolean = true,
    // 👇 ახალი პარამეტრი: თუ null-ია, გამოიყენებს default-ს, თუ არა - შენს მოწოდებულს
    border: BorderStroke? = null,
    // 👇 ახალი პარამეტრი: Padding-ის მართვა გარედან (default 16.dp)
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme.extended

    // Default Border ლოგიკა (თუ გარედან არ მოგვაწოდეს)
    val finalBorder = border ?: BorderStroke(0.5.dp, colors.cardBorder)

    val shadowModifier = if (colors.cardShadow != Color.Transparent) {
        Modifier.shadow(
            elevation = 4.dp,
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
        color = colors.cardBackground,
        border = finalBorder, // ვიყენებთ დინამიურ ბორდერს
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        // Padding-ს ვიღებთ პარამეტრიდან
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}