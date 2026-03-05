package com.mtislab.core.designsystem.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.getRegionIcon
import com.mtislab.core.domain.model.PackageInfoCardData
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A single info row inside [ProductInfoCard].
 *
 * @param icon       Icon from any module's resources (passed by the caller).
 * @param label      Dimmed label rendered above [value].
 * @param value      Primary value text.
 * @param valueColor Override for value color — use for status rows (e.g. green "აქტიური").
 *                   Defaults to [ExtendedColors.textPrimary].
 * @param isClickable Whether to show a trailing arrow and respond to taps.
 * @param onClick    Callback when [isClickable] is true.
 */
@Composable
fun PackageInfoRow(
    icon: DrawableResource,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.extended.textPrimary,
    isClickable: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.extended.textSecondary),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = valueColor,
            )
        }
        if (isClickable) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.extended.textSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun CountryBadge(
    isoCode: String,
    countryName: String,
    badgeType: PackageInfoCardData.BadgeType,
    region: String,
    modifier: Modifier = Modifier,
) {
    val flagUrl = remember(isoCode) { "https://flagcdn.com/h240/${isoCode.lowercase()}.png" }
    val regionIcon = remember(region) { getRegionIcon(region) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.extended.textPrimary.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (badgeType) {
                PackageInfoCardData.BadgeType.Region -> Image(
                    painter = painterResource(regionIcon),
                    contentDescription = countryName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                )
                PackageInfoCardData.BadgeType.Country -> AsyncImage(
                    model = flagUrl,
                    contentDescription = countryName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = countryName,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary,
            )
        }
    }
}

/** Internal divider — implementation detail of [ProductInfoCard], not a public API. */
@Composable
internal fun PackageInfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    )
}