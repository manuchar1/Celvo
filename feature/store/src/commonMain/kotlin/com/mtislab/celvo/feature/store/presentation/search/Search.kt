package com.mtislab.celvo.feature.store.presentation.search

import CelvoPlaceholder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.mascot_fox_no_results
import com.celvo.core.designsystem.resources.ic_cancel
import com.celvo.core.designsystem.resources.search_placeholder
import com.mtislab.core.designsystem.components.buttons.CelvoActionIconButton
import com.mtislab.core.designsystem.components.inputs.CelvoSearchBar
import com.mtislab.core.designsystem.components.items.CelvoCountryItem
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.domain.model.Route
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun SearchRoot(
    initialTab: Route.SearchTab,
    focusSearch: Boolean,
    onBackClick: () -> Unit,
    onNavigateToDetails: (String, String, String) -> Unit,
    viewModel: SearchViewModel = koinViewModel { parametersOf(initialTab, focusSearch) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SearchAction.OnBackClick -> onBackClick()
                is SearchAction.OnItemClick -> onNavigateToDetails(
                    action.item.id,
                    action.item.name,
                    action.item.type.name
                )

                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.requestFocus) {
        if (state.requestFocus) {
            kotlinx.coroutines.delay(300)
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CelvoSearchBar(
                query = state.query,
                onQueryChange = { onAction(SearchAction.OnQueryChange(it)) },
                placeholder = stringResource(CoreRes.string.search_placeholder),
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f)
            )

            CelvoActionIconButton(
                icon = vectorResource(CoreRes.drawable.ic_cancel),
                onClick = { onAction(SearchAction.OnBackClick) },
                tint = MaterialTheme.colorScheme.onBackground
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Tabs ---
        CelvoTabSwitcher(
            options = listOf("ქვეყანა", "რეგიონი"), // TODO: Localize
            selectedIndex = state.selectedTab.ordinal,
            onOptionSelected = { index ->
                val tab = Route.SearchTab.entries.getOrElse(index) { Route.SearchTab.COUNTRY }
                onAction(SearchAction.OnTabSelect(tab))
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Results Count ---
   /*     Text(
            text = "${state.searchResults.size} ქვეყანა", // TODO: Localize properly (Results count)
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )*/

        Spacer(modifier = Modifier.height(8.dp))


        if (state.searchResults.isEmpty() && state.query.isNotEmpty()) {

            CelvoPlaceholder(
                icon = Res.drawable.mascot_fox_no_results,
                title = "შედეგები ვერ მოიძებნა",
                message = "მითითებული სიტყვით ვერ მოიძებნა ვერაფერი.",
                actionLabel = "გასუფთავება",
                onActionClick = { onAction(SearchAction.OnClearQuery) }
            )
        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.searchResults,
                    key = { it.id }
                ) { item ->

                    val imageUrl =
                        if (state.selectedTab == Route.SearchTab.COUNTRY) item.imageUrl else null

                    CelvoCountryItem(
                        name = item.name,
                        id = item.id,
                        imageUrl = imageUrl,
                        price = null,
                        discountPercent = null,
                        onClick = { onAction(SearchAction.OnItemClick(item)) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}