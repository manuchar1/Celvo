package com.mtislab.core.designsystem.components.indicators

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtislab.core.designsystem.theme.CelvoPurple300 // ან რაც გაქვს თემაში
import com.mtislab.core.designsystem.theme.extended

@Composable
fun CelvoUsageGauge(
    usedAmount: Double,
    totalAmount: Double,
    unit: String = "GB",
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant, // ფერმკრთალი უკანა ფონი
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp
) {
    // ანიმაცია პროგრესისთვის
    val percentage = (usedAmount / totalAmount).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f) // კვადრატული ზომა რომ შეინარჩუნოს
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val componentSize = size.minDimension
            val arcSize = Size(componentSize, componentSize)
            val topLeftOffset = (size.width - componentSize) / 2 // ცენტრირება

            // 1. Background Track (ნაცრისფერი რკალი)
            drawArc(
                color = trackColor,
                startAngle = 135f, // იწყება მარცხენა ქვედა კუთხიდან
                sweepAngle = 270f, // გრძელდება 270 გრადუსი (ტოვებს ღია ძირს)
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, (size.height - componentSize) / 2)
            )

            // 2. Progress Indicator (ფერადი რკალი)
            drawArc(
                color = primaryColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = arcSize,
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, (size.height - componentSize) / 2)
            )
        }

        // 3. ტექსტი ცენტრში
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$usedAmount $unit",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp // ზომა შეგვიძლია პარამეტრად გავიტანოთ თუ დაგვჭირდა
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "/ $totalAmount $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
        }
    }
}