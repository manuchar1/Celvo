package com.mtislab.core.designsystem.components.indicators

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtislab.core.designsystem.theme.extended
import kotlin.math.roundToInt

/**
 * eSIM data usage gauge — segmented arc design.
 *
 * Two visual modes share the same shape so the home gauge keeps a single
 * silhouette:
 *
 *  - **Metered**: animated fill 0..1, primary color, primary label is the
 *    remaining-bytes string, secondary label is "/ total".
 *  - **Unlimited** (`isUnlimited = true`): the arc is always full, painted
 *    with a sweep gradient through `primary → secondary → primary`. The
 *    primary label is rendered as a large `∞` glyph that pulses gently to
 *    telegraph "never runs out". The secondary label reads "Unlimited".
 *
 * Both modes draw exclusively from `MaterialTheme.colorScheme.*` and the
 * project's `extended` tokens — no hex literals, so light/dark Just Works.
 *
 * @param progress       Fill ratio 0f..1f. Ignored when [isUnlimited] is true.
 * @param primaryText    Primary label inside the arc.
 * @param secondaryText  Secondary label beneath the primary.
 * @param isUnlimited    When true, switches to the unlimited treatment.
 * @param flag           Optional flag composable rendered above the labels.
 * @param bottomContent  Optional content below the secondary label.
 */
@Composable
fun CelvoUsageGauge(
    progress: Float,
    primaryText: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    isUnlimited: Boolean = false,
    flag: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    segmentCount: Int = 5,
    gapAngleDeg: Float = 2f,
    sweepAngleDeg: Float = 180f,
    strokeWidth: Dp = 35.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animationDuration: Int = 1200,
) {
    val targetProgress = if (isUnlimited) 1f else progress.coerceIn(0f, 1f)

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

    val startAngle = remember(sweepAngleDeg) {
        90f + (360f - sweepAngleDeg) / 2f
    }

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val unlimitedBrush = remember(primaryColor, secondaryColor) {
        Brush.sweepGradient(
            listOf(primaryColor, secondaryColor, primaryColor)
        )
    }

    // Slow opacity pulse on the ∞ glyph. Telegraphs "never runs out" without
    // drawing attention away from the rest of the card.
    val infinityAlpha: Float = if (isUnlimited) {
        val transition = rememberInfiniteTransition(label = "unlimited-pulse")
        transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "unlimited-pulse-alpha"
        ).value
    } else 1f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .padding(strokeWidth / 2)
        ) {
            val arcSize = Size(size.width, size.width)
            val strokePx = strokeWidth.toPx()

            val totalGapsAngle = (segmentCount - 1) * gapAngleDeg
            val segmentSweep = (sweepAngleDeg - totalGapsAngle) / segmentCount

            val fillAngle = animatable.value * sweepAngleDeg

            for (i in 0 until segmentCount) {
                val segStart = startAngle + i * (segmentSweep + gapAngleDeg)
                val segStartInSweep = i * (segmentSweep + gapAngleDeg)
                val segEndInSweep = segStartInSweep + segmentSweep

                when {
                    fillAngle >= segEndInSweep -> {
                        if (isUnlimited) {
                            drawArc(
                                brush = unlimitedBrush,
                                startAngle = segStart,
                                sweepAngle = segmentSweep,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                                topLeft = Offset.Zero,
                                size = arcSize,
                            )
                        } else {
                            drawArc(
                                color = primaryColor,
                                startAngle = segStart,
                                sweepAngle = segmentSweep,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                                size = arcSize,
                            )
                        }
                    }

                    fillAngle > segStartInSweep -> {
                        drawArc(
                            color = trackColor,
                            startAngle = segStart,
                            sweepAngle = segmentSweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                            size = arcSize,
                        )
                        val filledPortion = fillAngle - segStartInSweep
                        if (isUnlimited) {
                            drawArc(
                                brush = unlimitedBrush,
                                startAngle = segStart,
                                sweepAngle = filledPortion,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                                topLeft = Offset.Zero,
                                size = arcSize,
                            )
                        } else {
                            drawArc(
                                color = primaryColor,
                                startAngle = segStart,
                                sweepAngle = filledPortion,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                                size = arcSize,
                            )
                        }
                    }

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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Offsets the content below the arc band so the flag keeps a small,
            // device-independent gap from the gauge instead of touching it.
            modifier = Modifier.padding(top = 44.dp)
        ) {
            if (flag != null) {
                flag()
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (isUnlimited) {
                // titleLarge font (PlusJakartaSans) with a deliberately larger size
                // so the ∞ glyph dominates the centre of the arc without claiming a
                // dedicated typography token.
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 48.sp),
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                    modifier = Modifier.alpha(infinityAlpha)
                )
            } else {
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )

            if (bottomContent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                bottomContent()
            }
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
