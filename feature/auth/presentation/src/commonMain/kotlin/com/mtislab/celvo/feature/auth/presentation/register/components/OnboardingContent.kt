package com.mtislab.celvo.feature.auth.presentation.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import celvo.feature.auth.presentation.generated.resources.Res
import celvo.feature.auth.presentation.generated.resources.countries_500_plus
import celvo.feature.auth.presentation.generated.resources.country_georgia
import celvo.feature.auth.presentation.generated.resources.country_germany
import celvo.feature.auth.presentation.generated.resources.country_turkiye
import celvo.feature.auth.presentation.generated.resources.ic_flag_georgia
import celvo.feature.auth.presentation.generated.resources.ic_flag_germany
import celvo.feature.auth.presentation.generated.resources.ic_flag_turkiye
import celvo.feature.auth.presentation.generated.resources.ic_world
import celvo.feature.auth.presentation.generated.resources.onboarding_step_no_roaming
import celvo.feature.auth.presentation.generated.resources.onboarding_step_online
import celvo.feature.auth.presentation.generated.resources.onboarding_step_pay
import celvo.feature.auth.presentation.generated.resources.onboarding_step_pick_plan
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

data class GridItem(val name: String, val icon: DrawableResource)

@Composable
fun OnboardingSlidingContent(
    page: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when(page) {
           0 -> {
               CountriesGrid2x2()
           }
           1 -> {
               ActivationStepsGrid2x2()
           }
        }

    }
}

@Composable
private fun CountriesGrid2x2(modifier: Modifier = Modifier) {
    val extendedColors = MaterialTheme.colorScheme.extended
    val successBg = extendedColors.success.copy(alpha = 0.15f)
    val successBorder = extendedColors.success
    val successText = extendedColors.success

    val row1 = listOf(
        GridItem(stringResource(Res.string.country_turkiye), Res.drawable.ic_flag_turkiye),
        GridItem(stringResource(Res.string.country_germany), Res.drawable.ic_flag_germany)
    )

    val geoItem = GridItem(stringResource(Res.string.country_georgia), Res.drawable.ic_flag_georgia)
    val worldItem = GridItem(stringResource(Res.string.countries_500_plus), Res.drawable.ic_world)


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            row1.forEach { item ->
                CountryCard(
                    name = item.name,
                    flagRes = item.icon,
                    modifier = Modifier.weight(1f).aspectRatio(1.13f)
                )
            }
        }

        Row(
            modifier = Modifier.padding(end = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CountryCard(
                name = geoItem.name,
                flagRes = geoItem.icon,
                modifier = Modifier.weight(1f).aspectRatio(1.13f)
            )

            CountryCard(
                name = worldItem.name,
                flagRes = worldItem.icon,
                modifier = Modifier.weight(1f).aspectRatio(1.13f),
                backgroundColor = successBg,
                borderColor = successBorder,
                textColor = successText
            )
        }
    }
}

/**
 * Page 2 — "how it works" in the same visual language as [CountriesGrid2x2]:
 * staggered 2x2 card grid, numbered steps, green accent card as the payoff.
 */
@Composable
private fun ActivationStepsGrid2x2(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepCard(
                badge = "1",
                label = stringResource(Res.string.onboarding_step_pick_plan),
                modifier = Modifier.weight(1f).aspectRatio(1.13f)
            )
            StepCard(
                badge = "2",
                label = stringResource(Res.string.onboarding_step_pay),
                modifier = Modifier.weight(1f).aspectRatio(1.13f)
            )
        }

        Row(
            modifier = Modifier.padding(end = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepCard(
                badge = "3",
                label = stringResource(Res.string.onboarding_step_online),
                modifier = Modifier.weight(1f).aspectRatio(1.13f)
            )
            StepCard(
                badge = "✓",
                label = stringResource(Res.string.onboarding_step_no_roaming),
                modifier = Modifier.weight(1f).aspectRatio(1.13f),
                accent = true
            )
        }
    }
}

@Composable
private fun StepCard(
    badge: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    val extendedColors = MaterialTheme.colorScheme.extended
    val badgeColor = if (accent) extendedColors.success else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                spotColor = extendedColors.cardShadow,
                ambientColor = extendedColors.cardShadow,
                clip = false
            )
            .clip(shape)
            .background(if (accent) badgeColor.copy(alpha = 0.15f) else extendedColors.cardBackground)
            .border(
                width = 0.5.dp,
                color = if (accent) badgeColor else extendedColors.cardBorder,
                shape = shape
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans
                ),
                color = badgeColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PlusJakartaSans
            ),
            color = if (accent) badgeColor else extendedColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}