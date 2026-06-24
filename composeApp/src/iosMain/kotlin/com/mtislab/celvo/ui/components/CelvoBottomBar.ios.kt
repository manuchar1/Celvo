package com.mtislab.celvo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mtislab.celvo.navigation.TopLevelRoute
import com.mtislab.core.designsystem.theme.extended
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIVisualEffectView

/** Fully-rounded "pill" geometry shared by the bar shell and the selection lozenge. */
private val CapsuleShape = RoundedCornerShape(percent = 50)

private const val CapsuleHeightDp = 64.0

/**
 * iOS bottom navigation — Apple "Liquid Glass" treatment.
 *
 * The bar is a *floating* capsule that lives in the topmost UI layer: the app
 * content runs edge-to-edge beneath it (see `LocalCelvoNavBarFloating` in
 * MainScreen) and refracts through a native [UIVisualEffectView]. Three stacked
 * layers build the glass:
 *
 *  1. native blur material — pinned to the in-app Light/Dark theme;
 *  2. a Compose sheen + specular rim — light pooling on the pane edge;
 *  3. the navigation items + a spring-driven "liquid" selection lozenge.
 *
 * Purely presentational — routing/state still belong to MainScreenScreen.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CelvoBottomBar(
    destinations: List<TopLevelRoute<*>>,
    currentRoute: String?,
    onNavigate: (TopLevelRoute<*>) -> Unit,
    modifier: Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // A native UIVisualEffectView otherwise follows the iOS *system* appearance.
    // Pin it to the in-app (Compose-controlled) theme so the glass tracks the
    // Light/Dark toggle rather than the device setting.
    val glassInterfaceStyle = if (isDark) {
        UIUserInterfaceStyle.UIUserInterfaceStyleDark
    } else {
        UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
    val selectedIndex = destinations.indexOfFirst { currentRoute.matches(it) }

    // Specular rim — light refracting along the glass edge: brightest where it
    // meets the top of the pane, dissolving toward the base.
    val rimBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.04f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.20f),
                Color.Black.copy(alpha = 0.08f),
            ),
        )
    }
    // Sheen — a faint highlight pooled at the top of the glass pane.
    val sheenBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.10f else 0.28f),
            Color.Transparent,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CapsuleHeightDp.dp)
                .shadow(
                    elevation = if (isDark) 14.dp else 20.dp,
                    shape = CapsuleShape,
                    clip = false,
                )
                .clip(CapsuleShape),
        ) {
            // ── Layer 1 · native Liquid Glass material ──────────────────────
            UIKitView(
                factory = {
                    val blurEffect = UIBlurEffect.effectWithStyle(
                        UIBlurEffectStyle.UIBlurEffectStyleSystemChromeMaterial,
                    )
                    UIVisualEffectView(effect = blurEffect).apply {
                        // Round the native layer itself — Compose clip alone
                        // does not curve an interop view's corners.
                        layer.cornerRadius = CapsuleHeightDp / 2.0
                        layer.masksToBounds = true
                        clipsToBounds = true
                        // Decorative only: keep all touch handling in Compose.
                        userInteractionEnabled = false
                        overrideUserInterfaceStyle = glassInterfaceStyle
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Re-applied on recomposition so the glass follows the
                    // in-app theme toggle, not the device appearance.
                    view.overrideUserInterfaceStyle = glassInterfaceStyle
                },
            )

            // ── Layer 2 · sheen highlight + specular rim ────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sheenBrush, CapsuleShape)
                    .border(width = 1.dp, brush = rimBrush, shape = CapsuleShape),
            )

            // ── Layer 3 · navigation items + liquid selection lozenge ───────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                val slotWidth = maxWidth / destinations.size.coerceAtLeast(1)
                val lozengeOffset by animateDpAsState(
                    targetValue = slotWidth * selectedIndex.coerceAtLeast(0),
                    animationSpec = spring(
                        dampingRatio = 0.74f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    label = "lozengeOffset",
                )

                if (selectedIndex >= 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = lozengeOffset)
                            .width(slotWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp)
                            .clip(CapsuleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    ),
                                ),
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                shape = CapsuleShape,
                            ),
                    )
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    destinations.forEachIndexed { index, item ->
                        CelvoTabItem(
                            item = item,
                            isSelected = index == selectedIndex,
                            onClick = { onNavigate(item) },
                            modifier = Modifier
                                .width(slotWidth)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CelvoTabItem(
    item: TopLevelRoute<*>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.extended.textSecondary
    val tint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "tabTint",
    )
    // A single spring drives press feedback and the selected "lift".
    val contentScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.06f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "tabScale",
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                // No Material ripple — iOS tab bars use scale feedback instead.
                indication = null,
                onClick = onClick,
            )
            .scale(contentScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = stringResource(item.name),
            tint = tint,
            modifier = Modifier.size(23.dp),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = stringResource(item.name),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Compose Navigation serializes data-object routes to their fully-qualified
 * class name; parametric routes append a `/{arg}` or `?arg=` suffix. Match by
 * prefix so both forms select the right tab.
 */
private fun String?.matches(item: TopLevelRoute<*>): Boolean {
    if (this == null) return false
    val qn = item.route::class.qualifiedName ?: return false
    return this == qn || startsWith("$qn/") || startsWith("$qn?")
}
