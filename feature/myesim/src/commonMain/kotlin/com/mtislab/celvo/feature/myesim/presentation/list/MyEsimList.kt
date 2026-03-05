package com.mtislab.celvo.feature.myesim.presentation.list

import CelvoPlaceholder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.myesim.generated.resources.Res
import celvo.feature.myesim.generated.resources.ic_add
import celvo.feature.myesim.generated.resources.ic_arrow_right
import celvo.feature.myesim.generated.resources.ic_network_broadcast
import celvo.feature.myesim.generated.resources.mascot_no_e_sim_added
import coil3.compose.AsyncImage
import com.celvo.core.designsystem.resources.ic_sim_card
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoGreen300
import com.mtislab.core.designsystem.theme.CelvoGreen500
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.CelvoRose500Alpha15
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun MyEsimListRoot(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit,
    onTopUpClick: (UserEsim) -> Unit,
    viewModel: MyEsimListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is MyEsimListUiEvent.OpenUrl -> {
                try {
                    uriHandler.openUri(event.url)
                } catch (_: Exception) {
                }
            }
        }
    }

    // ✅ This is now the SINGLE source of truth for triggering a load.
    // It fires on first composition (initial load) AND every time the user
    // returns to this screen (e.g. after visiting the platform eSIM setup screen).
    // The isLoading guard in the ViewModel prevents concurrent duplicate calls.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(MyEsimListAction.Refresh)
    }

    MyEsimListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is MyEsimListAction.EsimClick -> onEsimClick(action.esim)
                is MyEsimListAction.DetailsClick -> onEsimClick(action.esim)
                is MyEsimListAction.TopUpClick -> onTopUpClick(action.esim)
                is MyEsimListAction.AddEsimClick -> onAddEsimClick()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun MyEsimListScreen(
    state: MyEsimListState,
    onAction: (MyEsimListAction) -> Unit,
) {
    val colors = MaterialTheme.colorScheme.extended

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        MyEsimHeader(
            onAddClick = { onAction(MyEsimListAction.AddEsimClick) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            state.isLoading && state.esims.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.showError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "შეცდომა მოხდა",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CelvoButton(
                            text = "სცადე თავიდან",
                            onClick = { onAction(MyEsimListAction.RetryClick) },
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            state.showEmptyState -> {
                CelvoPlaceholder(
                    icon = Res.drawable.mascot_no_e_sim_added,
                    title = "აქ გამოჩნდება შეძენილი Esim",
                    message = "დამატეთ და მართეთ რამდენიმე eSIM სხვადასხვა მოწყობილობაზე მარტივად.",
                    actionLabel = "ახალი Esim",
                    onActionClick = { onAction(MyEsimListAction.AddEsimClick) }
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(
                        items = state.esims,
                        key = { it.id }
                    ) { esim ->
                        EsimCard(
                            esim = esim,
                            isInstalling = state.isInstalling && state.installingEsimId == esim.id,
                            onTopUpClick = { onAction(MyEsimListAction.TopUpClick(esim)) },
                            onDetailsClick = { onAction(MyEsimListAction.DetailsClick(esim)) },
                            onActivateClick = { onAction(MyEsimListAction.ActivateClick(esim)) },
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun MyEsimHeader(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ჩემი E-Sim",
            style = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = (-0.5).sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.extended.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        AddEsimButton(
            onClick = onAddClick
        )
    }
}

@Composable
private fun AddEsimButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = CelvoDark900
    val containerColor = CelvoPurple300

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier.height(36.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "დამატება",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}


@Composable
private fun EsimCard(
    esim: UserEsim,
    isInstalling: Boolean,
    onTopUpClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onActivateClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.extended

    CelvoCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        onClick = onDetailsClick
    ) {
        // --- HEADER ---
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

                CelvoCircleButton(icon = painterResource(CoreRes.drawable.ic_sim_card), onClick = {})

                Column {
                    Text(
                        text = "Esim #${esim.iccid.takeLast(4)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

              /*      Text(
                        text = "${esim.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.success,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )*/
                }


            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = esim.status, label = esim.statusDisplayName)
        }

        Spacer(modifier = Modifier.height(16.dp))


        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EsimInfoRow(
                icon = Res.drawable.ic_network_broadcast,
                label = "შეძენილი პაკეტები:",
                value = esim.totalBundles.toString(),
                valueColor = colors.textPrimary,
                iconTint = colors.textSecondary
            )

            EsimCountryFlags(countries = esim.supportedCountries)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.extended.cardBorder
        )

        // --- BUTTON LOGIC ---
        if (esim.primaryAction == "INSTALL") {
            CelvoButton(
                text = "ინსტალაცია",
                onClick = onActivateClick,
                enabled = !isInstalling,
                containerColor = CelvoGreen500,
                contentColor = CelvoDark900,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top-Up ღილაკი მხოლოდ მაშინ თუ გვინდა გამოჩნდეს.
                // თუ დიზაინში ორივე გინდა, შეგიძლია TopUpActionButton-იც დაამატო, როგორც ადრე გქონდა.
                // აქ ორივე მოთავსებულია Row-ში:
                TopUpActionButton(
                    text = "პაკეტები",
                    onClick = onTopUpClick,
                    modifier = Modifier.weight(1f)
                )

                DetailsActionButton(
                    text = "დეტალები",
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
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
            text = label,
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
private fun TopUpActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.extended.cardBackground
    val contentColor = MaterialTheme.colorScheme.extended.textPrimary

    CelvoCard(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = CircleShape,
        containerColor = backgroundColor,
        border = null,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp // ოდნავ პატარა ფონტი ორ ღილაკზე დასატევად
                ),
                color = contentColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CelvoGreen300),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CelvoDark900
                )
            }
        }
    }
}

@Composable
private fun DetailsActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.extended.cardBackground
    val contentColor = MaterialTheme.colorScheme.extended.textPrimary

    CelvoCard(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = CircleShape,
        containerColor = backgroundColor,
        border = null,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp // ოდნავ პატარა ფონტი ორ ღილაკზე დასატევად
                ),
                color = contentColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CelvoPurple300),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = CelvoDark900
                )
            }
        }
    }
}



private const val MAX_VISIBLE_FLAGS = 4

@Composable
private fun EsimCountryFlags(
    countries: List<com.mtislab.celvo.feature.myesim.domain.model.EsimCountry>,
    modifier: Modifier = Modifier
) {
    if (countries.isEmpty()) return

    val colors = MaterialTheme.colorScheme.extended

    val hasOverflow = countries.size > MAX_VISIBLE_FLAGS
    val visibleFlags = if (hasOverflow) countries.take(MAX_VISIBLE_FLAGS - 1) else countries
    val overflowCount = countries.size - visibleFlags.size

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleFlags.forEach { country ->
            FlagCircle {
                AsyncImage(
                    model = country.flagUrl.ifEmpty {
                        "https://flagcdn.com/h120/${country.isoCode.lowercase()}.png"
                    },
                    contentDescription = country.isoCode,
                    modifier = Modifier
                        .fillMaxSize()
                        //.clip(CircleShape)
                    ,
                    contentScale = ContentScale.Crop

                )
            }
        }

        if (hasOverflow) {
            FlagCircle {
                Text(
                    text = "+$overflowCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = colors.textPrimary
                )
            }
        }
    }
}

/**
 * The circular card container — architecturally identical to CelvoCircleButton's
 * internal CelvoCard(shape = CircleShape), but accepts arbitrary content (AsyncImage, Text)
 * instead of being locked to a Painter icon.
 */
@Composable
private fun FlagCircle(
    size: Dp = 36.dp,
    content: @Composable BoxScope.() -> Unit
) {
    CelvoCard(
        onClick = {},
        enabled = false,
        modifier = Modifier.size(size),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}