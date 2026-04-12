package com.mtislab.core.designsystem.components.indicators

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mtislab.core.designsystem.theme.extended
import kotlin.math.roundToInt

/**
 * eSIM data usage gauge — segmented arc design.
 *
 * The arc is split into [segmentCount] discrete segments separated by small gaps.
 * Filled segments use [primaryColor]; unfilled segments use [trackColor].
 *
 * @param usedAmount   Data consumed (e.g. 15).
 * @param totalAmount  Total data allowance (e.g. 20).
 * @param unit         Display unit label (e.g. "GB").
 * @param flagUrl      Country flag image URL (Coil 3).
 * @param segmentCount Number of arc segments (default 10).
 * @param gapAngleDeg  Angle in degrees of each gap between segments.
 * @param sweepAngleDeg Total arc sweep in degrees (centered at the top, gap at the bottom).
 * @param strokeWidth  Thickness of the arc stroke.
 * @param primaryColor Color for filled (active) segments.
 * @param trackColor   Color for unfilled (background) segments.
 * @param animationDuration Duration of the fill animation in milliseconds.
 */
@Composable
fun CelvoUsageGauge(
    usedAmount: Float,
    totalAmount: Float,
    unit: String,
    flagUrl: String?,
    modifier: Modifier = Modifier,
    segmentCount: Int = 5,
    gapAngleDeg: Float = 2f,
    sweepAngleDeg: Float = 180f,
    strokeWidth: Dp = 35.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animationDuration: Int = 1200
) {
    // ── Progress calculation (NaN / Infinity-safe) ──
    val targetProgress = if (totalAmount > 0f) {
        (usedAmount / totalAmount).coerceIn(0f, 1f)
    } else 0f

    // ── Animation (0 → targetProgress) ──
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    // ── Segment geometry (pre-calculated, stable across recompositions) ──
    val startAngle = remember(sweepAngleDeg) {
        // Center the gap at the bottom (6-o'clock = 90°).
        // Arc starts at 90° + halfGap, sweeps clockwise back to 90° − halfGap.
        90f + (360f - sweepAngleDeg) / 2f
    }

    Box(

        modifier = modifier
            .aspectRatio(2f)
            .padding(strokeWidth / 2),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = Size(size.width, size.width)
            val strokePx = strokeWidth.toPx()

            val totalGapsAngle = (segmentCount - 1) * gapAngleDeg
            val segmentSweep = (sweepAngleDeg - totalGapsAngle) / segmentCount

            // Continuous fill angle (not quantized to segments)
            val fillAngle = animatable.value * sweepAngleDeg

            for (i in 0 until segmentCount) {
                val segStart = startAngle + i * (segmentSweep + gapAngleDeg)
                val segEnd = segStart + segmentSweep

                // How far into the total sweep does this segment start/end?
                val segStartInSweep = i * (segmentSweep + gapAngleDeg)
                val segEndInSweep = segStartInSweep + segmentSweep

                when {
                    // Fully filled segment
                    fillAngle >= segEndInSweep -> {
                        drawArc(
                            color = primaryColor,
                            startAngle = segStart,
                            sweepAngle = segmentSweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = arcSize,
                        )
                    }
                    // Partially filled segment (the boundary)
                    fillAngle > segStartInSweep -> {
                        // Draw track (background) for full segment
                        drawArc(
                            color = trackColor,
                            startAngle = segStart,
                            sweepAngle = segmentSweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = arcSize,
                        )
                        // Overlay primary for the filled portion
                        val filledPortion = fillAngle - segStartInSweep
                        drawArc(
                            color = primaryColor,
                            startAngle = segStart,
                            sweepAngle = filledPortion,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = arcSize,
                        )
                    }
                    // Fully unfilled segment
                    else -> {
                        drawArc(
                            color = trackColor,
                            startAngle = segStart,
                            sweepAngle = segmentSweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = arcSize,
                        )
                    }
                }
            }
        }

        // ── Center content: flag + data labels ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 26.dp)
        ) {
            if (!flagUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = flagUrl,
                    contentDescription = "Region Flag",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = "${usedAmount.formatDataAmount()} $unit",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.extended.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary label — e.g. "/ 20 GB"
            Text(
                text = "/ ${totalAmount.formatDataAmount()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
        }
    }
}


fun Float.formatDataAmount(): String {
    if (this.isNaN() || this.isInfinite()) return "0"

    return if (this % 1f == 0f) {
        this.toInt().toString()
    } else {
        val rounded = (this * 10f).roundToInt() / 10f
        rounded.toString()
    }
}