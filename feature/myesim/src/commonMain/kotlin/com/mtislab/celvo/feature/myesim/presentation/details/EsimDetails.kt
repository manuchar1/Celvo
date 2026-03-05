package com.mtislab.celvo.feature.myesim.presentation.details

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.myesim.generated.resources.Res
import celvo.feature.myesim.generated.resources.ic_network_broadcast
import celvo.feature.myesim.generated.resources.ic_validity_period

import com.mtislab.celvo.feature.myesim.data.dto.BundleDto
import com.mtislab.celvo.feature.myesim.data.mapper.toPackageInfoCardData
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundle
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus

import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.cards.PackageInfoRow
import com.mtislab.core.designsystem.components.cards.ProductInfoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
import com.mtislab.core.designsystem.components.indicators.CelvoUsageGauge
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoGreen500
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.CelvoRose500Alpha15
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EsimDetailsRoot(
    onBackClick: () -> Unit,
    onTopUpClick: (String) -> Unit,
    viewModel: EsimDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is EsimDetailsEvent.NavigateToTopUp -> onTopUpClick(event.esimId)
                is EsimDetailsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is EsimDetailsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    EsimDetailsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                EsimDetailsAction.BackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsimDetailsScreen(
    state: EsimDetailsState,
    snackbarHostState: SnackbarHostState,
    onAction: (EsimDetailsAction) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(EsimDetailsState.EsimDetailTab.CURRENT) }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(EsimDetailsAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                CelvoDetailHeader(
                    title = state.esim?.userLabel ?: "eSIM",
                    onBackClick = { onAction(EsimDetailsAction.BackClick) },
                    modifier = Modifier.fillMaxWidth()
                )



                if (state.isLoading && state.bundleInfo == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else if (state.bundleInfo != null) {
                    // Tab Switcher
                    CelvoTabSwitcher(
                        options = EsimDetailsState.EsimDetailTab.entries.map { it.title },
                        selectedIndex = EsimDetailsState.EsimDetailTab.entries.indexOf(selectedTab),
                        onOptionSelected = { index ->
                            selectedTab = EsimDetailsState.EsimDetailTab.entries[index]
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            //.height(48.dp)
                    )

                    // Content
                    when (selectedTab) {
                        EsimDetailsState.EsimDetailTab.CURRENT -> CurrentBundlesTabContent(
                            activeBundle = state.bundleInfo.activeBundle as BundleDto?,
                            queuedBundles = state.bundleInfo.queuedBundles
                        )
                        EsimDetailsState.EsimDetailTab.HISTORY -> HistoryBundlesTabContent(
                            historyBundles = state.bundleInfo.historyBundles
                        )
                    }
                } else if (state.bundlesError != null) {
                    // Error State
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Failed to load eSIM details",
                            color = MaterialTheme.colorScheme.onPrimary,
                            // style = CelvoTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentBundlesTabContent(
    activeBundle: BundleDto?,
    queuedBundles: List<EsimBundle>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Active Bundle (Hero Section with Gauge)
        item {
            if (activeBundle != null) {
                ProductInfoCard(
                    data = activeBundle.toPackageInfoCardData(),
                    trailingRow = {
                        PackageInfoRow(
                            icon = Res.drawable.ic_validity_period,
                            label = "სტატუსი",
                            value = activeBundle.state,
                            valueColor = MaterialTheme.colorScheme.extended.success,
                        )
                    },
                    bottomContent = {
                        CelvoUsageGauge(
                            usedAmount = activeBundle.usedBytes.toFloat(),
                            totalAmount = activeBundle.initialBytes.toFloat(),
                            unit = "GB",
                            flagUrl = null,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            }

        }

        // 2. Queued Bundles
        if (queuedBundles.isNotEmpty()) {
            items(queuedBundles) { bundle ->
                PackageCard(
                    bundle = bundle,

                )

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Queued: ${bundle.displayName}")
                }
            }
        }
    }
}


@Composable
private fun PackageCard(
    bundle: EsimBundle,

) {
    val colors = MaterialTheme.colorScheme.extended

    CelvoCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),

        //onClick = onDetailsClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {

                CelvoCircleButton(icon = painterResource(Res.drawable.ic_network_broadcast), onClick = {})

                Text(
                    text = "Esim #${bundle.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = EsimStatus.ACTIVE, label = bundle.state)
        }

        Spacer(modifier = Modifier.height(16.dp))


        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EsimInfoRow(
                icon = Res.drawable.ic_network_broadcast,
                label = "პაკეტი: ${bundle.initialBytes}",
                value = bundle.usedBytes.toString(),
                valueColor = colors.textPrimary,
                iconTint = colors.textSecondary
            )

           // EsimCountryFlags(countries = esim.supportedCountries)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.extended.cardBorder
        )

    }
}





@Composable
private fun StatusBadge(
    status: EsimStatus,
    label: String
) {
    val color = MaterialTheme.colorScheme.extended

    val (textColor, backgroundColor) = when (status) {
        EsimStatus.ACTIVE, EsimStatus.ENABLED, EsimStatus.INSTALLED -> Pair(
            color.success,
            CelvoGreen500Alpha15
        )

        EsimStatus.RELEASED, EsimStatus.DOWNLOADED  -> Pair(
            color.textLink,
            CelvoPurple500Alpha15
        )

        else -> Pair(
            color.destructive,
            CelvoRose500Alpha15
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label, // API-დან წამოსული ტექსტი
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = textColor
        )
    }
}

@Composable
private fun EsimInfoRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.extended.textSecondary,
    iconTint: Color = MaterialTheme.colorScheme.extended.textSecondary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.extended.textSecondary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = valueColor
        )
    }
}



@Composable
private fun HistoryBundlesTabContent(
    historyBundles: List<EsimBundle>
) {
    if (historyBundles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No history available",
                //style = CelvoTheme.typography.bodyLarge,
                //color = CelvoTheme.extendedColors.textSecondary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyBundles) { bundle ->
                // TODO:  History Bundle Card კომპონენტესბი
                Text("History: ${bundle.displayName}")
            }
        }
    }
}