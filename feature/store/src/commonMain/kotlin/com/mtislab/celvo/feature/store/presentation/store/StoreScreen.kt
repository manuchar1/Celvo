package com.mtislab.celvo.feature.store.presentation.store

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.action_retry
import celvo.feature.store.generated.resources.action_see_all
import celvo.feature.store.generated.resources.section_popular_countries
import celvo.feature.store.generated.resources.section_popular_regions
import coil3.compose.AsyncImage
import com.celvo.core.designsystem.resources.ic_log_in
import com.celvo.core.designsystem.resources.ic_notif_ring
import com.celvo.core.designsystem.resources.search
import com.celvo.core.designsystem.resources.search_placeholder
import com.mtislab.celvo.feature.store.presentation.components.MarketingBannerCarousel
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.indicators.CelvoUsageGauge
import com.mtislab.core.designsystem.components.items.CelvoRegionItem
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.theme.titleXSmall
import com.mtislab.core.domain.model.ActiveBundle
import com.mtislab.core.domain.model.Route
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun StoreScreenRoot(
    viewModel: StoreViewModel = koinViewModel(),
    onNavigateToDetails: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: (Route.SearchTab, Boolean) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            when (action) {
                is StoreAction.OnItemClick -> onNavigateToDetails(
                    action.item.id,
                    action.item.name,
                    action.item.type.name
                )

                is StoreAction.OnRegionClick -> onNavigateToDetails(
                    action.item.id,
                    action.item.name,
                    action.item.type.name
                )

                is StoreAction.OnSearchClick -> onNavigateToSearch(Route.SearchTab.COUNTRY, true)
                else -> { /* ViewModel handles the rest */
                }
            }
        },
        onLoginClick = onNavigateToLogin,
        onNavigateToSearch = onNavigateToSearch
    )
}


@Composable
fun StoreScreen(
    state: StoreState,
    onAction: (StoreAction) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSearch: (Route.SearchTab, Boolean) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            when (state) {
                is StoreState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is StoreState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { onAction(StoreAction.OnRetry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StoreState.Content -> {
                    HomeContent(
                        state = state,
                        onAction = onAction,
                        onLoginClick = onLoginClick,
                        onNavigateToSearch = onNavigateToSearch

                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: StoreState.Content,
    onAction: (StoreAction) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSearch: (Route.SearchTab, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item(key = "header") {
            HomeHeader(
                isLoggedIn = state.isLoggedIn,
                activeBundle = state.activeEsimHome?.activeBundle,
                onLoginClick = onLoginClick,
                onNotificationClick = { /* TODO */ },
                modifier = Modifier.padding(horizontal = 16.dp),
                onAction = onAction
            )
        }

        // ── 2. Active eSIM Gauge (logged-in with active bundle only) ─────
        if (state.isLoggedIn && state.activeEsimHome?.hasActiveBundle == true) {
            val bundle = state.activeEsimHome.activeBundle
            if (bundle != null) {
                item(key = "usage_gauge") {
                    ActiveEsimSection(
                        bundle = bundle,
                        onTopUpClick = { onAction(StoreAction.OnTopUpClick) },
                        onDetailsClick = { onAction(StoreAction.OnDetailsClick) },
                        onSupportClick = { onAction(StoreAction.OnSupportClick) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ── 3. Marketing Banners ────────
        if (state.marketingBanners.isNotEmpty()) {
            item(key = "banners") {
                MarketingBannerCarousel(
                    banners = state.marketingBanners,
                    onBannerClick = { link -> onAction(StoreAction.OnBannerClick(link)) }
                )
                //Spacer(modifier = Modifier.height(24.dp))
            }
        }


        if (state.regions.isNotEmpty()) {
            item(key = "regions_header") {
                SectionHeader(
                    title = stringResource(Res.string.section_popular_regions),
                    onSeeAllClick = { onNavigateToSearch(Route.SearchTab.REGION, false) },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item(key = "regions_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(
                        items = state.regions,
                        key = { it.id }
                    ) { region ->
                        CelvoRegionItem(
                            name = region.name,
                            id = region.id,
                            imageUrl = null,
                            priceDisplay = region.formattedPrice,
                            modifier = Modifier.width(140.dp),
                            onClick = { onAction(StoreAction.OnRegionClick(region)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }


        if (state.topPicks.isNotEmpty()) {
            item(key = "top_picks_header") {
                SectionHeader(
                    title = stringResource(Res.string.section_popular_countries),
                    onSeeAllClick = { onNavigateToSearch(Route.SearchTab.COUNTRY, false) },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item(key = "top_picks_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(
                        items = state.topPicks,
                        key = { "top_${it.id}" }
                    ) { item ->
                        CelvoRegionItem(
                            name = item.name,
                            id = item.id,
                            imageUrl = item.imageUrl,
                            priceDisplay = item.formattedPrice,
                            modifier = Modifier.width(140.dp),
                            onClick = { onAction(StoreAction.OnItemClick(item)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}



@Composable
private fun HomeHeader(
    isLoggedIn: Boolean,
    activeBundle: ActiveBundle?,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (StoreAction) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.weight(1f)) {
            if (isLoggedIn && activeBundle != null) {
                // Logged-in with active bundle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { /* TODO */ }
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = activeBundle.flagUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        contentScale = Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeBundle.countryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "▾",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                SearchBarTrigger(
                    onClick = { onAction(StoreAction.OnSearchClick) }
                )
            }
        }

        if (isLoggedIn) {
            CelvoCircleButton(
                icon = painterResource(CoreRes.drawable.ic_notif_ring),
                onClick = onNotificationClick,
                size = 48.dp
            )
        } else {
            CelvoCircleButton(
                icon = painterResource(CoreRes.drawable.ic_log_in),
                onClick = onLoginClick,
                size = 48.dp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active eSIM Section (Usage Gauge + Actions)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveEsimSection(
    bundle: ActiveBundle,
    onTopUpClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.colorScheme.extended

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Usage Gauge
        val usedGb =
            bundle.usedFormatted.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
        val totalGb =
            bundle.initialFormatted.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0

        CelvoUsageGauge(
            usedAmount = usedGb,
            totalAmount = totalGb,
            unit = "GB",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Active status chip: "✓ აქტიური - ოქტ 6, 2026"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = extendedColors.success,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "აქტიური - ${bundle.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.success,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons row: შევსება | დეტალები | ?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Up button
            ActionChip(
                label = "შევსება",
                icon = "+",
                onClick = onTopUpClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details button
            ActionChip(
                label = "დეტალები",
                icon = "→",
                onClick = onDetailsClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Support circle button
            CelvoCircleButton(
                icon = painterResource(CoreRes.drawable.ic_log_in), // TODO: Replace with ic_help / ic_support
                onClick = onSupportClick,
                size = 40.dp,

                )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Chip (შევსება +, დეტალები →)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActionChip(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SearchBarTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CelvoCard(
        modifier = modifier.padding(vertical = 12.dp).padding(end = 16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(CoreRes.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.extended.textPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(CoreRes.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
        }
    }
}


@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.extended.textPrimary,
            style = MaterialTheme.typography.titleXSmall
        )

            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = stringResource(Res.string.action_see_all),
                    style = MaterialTheme.typography.titleXSmall,
                    color = MaterialTheme.colorScheme.primary
                )

        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.action_retry))
        }
    }
}