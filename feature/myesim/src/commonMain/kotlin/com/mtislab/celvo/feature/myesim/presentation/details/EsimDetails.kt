package com.mtislab.celvo.feature.myesim.presentation.details

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.myesim.generated.resources.Res
import celvo.feature.myesim.generated.resources.esim_details_days_left
import celvo.feature.myesim.generated.resources.esim_details_load_error
import celvo.feature.myesim.generated.resources.esim_details_no_active
import celvo.feature.myesim.generated.resources.esim_details_remaining
import celvo.feature.myesim.generated.resources.ic_network_broadcast
import celvo.feature.myesim.generated.resources.ic_validity_period
import celvo.feature.myesim.generated.resources.myesim_error_title
import celvo.feature.myesim.generated.resources.myesim_retry
import coil3.compose.AsyncImage
import com.mtislab.celvo.feature.myesim.data.mapper.toBundleDisplay
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundle
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.core.designsystem.components.bundles.ThrottleDisclosure
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
import com.mtislab.core.designsystem.components.placeholders.CelvoErrorState
import com.mtislab.core.designsystem.theme.CelvoGreen500
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.CelvoRose500Alpha15
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.getRegionIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

                CelvoDetailHeader(
                    title = state.esim?.userLabel ?: "eSIM",
                    onBackClick = { onAction(EsimDetailsAction.BackClick) },
                    modifier = Modifier.fillMaxWidth()
                )

                when {
                    state.isLoading && state.bundleInfo == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    state.bundlesError != null && state.bundleInfo == null -> {
                        CelvoErrorState(
                            error = state.bundlesError,
                            onRetry = { onAction(EsimDetailsAction.Refresh) },
                            serverErrorTitle = stringResource(Res.string.myesim_error_title),
                            serverErrorMessage = stringResource(Res.string.esim_details_load_error),
                            serverErrorActionLabel = stringResource(Res.string.myesim_retry)
                        )
                    }

                    state.bundleInfo != null -> {
                        AllBundlesContent(bundleInfo = state.bundleInfo)
                    }
                }
            }
        }
    }
}


// ─── All Bundles ───────────────────────────────────────────────────────────────

@Composable
private fun AllBundlesContent(bundleInfo: EsimBundleInfo) {
    val allBundles = bundleInfo.currentBundles + bundleInfo.historyBundles

    if (allBundles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.esim_details_no_active),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allBundles, key = { it.assignmentId.raw }) { bundle ->
                BundleCard(bundle = bundle)
            }
        }
    }
}


// ─── Unified Bundle Card ───────────────────────────────────────────────────────

@Composable
private fun BundleCard(bundle: EsimBundle) {
    val colors = MaterialTheme.colorScheme.extended
    val display = bundle.toBundleDisplay()
    val isRealCountryCode =
        bundle.countryCode.length == 2 && bundle.countryCode.all { it.isLetter() }

    CelvoCard {
        // ── Header: Flag + Country Name + Status Badge ──
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
                // Flag for countries (remote) or region icon from local
                if (isRealCountryCode) {
                    AsyncImage(
                        model = bundle.flagUrl.ifEmpty {
                            "https://flagcdn.com/h120/${bundle.countryCode.lowercase()}.png"
                        },
                        contentDescription = bundle.countryName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    Image(
                        painter = painterResource(getRegionIcon(bundle.countryCode)),
                        contentDescription = bundle.countryName,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Column {
                    Text(
                        text = bundle.countryName.ifEmpty { bundle.displayName },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(bundle = bundle)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Data usage progress (active metered bundles only) ──
        if (bundle.isActive && !bundle.isUnlimited) {
            DataUsageBar(bundle = bundle)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Info rows ──
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Data amount — ∞ Unlimited for unlimited, remaining/total for active
            // metered, or just the initial allotment for queued/history.
            InfoRow(
                icon = Res.drawable.ic_network_broadcast,
                label = when {
                    bundle.isUnlimited -> display.displayName
                    bundle.isActive -> "${bundle.remainingFormatted} / ${bundle.initialFormatted}"
                    else -> bundle.initialFormatted
                },
                iconTint = colors.textSecondary
            )

            // Validity / Duration
            val validityText = when {
                bundle.isActive && bundle.remainingDays != null -> {
                    "${bundle.remainingDays} ${stringResource(Res.string.esim_details_days_left)}"
                }

                bundle.duration != null -> bundle.duration
                else -> null
            }
            if (validityText != null) {
                InfoRow(
                    icon = Res.drawable.ic_validity_period,
                    label = validityText,
                    iconTint = colors.textSecondary
                )
            }
        }

        // ── Fair-use disclosure for unlimited bundles ──
        display.throttle?.let { terms ->
            Spacer(modifier = Modifier.height(12.dp))
            ThrottleDisclosure(terms = terms)
        }
    }
}


// ─── Data Usage Bar ────────────────────────────────────────────────────────────

@Composable
private fun DataUsageBar(bundle: EsimBundle) {
    val colors = MaterialTheme.colorScheme.extended
    val progress = if (bundle.initialBytes > 0) {
        (bundle.remainingBytes.toFloat() / bundle.initialBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val barColor = when {
        progress > 0.5f -> CelvoGreen500
        progress > 0.2f -> colors.warning
        else -> colors.destructive
    }

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${bundle.remainingFormatted} ${stringResource(Res.string.esim_details_remaining)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                ),
                color = colors.textSecondary
            )
            Text(
                text = bundle.initialFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                ),
                color = colors.textSecondary
            )
        }
    }
}


// ─── Status Badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(bundle: EsimBundle) {
    val colors = MaterialTheme.colorScheme.extended

    val (textColor, backgroundColor) = when {
        bundle.isActive -> Pair(colors.success, CelvoGreen500Alpha15)
        bundle.isPending -> Pair(colors.textLink, CelvoPurple500Alpha15)
        else -> Pair(colors.destructive, CelvoRose500Alpha15)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = bundle.displayStatus,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = textColor
        )
    }
}


// ─── Info Row ──────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
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
    }
}
