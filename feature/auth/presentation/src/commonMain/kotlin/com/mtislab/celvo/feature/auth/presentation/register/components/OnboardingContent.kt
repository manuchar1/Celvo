package com.mtislab.celvo.feature.auth.presentation.register.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import celvo.feature.auth.presentation.generated.resources.Res
import celvo.feature.auth.presentation.generated.resources.countries_500_plus
import celvo.feature.auth.presentation.generated.resources.country_georgia
import celvo.feature.auth.presentation.generated.resources.country_germany
import celvo.feature.auth.presentation.generated.resources.country_turkiye
import celvo.feature.auth.presentation.generated.resources.ic_flag_georgia
import celvo.feature.auth.presentation.generated.resources.ic_flag_germany
import celvo.feature.auth.presentation.generated.resources.ic_flag_turkiye
import celvo.feature.auth.presentation.generated.resources.ic_world
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