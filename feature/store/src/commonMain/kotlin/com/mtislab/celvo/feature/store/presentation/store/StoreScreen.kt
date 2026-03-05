package com.mtislab.celvo.feature.store.presentation.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.action_retry
import celvo.feature.store.generated.resources.action_see_all
import celvo.feature.store.generated.resources.add_data
import celvo.feature.store.generated.resources.install
import celvo.feature.store.generated.resources.my_plans
import celvo.feature.store.generated.resources.section_popular_countries
import celvo.feature.store.generated.resources.section_popular_regions
import celvo.feature.store.generated.resources.select_esim
import com.celvo.core.designsystem.resources.ic_arrow_down
import com.celvo.core.designsystem.resources.ic_download
import com.celvo.core.designsystem.resources.ic_log_in
import com.celvo.core.designsystem.resources.ic_notif_ring
import com.celvo.core.designsystem.resources.ic_rounded_arrow_left
import com.celvo.core.designsystem.resources.ic_rounded_question_mark
import com.celvo.core.designsystem.resources.ic_sim_card
import com.celvo.core.designsystem.resources.ic_top_up
import com.celvo.core.designsystem.resources.search
import com.celvo.core.designsystem.resources.search_placeholder
import com.mtislab.celvo.feature.store.presentation.components.MarketingBannerCarousel
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.indicators.CelvoUsageGauge
import com.mtislab.core.designsystem.components.items.CelvoRegionItem
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.theme.titleXSmall
import com.mtislab.core.domain.model.EsimHomePackage
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.model.UserEsim
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

    val claimedCode by PromoStateHolder.claimedPromoCode.collectAsState()


    // ── Collect one-shot URL events ──────────────────────────────────────
    // LocalUriHandler is the correct Compose Multiplatform abstraction for
    // opening URLs on both Android (Intent.ACTION_VIEW) and iOS (UIApplication
    // openURL). No expect/actual required.
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is StoreUiEvent.OpenUrl -> {
                    // runCatching prevents a crash if the device has no app
                    // capable of handling the eSIM deep-link scheme.
                    runCatching { uriHandler.openUri(event.url) }
                        .onFailure {
                            // Surface the failure back so the UI can show
                            // a user-friendly error if needed. For now we
                            // log — hook into your error reporting later.
                        }
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onScreenResumed()
    }

    StoreScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            // Navigation side-effects that belong at the Root level
            when (action) {
                is StoreAction.OnItemClick -> onNavigateToDetails(
                    action.item.id, action.item.name, action.item.type.name
                )

                is StoreAction.OnRegionClick -> onNavigateToDetails(
                    action.item.id, action.item.name, action.item.type.name
                )

                is StoreAction.OnSearchClick -> onNavigateToSearch(
                    Route.SearchTab.COUNTRY, true
                )

                else -> Unit
            }
        },
        onLoginClick = onNavigateToLogin,
        onNavigateToSearch = onNavigateToSearch
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreState,
    onAction: (StoreAction) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSearch: (Route.SearchTab, Boolean) -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { padding ->
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
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onAction(StoreAction.OnRefresh) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HomeContent(
                            state = state,
                            onAction = onAction,
                            onLoginClick = onLoginClick,
                            onNavigateToSearch = onNavigateToSearch
                        )
                    }

                    if (state.showEsimSwitcher) {
                        EsimSwitcherSheet(
                            esims = state.activeEsimHome?.esims ?: emptyList(),
                            selectedIndex = state.selectedEsimIndex,
                            onEsimSelected = { onAction(StoreAction.OnEsimSelected(it)) },
                            onDismiss = { onAction(StoreAction.OnEsimSwitcherDismiss) }
                        )
                    }
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


        // ── 1. Header ────────────────────────────────────────────────────
        item(key = "header") {
            HomeHeader(
                isLoggedIn = state.isLoggedIn,
                selectedEsim = state.selectedEsim,
                onLoginClick = onLoginClick,
                onNotificationClick = { /* TODO */ },
                onEsimSwitcherClick = { onAction(StoreAction.OnEsimSwitcherClick) },
                onSearchClick = { onAction(StoreAction.OnSearchClick) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // ── 2. eSIM Section (Gauge Carousel + Actions) ──────────────────
        val selectedEsim = state.selectedEsim
        if (state.isLoggedIn && selectedEsim != null) {
            item(key = "esim_section_${selectedEsim.iccid}") {

                EsimSection(
                    esim = selectedEsim,
                    isDataStale = state.isDataStale,
                    onInstallClick = { onAction(StoreAction.OnInstallClick) },
                    onTopUpClick = { onAction(StoreAction.OnTopUpClick) },
                    onDetailsClick = { onAction(StoreAction.OnDetailsClick) },
                    onSupportClick = { onAction(StoreAction.OnSupportClick) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Divider between eSIM section and marketing content
            item(key = "esim_divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.extended.divider
                )
            }
        }

        // ── 3. Marketing Banners ─────────────────────────────────────────
        if (state.marketingBanners.isNotEmpty()) {
            item(key = "banners") {
                MarketingBannerCarousel(
                    banners = state.marketingBanners,
                    // 👉 Read from the strict MVI state
                    claimedPromoCode = state.claimedPromoCode,
                    onBannerClick = { deepLink ->
                        onAction(StoreAction.OnBannerClick(deepLink))
                    },
                    // 👉 Fire the pure intent
                    onClaimCode = { code ->
                        onAction(StoreAction.OnClaimPromoCode(code))
                    }
                )
            }
        }

        // ── 4. Regions ───────────────────────────────────────────────────
        if (state.regions.isNotEmpty()) {
            item(key = "regions_header") {
                SectionHeader(
                    title = stringResource(Res.string.section_popular_regions),
                    onSeeAllClick = { onNavigateToSearch(Route.SearchTab.REGION, false) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item(key = "regions_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(items = state.regions, key = { it.id }) { region ->
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

        // ── 5. Top Picks ─────────────────────────────────────────────────
        if (state.topPicks.isNotEmpty()) {
            item(key = "top_picks_header") {
                SectionHeader(
                    title = stringResource(Res.string.section_popular_countries),
                    onSeeAllClick = { onNavigateToSearch(Route.SearchTab.COUNTRY, false) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            item(key = "top_picks_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(items = state.topPicks, key = { "top_${it.id}" }) { item ->
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


// ═════════════════════════════════════════════════════════════════════════════
// Header
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeHeader(
    isLoggedIn: Boolean,
    selectedEsim: UserEsim?,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onEsimSwitcherClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (isLoggedIn && selectedEsim != null) {
                EsimSwitcherChip(
                    esim = selectedEsim,
                    onClick = onEsimSwitcherClick
                )
            } else {
                SearchBarTrigger(onClick = onSearchClick)
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


@Composable
fun EsimSwitcherChip(
    esim: UserEsim,
    onClick: () -> Unit
) {
    CelvoCard(
        modifier = Modifier.padding(end = 16.dp),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(CoreRes.drawable.ic_sim_card),
                contentDescription = "SIM Card",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )


            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = esim.headerLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(CoreRes.drawable.ic_arrow_down),
                contentDescription = "Dropdown indicator",
                modifier = Modifier.width(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// ═════════════════════════════════════════════════════════════════════════════
// eSIM Section — Package Carousel + Status + Actions
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun EsimSection(
    esim: UserEsim,
    isDataStale: Boolean,
    onInstallClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val packages = esim.packages


    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (packages.isNotEmpty()) {
            // ── Package Carousel ─────────────────────────────────────────
            val pagerState = rememberPagerState(pageCount = { packages.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                PackageGaugePage(pkg = packages[page])
            }


            //Spacer(modifier = Modifier.height(12.dp))

            // ── Status Chip ──────────────────────────────────────────────
            val currentPkg = packages.getOrNull(pagerState.currentPage)
            /*     if (currentPkg != null) {
                     PackageStatusChip(pkg = currentPkg)
                 }*/

            Spacer(modifier = Modifier.height(12.dp))

            // ── Dot Indicators ───────────────────────────────────────────
            if (packages.size > 1) {
                PagerDotIndicator(
                    pageCount = packages.size,
                    currentPage = pagerState.currentPage
                )
            }
        }

        // ── Stale Data Warning ───────────────────────────────────────────
        if (isDataStale) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "მონაცემები შესაძლოა მოძველებული იყოს",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Action Buttons ───────────────────────────────────────────────
        EsimActionButtons(
            isInstalled = esim.installed,
            onInstallClick = onInstallClick,
            onTopUpClick = onTopUpClick,
            onDetailsClick = onDetailsClick,
            onSupportClick = onSupportClick
        )
    }
}


// ═════════════════════════════════════════════════════════════════════════════
// Package Gauge Page (single HorizontalPager page)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun PackageGaugePage(
    pkg: EsimHomePackage
) {
    val usedGb = pkg.usedFormatted
        .filter { it.isDigit() || it == '.' }
        .toDoubleOrNull() ?: 0.0
    val totalGb = pkg.initialFormatted
        .filter { it.isDigit() || it == '.' }
        .toDoubleOrNull() ?: 1.0


    CelvoUsageGauge(
        usedAmount = usedGb.toFloat(),
        totalAmount = totalGb.toFloat(),
        unit = "GB",
        modifier = Modifier
            .fillMaxWidth(),
        flagUrl = pkg.flagUrl,
        primaryColor = MaterialTheme.colorScheme.primary,

        )

}


@Composable
private fun PagerDotIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            }
            val dotSize = if (isSelected) 8.dp else 6.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


// ═════════════════════════════════════════════════════════════════════════════
// Action Buttons — colored icon circles per Figma
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun EsimActionButtons(
    isInstalled: Boolean,
    onInstallClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {


    Row(
        modifier = modifier.fillMaxWidth(),
        // horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (isInstalled) {
            ActionChip(
                label = stringResource(Res.string.add_data),
                icon = painterResource(CoreRes.drawable.ic_top_up),
                onClick = onTopUpClick,
                modifier = modifier.weight(1f)
            )
        } else {
            ActionChip(
                label = stringResource(Res.string.install),
                icon = painterResource(CoreRes.drawable.ic_download),
                onClick = onInstallClick,
                modifier = modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))


        ActionChip(
            label = stringResource(Res.string.my_plans),
            icon = painterResource(CoreRes.drawable.ic_rounded_arrow_left),
            onClick = onDetailsClick,
            modifier = modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))


        CelvoCircleButton(
            icon = painterResource(CoreRes.drawable.ic_rounded_question_mark),
            onClick = onSupportClick,
            contentColor = Color.Unspecified,

            )
    }
}


@Composable
private fun ActionChip(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CelvoButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp),
        text = label,
        trailingIcon = icon,
        arrangement = Arrangement.SpaceBetween
    )


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EsimSwitcherSheet(
    esims: List<UserEsim>,
    selectedIndex: Int,
    onEsimSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(Res.string.select_esim),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            esims.forEachIndexed { index, esim ->
                val isSelected = index == selectedIndex
                val extendedColors = MaterialTheme.colorScheme.extended

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable { onEsimSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {


                    CelvoCircleButton(
                        icon = painterResource(CoreRes.drawable.ic_sim_card),
                        onClick = {})


                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "eSIM #${esim.esimNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = esim.profileStatusDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = extendedColors.textSecondary
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (index < esims.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}


@Composable
private fun SearchBarTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CelvoCard(
        modifier = modifier
            .padding(vertical = 12.dp)
            .padding(end = 16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
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