package com.mtislab.celvo.feature.store.presentation.packages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.packages_badge_countries
import celvo.feature.store.generated.resources.packages_badge_discount
import celvo.feature.store.generated.resources.packages_pick_plan
import celvo.feature.store.generated.resources.packages_subtitle_separator
import celvo.feature.store.generated.resources.packages_tab_data
import celvo.feature.store.generated.resources.packages_tab_unlimited
import coil3.compose.AsyncImage
import com.celvo.core.designsystem.resources.ic_left_arrow
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.getRegionIcon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes


@Composable
fun PackagesScreenRoot(
    isoCode: String,
    countryName: String,
    type: String,
    onBackClick: () -> Unit,
    onPackageSelected: (EsimPackage) -> Unit,
    viewModel: PackagesScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(isoCode) {
        viewModel.onAction(PackagesScreenAction.LoadPackages(isoCode))
    }


    PackagesScreenScreen(
        state = state,
        countryName = countryName,
        type = type,
        isoCode = isoCode,
        onAction = { action ->
            when (action) {
                is PackagesScreenAction.BackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        },
        onPackageSelected = onPackageSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreenScreen(
    state: PackagesScreenState,
    countryName: String,
    type: String,
    isoCode: String,
    onAction: (PackagesScreenAction) -> Unit,
    onPackageSelected: (EsimPackage) -> Unit
) {

    val flagUrl = remember(isoCode) {
        "https://flagcdn.com/h240/${isoCode.lowercase()}.png"
    }

    val regionIcon = remember(isoCode) {
        getRegionIcon(isoCode)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PackagesTopBar(
                countryName = countryName,
                regionIcon = regionIcon,
                flagUrl = flagUrl,
                type = type,
                onBackClick = { onAction(PackagesScreenAction.BackClick) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Switcher ---
            if (state.showCategorySwitcher) {
                val selectedIndex = when (state.selectedCategory) {
                    PackageCategory.DATA -> 0
                    PackageCategory.UNLIMITED -> 1
                }

                CelvoTabSwitcher(
                    options = listOf(
                        stringResource(Res.string.packages_tab_data),
                        stringResource(Res.string.packages_tab_unlimited),
                    ),
                    selectedIndex = selectedIndex,
                    onOptionSelected = { index ->
                        val newCategory =
                            if (index == 0) PackageCategory.DATA else PackageCategory.UNLIMITED
                        onAction(PackagesScreenAction.SelectCategory(newCategory))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Title ---
            Text(
                text = stringResource(Res.string.packages_pick_plan),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // --- List ---
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.extended.success)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(state.filteredPackages) { pkg ->
                        PackageCard(
                            pkg = pkg,
                            onClick = { onPackageSelected(pkg) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PackageCard(
    pkg: EsimPackage,
    onClick: () -> Unit
) {
    val title = buildString {
        append(pkg.dataAmountDisplay)
        pkg.planTier?.takeIf { it.isNotBlank() }?.let {
            append(' ')
            append(it)
        }
    }

    val subtitle = packageSubtitle(pkg)

    CelvoCard(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            pkg.badgeText?.takeIf { it.isNotBlank() }?.let { promoText ->
                PromoPill(
                    text = promoText,
                    hexColor = pkg.badgeColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.extended.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                        maxLines = 1
                    )
                }

                PriceSection(pkg)
            }
        }
    }
}

@Composable
private fun packageSubtitle(pkg: EsimPackage): String {
    val separator = stringResource(Res.string.packages_subtitle_separator)
    val coverageLabel = if (pkg.coverageCount > 1) {
        stringResource(Res.string.packages_badge_countries, pkg.coverageCount)
    } else {
        null
    }
    return listOfNotNull(pkg.validityDisplay, coverageLabel)
        .filter { it.isNotBlank() }
        .joinToString(separator)
}

@Composable
fun PriceSection(pkg: EsimPackage) {
    val originalPrice = pkg.originalPrice
    val showStrike = originalPrice != null && originalPrice > pkg.price
    val discountPercent = pkg.discountPercent?.takeIf { it > 0 }

    Column(horizontalAlignment = Alignment.End) {
        if (showStrike) {
            val formattedOld = if (originalPrice % 1.0 == 0.0) {
                "${originalPrice.toInt()}"
            } else {
                "$originalPrice"
            }
            Text(
                text = "${pkg.currency} $formattedOld",
                style = MaterialTheme.typography.bodySmall.copy(
                    textDecoration = TextDecoration.LineThrough,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
        }

        Text(
            text = "${pkg.currency} ${pkg.price}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )

        if (discountPercent != null) {
            Spacer(modifier = Modifier.height(4.dp))
            DiscountChip(
                percent = discountPercent,
                hexColor = pkg.badgeColor
            )
        }
    }
}

@Composable
private fun DiscountChip(percent: Int, hexColor: String?) {
    val bg = parseHexColor(hexColor) ?: MaterialTheme.colorScheme.extended.warning
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(Res.string.packages_badge_discount, percent),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = Color.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun PromoPill(
    text: String,
    hexColor: String?,
    modifier: Modifier = Modifier
) {
    val bg = parseHexColor(hexColor) ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = Color.Black,
            maxLines = 1
        )
    }
}

private fun parseHexColor(hex: String?): Color? {
    val raw = hex?.trim()?.removePrefix("#") ?: return null
    val normalized = when (raw.length) {
        6 -> "FF$raw"
        8 -> raw
        else -> return null
    }
    val parsed = normalized.toLongOrNull(16) ?: return null
    return Color(parsed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesTopBar(
    countryName: String,
    regionIcon: DrawableResource,
    flagUrl: Any,
    type: String,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if (type == "REGION") {
                    Image(
                        painter = painterResource(regionIcon),
                        contentDescription = type,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    AsyncImage(
                        model = flagUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    )
                }


                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = countryName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(CoreRes.drawable.ic_left_arrow),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.extended.textPrimary
                )
            }
        }
    )
}