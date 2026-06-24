package com.mtislab.celvo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtislab.celvo.navigation.TopLevelRoute
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Android-flavoured bottom bar — wraps Material3 [NavigationBar] in a
 * card-like surface (matching [com.mtislab.core.designsystem.components.cards.CelvoCard]:
 * `extended.cardBackground` fill, hairline `cardBorder`, and `cardShadow`-tinted
 * elevation) so the bar reads as a floating pane rather than a solid slab.
 *
 * Selected-state contrast was the other pain point: the previous indicator used
 * `primaryContainer` (CelvoPurple900) which sat too close in hue to the icon's
 * `primary` (CelvoPurple500), washing out the active glyph. We now use a
 * translucent primary lozenge — same approach as the iOS "Liquid Glass" pill —
 * so the icon stays vivid against it. The selected label also switches to
 * SemiBold to reinforce the active tab.
 */
@Composable
actual fun CelvoBottomBar(
    destinations: List<TopLevelRoute<*>>,
    currentRoute: String?,
    onNavigate: (TopLevelRoute<*>) -> Unit,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extended = colorScheme.extended
    val barShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    val shadowModifier = if (extended.cardShadow != Color.Transparent) {
        Modifier.shadow(
            elevation = 8.dp,
            shape = barShape,
            spotColor = extended.cardShadow,
            ambientColor = extended.cardShadow,
            clip = false,
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(shadowModifier),
        shape = barShape,
        color = extended.cardBackground,
        border = BorderStroke(0.5.dp, extended.cardBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            destinations.forEach { item ->
                val isSelected = currentRoute.matches(item)

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item) },
                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = stringResource(item.name),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.name),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // Translucent primary lozenge — keeps the icon vivid
                        // (iOS uses the same pattern in CelvoBottomBar.ios.kt).
                        indicatorColor = colorScheme.primary.copy(alpha = 0.18f),
                        selectedIconColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        unselectedIconColor = colorScheme.extended.textSecondary,
                        unselectedTextColor = colorScheme.extended.textSecondary,
                    ),
                )
            }
        }
    }
}

/**
 * Compose Navigation serializes data-object routes to their fully-qualified
 * class name; for parametric routes it appends a `/{arg}` or `?arg=` suffix.
 * Match by prefix so both forms select the right tab.
 */
private fun String?.matches(item: TopLevelRoute<*>): Boolean {
    if (this == null) return false
    val qn = item.route::class.qualifiedName ?: return false
    return this == qn || startsWith("$qn/") || startsWith("$qn?")
}
