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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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

import com.mtislab.celvo.feature.store.presentation.components.MarketingBannerCarousel
import com.mtislab.core.designsystem.components.inputs.CelvoSearchBar
import com.mtislab.core.designsystem.components.items.CelvoCountryItem
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StoreScreenRoot(
    viewModel: StoreViewModel = koinViewModel(), onNavigateToDetails: (String, String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        state = state, onAction = { action ->
            viewModel.onAction(action)
            if (action is StoreAction.OnItemClick) {
                onNavigateToDetails(action.item.id, action.item.name)
            }
        })
}

@Composable
fun StoreScreen(
    state: StoreState, onAction: (StoreAction) -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. TAB SWITCHER
            val tabs = listOf(
                stringResource(Res.string.tab_country), stringResource(Res.string.tab_region)
            )
            val selectedIndex = if (state.selectedTab == StoreTab.COUNTRIES) 0 else 1

            CelvoTabSwitcher(
                options = tabs, selectedIndex = selectedIndex, onOptionSelected = { index ->
                    val newTab = if (index == 0) StoreTab.COUNTRIES else StoreTab.REGIONS
                    onAction(StoreAction.OnTabSelected(newTab))
                })

            Spacer(modifier = Modifier.height(24.dp))

            // 2. SEARCH BAR
            CelvoSearchBar(
                query = state.searchQuery,
                onQueryChange = { onAction(StoreAction.OnSearchQueryChange(it)) },
                placeholder = stringResource(Res.string.search_placeholder)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. CONTENT AREA
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.errorMessage != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onAction(StoreAction.OnRetry) }) {
                            Text(stringResource(Res.string.action_retry))
                        }
                    }
                }
            } else if (state.displayedItems.isEmpty()) {
                // EMPTY STATE
                EmptySearchResults(query = state.searchQuery)
            } else {
                // ✅ MAIN LIST (VERTICAL)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp), // დაშორება ელემენტებს შორის (Figma: 12px)
                    contentPadding = PaddingValues(bottom = 100.dp) // ადგილი BottomBar-ისთვის
                ) {

                    // --- Banners ---
                    item {
                        if (state.marketingBanners.isNotEmpty()) {
                            // პატარა დაშორება სერჩსა და ბანერს შორის
                            MarketingBannerCarousel(
                                banners = state.marketingBanners, onBannerClick = { link ->
                                    onAction(StoreAction.OnBannerClick(link))
                                })
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // --- Top Picks (Vertical List) ---
                    // ეს სექცია გამოჩნდება მხოლოდ მაშინ, როცა displayedTopPicks არ არის ცარიელი.
                    // ViewModel-ის ლოგიკით, ძებნის დროს ეს სია ცარიელდება, ამიტომ სექციაც გაქრება.
                    if (state.displayedTopPicks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(Res.string.section_top_picks),
                                hasSeeAll = true, // Figma-ში აქვს "მეტის ნახვა"
                                onSeeAllClick = { /* TODO: ნავიგაცია სრულ სიაზე */ })
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Top Picks აიტემები (ვერტიკალურად)
                        items(state.displayedTopPicks) { item ->
                            CelvoCountryItem(
                                name = item.name,
                                price = "${item.formattedPrice} - დან", // ფორმატირება
                                flagUrl = item.imageUrl,
                                discountPercent = null, // TODO: თუ ბექენდიდან მოვა, აქ ჩავსვამთ item.discountPercent
                                onClick = { onAction(StoreAction.OnItemClick(item)) })
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp)) // დაშორება სექციებს შორის
                        }
                    }

                    // --- All Countries / Search Results ---
                    item {
                        val headerTitle =
                            if (state.searchQuery.isNotEmpty()) stringResource(Res.string.section_search_results)
                            else if (state.selectedTab == StoreTab.COUNTRIES) stringResource(Res.string.section_all_countries)
                            else stringResource(Res.string.section_regional_plans)

                        SectionHeader(title = headerTitle)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Main Items (Vertical)
                    items(state.displayedItems) { item ->
                        CelvoCountryItem(
                            name = item.name,
                            price = "${item.formattedPrice} - დან",
                            flagUrl = item.imageUrl,
                            discountPercent = null,
                            onClick = { onAction(StoreAction.OnItemClick(item)) })
                    }
                }
            }
        }
    }
}

// --- Helper UI Components ---

@Composable
fun SectionHeader(
    title: String, hasSeeAll: Boolean = false, onSeeAllClick: () -> Unit = {}
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
                    text = "მეტის ნახვა", //stringResource(Res.string.action_see_all), // "მეტის ნახვა"
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
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