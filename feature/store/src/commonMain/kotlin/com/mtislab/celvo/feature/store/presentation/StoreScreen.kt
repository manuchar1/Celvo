package com.mtislab.celvo.feature.store.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.action_retry
import celvo.feature.store.generated.resources.search_no_results
import celvo.feature.store.generated.resources.search_no_results_desc
import celvo.feature.store.generated.resources.search_placeholder
import celvo.feature.store.generated.resources.section_all_countries
import celvo.feature.store.generated.resources.section_regional_plans
import celvo.feature.store.generated.resources.section_search_results
import celvo.feature.store.generated.resources.section_top_picks
import celvo.feature.store.generated.resources.tab_country
import celvo.feature.store.generated.resources.tab_region
import com.celvo.core.designsystem.resources.ic_cancel
import com.celvo.core.designsystem.resources.ic_google_logo
import com.celvo.core.designsystem.resources.ic_log_in
import com.mtislab.celvo.feature.store.presentation.components.MarketingBannerCarousel
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import com.mtislab.core.designsystem.components.inputs.CelvoSearchBar
import com.mtislab.core.designsystem.components.items.CelvoCountryItem
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun StoreScreenRoot(
    viewModel: StoreViewModel = koinViewModel(),
    onNavigateToDetails: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
            if (action is StoreAction.OnItemClick) {
                onNavigateToDetails(action.item.id, action.item.name)
            }
        },
        onLoginClick = onNavigateToLogin
    )
}

@Composable
fun StoreScreen(
    state: StoreState,
    onAction: (StoreAction) -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            // 1. Header Area
            StoreHeader(
                selectedTab = state.selectedTab,
                isLoggedIn = state.isLoggedIn,
                onTabSelect = { onAction(StoreAction.OnTabSelected(it)) },
                onLoginClick = onLoginClick,
                onNotificationClick = { /* TODO: Handle Notification */ }


            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Search Bar
            CelvoSearchBar(
                query = state.searchQuery,
                onQueryChange = { onAction(StoreAction.OnSearchQueryChange(it)) },
                placeholder = stringResource(Res.string.search_placeholder)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Content Area with State Management
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    state.errorMessage != null -> {
                        ErrorState(
                            message = state.errorMessage ?: "Unknown error",
                            onRetry = { onAction(StoreAction.OnRetry) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.displayedItems.isEmpty() && state.searchQuery.isNotEmpty() -> {
                        EmptySearchResults(
                            query = state.searchQuery,
                        )
                    }

                    else -> {
                        StoreContent(state = state, onAction = onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreContent(
    state: StoreState,
    onAction: (StoreAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // A. Marketing Banners (Only if available)
        if (state.marketingBanners.isNotEmpty()) {
            item {
                MarketingBannerCarousel(
                    banners = state.marketingBanners,
                    onBannerClick = { link -> onAction(StoreAction.OnBannerClick(link)) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // B. Top Picks Section (Only if available)
        if (state.displayedTopPicks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.section_top_picks),
                    hasSeeAll = true,
                    onSeeAllClick = { /* TODO: Navigate to full list */ }
                )
            }
            items(state.displayedTopPicks) { item ->
                CelvoCountryItem(
                    name = item.name,
                    price = item.formattedPrice, // Assuming pre-formatted string
                    flagUrl = item.imageUrl,
                    discountPercent = null, // Add to model if needed
                    onClick = { onAction(StoreAction.OnItemClick(item)) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // C. Main List Section
        item {
            val headerTitle = when {
                state.searchQuery.isNotEmpty() -> stringResource(Res.string.section_search_results)
                state.selectedTab == StoreTab.COUNTRIES -> stringResource(Res.string.section_all_countries)
                else -> stringResource(Res.string.section_regional_plans)
            }
            SectionHeader(title = headerTitle)
        }

        items(state.displayedItems) { item ->
            CelvoCountryItem(
                name = item.name,
                price = item.formattedPrice,
                flagUrl = item.imageUrl,
                discountPercent = null,
                onClick = { onAction(StoreAction.OnItemClick(item)) }
            )
        }
    }
}

@Composable
private fun StoreHeader(
    selectedTab: StoreTab,
    isLoggedIn: Boolean,
    onTabSelect: (StoreTab) -> Unit,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab Switcher
        // weight(1f, fill = false) prevents tabs from overlapping the button
        CelvoTabSwitcher(
            options = listOf(
                stringResource(Res.string.tab_country),
                stringResource(Res.string.tab_region)
            ),
            selectedIndex = if (selectedTab == StoreTab.COUNTRIES) 0 else 1,
            onOptionSelected = { index ->
                onTabSelect(if (index == 0) StoreTab.COUNTRIES else StoreTab.REGIONS)
            },
            modifier = Modifier.weight(1f, fill = false)
        )

        // Using a small spacer for touch target safety,
        // relying on SpaceBetween for main positioning
        Spacer(modifier = Modifier.width(16.dp))

        if (isLoggedIn) {
            CelvoCircleButton(
                icon = painterResource(CoreRes.drawable.ic_cancel),
                onClick = onNotificationClick,
                // backgroundColor = ... თუ გინდა შეცვალე
            )
        } else {
            CelvoCircleButton(
                icon = painterResource(CoreRes.drawable.ic_log_in),
                onClick = onLoginClick
            )
        }

       /* CelvoCircleButton(
            icon = painterResource(CoreRes.drawable.ic_log_in),
            onClick = onLoginClick
        )*/
    }
}

@Composable
fun SectionHeader(
    title: String,
    hasSeeAll: Boolean = false,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (hasSeeAll) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "მეტის ნახვა", // TODO: Move to stringResource(Res.string.action_see_all)
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptySearchResults(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.search_no_results),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.search_no_results_desc, query),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.action_retry))
        }
    }
}