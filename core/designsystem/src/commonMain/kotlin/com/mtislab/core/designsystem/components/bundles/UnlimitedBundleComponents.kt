package com.mtislab.core.designsystem.components.bundles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.domain.model.ThrottleTerms
import com.mtislab.core.domain.model.UnlimitedTier

/**
 * Small pill badge for the marketing tier of an unlimited bundle (Essential,
 * Plus, Premium, …). Every colour comes from the existing palette — tiers map
 * to alpha-tinted role colours rather than new tokens.
 */
@Composable
fun UnlimitedTierBadge(
    tier: UnlimitedTier,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.extended

    val (textColor, backgroundColor) = when (tier) {
        UnlimitedTier.ESSENTIAL -> colors.textSecondary to colors.inputBackground
        UnlimitedTier.PLUS -> colors.success to CelvoGreen500Alpha15
        UnlimitedTier.PREMIUM -> colors.textLink to CelvoPurple500Alpha15
        UnlimitedTier.PROMO -> colors.warning to colors.warning.copy(alpha = 0.15f)
        UnlimitedTier.OTHER -> colors.textSecondary to colors.inputBackground
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = tier.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

/**
 * Throttle / fair-use disclosure shown below an unlimited bundle. Two lines:
 *  1. Cap reached at full speed (e.g. "1 GB at full speed").
 *  2. What happens after the cap (e.g. "then 1.3 Mbps").
 *
 * Each line is prefixed with a contextual icon. The component is purely
 * presentational — formatting lives in `BundleDisplayBuilders.throttle()`.
 */
@Composable
fun ThrottleDisclosure(
    terms: ThrottleTerms,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme.extended

    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ThrottleGlyph(symbol = "", tint = colors.warning)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = terms.capLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThrottleGlyph(symbol = "", tint = colors.textSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = terms.throttledLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ThrottleGlyph(symbol: String, tint: Color) {
    // Emoji glyphs render via the system font, so we ignore the tint colour for
    // the glyph itself but keep the parameter so a future swap to a vector icon
    // stays one-line. The visual weight comes from the body-medium size.
    Text(
        text = symbol,
        style = MaterialTheme.typography.bodyMedium
    )
}
