package com.mtislab.celvo.feature.myesim.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.myesim.generated.resources.Res
import celvo.feature.myesim.generated.resources.ic_add
import celvo.feature.myesim.generated.resources.ic_arrow_right
import celvo.feature.myesim.generated.resources.ic_auto_renewal
import celvo.feature.myesim.generated.resources.ic_network_broadcast
import celvo.feature.myesim.generated.resources.ic_validity_period
import coil3.compose.AsyncImage
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoGreen300
import com.mtislab.core.designsystem.theme.CelvoGreen500
import com.mtislab.core.designsystem.theme.CelvoGreen500Alpha15
import com.mtislab.core.designsystem.theme.CelvoGreen700
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.CelvoPurple500Alpha15
import com.mtislab.core.designsystem.theme.CelvoRose500
import com.mtislab.core.designsystem.theme.CelvoRose500Alpha15
import com.mtislab.core.designsystem.theme.CelvoRose700
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel



/**
 * Simplified callback: UI only needs to launch the PendingIntent and notify when done.
 * No result interpretation needed — Samsung always returns RESULT_CANCELED,
 * and the real result arrives via broadcast to AndroidEsimInstaller.
 */
typealias OnResolutionRequired = (
    resolutionData: Any,
    onLaunched: () -> Unit
) -> Unit

@Composable
fun MyEsimListRoot(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit,
    onResolutionRequired: OnResolutionRequired? = null,
    viewModel: MyEsimListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Launch resolution when needed
    LaunchedEffect(state.resolutionRequired) {
        state.resolutionRequired?.let { resolutionData ->
            onResolutionRequired?.invoke(
                resolutionData,
                { viewModel.onAction(MyEsimListAction.ResolutionLaunched) }
            )
        }
    }

    MyEsimListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is MyEsimListAction.EsimClick -> onEsimClick(action.esim)
                is MyEsimListAction.DetailsClick -> onEsimClick(action.esim)
                is MyEsimListAction.ActivateClick -> viewModel.onAction(action)
                MyEsimListAction.AddEsimClick -> onAddEsimClick()
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
    val isDarkTheme = MaterialTheme.colorScheme.background == CelvoDark900

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
            state.isLoading -> {
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
                EmptyEsimState(
                    onAddClick = { onAction(MyEsimListAction.AddEsimClick) },
                )
            }

            state.showContent -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(
                        items = state.filteredEsims,
                        key = { it.id }
                    ) { esim ->
                        EsimCard(
                            esim = esim,
                            // ✅ დამატებული: isInstalling პარამეტრი loading state-ისთვის
                            isInstalling = state.isEsimInstalling(esim.id),
                            onTopUpClick = { onAction(MyEsimListAction.TopUpClick(esim)) },
                            onDetailsClick = { onAction(MyEsimListAction.DetailsClick(esim)) },
                            onActivateClick = { onAction(MyEsimListAction.ActivateClick(esim)) },
                            isDarkTheme = isDarkTheme
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
    isDarkTheme: Boolean
) {
    val colors = MaterialTheme.colorScheme.extended

    CelvoCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        // დეტალებზე გადასვლა ყოველთვის მუშაობს მთელ ქარდზე
        onClick = onDetailsClick
    ) {
        // --- HEADER (იგივე) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CountryFlag(
                    flagUrl = esim.country.flagUrl,
                    countryCode = esim.country.code,
                    size = 40
                )
                Text(
                    text = esim.userLabel ?: esim.country.name, // აქ შეიძლება country.name ცარიელი იყოს
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
            }
            // StatusBadge იყენებს esim.status-ს, რომელიც პირდაპირ profileStatus-დან მოდის
            StatusBadge(status = esim.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- INFO ROWS (მხოლოდ თუ მონაცემი გვაქვს) ---
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // თუ null-ია, ეს ხაზი არ გამოჩნდება
            esim.dataUsage?.let { usage ->
                EsimInfoRow(
                    icon = Res.drawable.ic_network_broadcast,
                    label = "პაკეტი:",
                    value = "${usage.usedFormatted} / ${usage.totalFormatted}",
                    valueColor = colors.textPrimary,
                    iconTint = colors.textSecondary
                )
            }

            // თუ null-ია, ეს ხაზი არ გამოჩნდება
            esim.validity?.let { validity ->
                EsimInfoRow(
                    icon = Res.drawable.ic_validity_period,
                    label = "დარჩენილი დრო:",
                    value = "${validity.remainingDays} დღე",
                    valueColor = colors.textPrimary,
                    iconTint = colors.textSecondary
                )
            }

            // Auto renewal ინფო ახალ API-ში არ ჩანს, ამიტომ აქ ან null შემოწმება უნდა,
            // ან დროებით ამოღება/დამალვა.
            // რადგან მოდელში არ შეგვიცვლია autoRenewalEnabled (Boolean),
            // ის false იქნება დეფოლტად Repository-ში, ამიტომ "გამორთული" ეწერება.
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.extended.cardBorder
        )

        // --- BUTTON LOGIC ---
        // 2. მხოლოდ მაშინ ვაჩენთ ღილაკს თუ დაბრუნდა "primaryAction": "INSTALL"
        if (esim.primaryAction == "INSTALL") {
            CelvoButton(
                text = if (isInstalling) "მიმდინარეობს..." else "ინსტალაცია", // ან "გააქტიურება"
                onClick = onActivateClick,
                enabled = !isInstalling,
                containerColor = CelvoGreen500,
                contentColor = CelvoDark900,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // სხვა შემთხვევაში - ან არაფერი, ან დეტალების ღილაკი.
            // შენი მოთხოვნა: "მხოლოდ მაშინ ვაჩენთ ღილაკს..."
            // თუ აქ "Details" ღილაკიც არ გინდა, მაშინ ეს ბლოკი ცარიელი უნდა იყოს.
            // მაგრამ არსებულ კოდში გქონდა TopUp/Details.
            // თუ გინდა ძველი ლოგიკა დატოვო როგორც fallback:

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // დეტალების ღილაკი ყოველთვის კარგია რომ იყოს
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
private fun CountryFlag(
    flagUrl: String,
    countryCode: String,
    size: Int = 40
) {
    AsyncImage(
        model = flagUrl.ifEmpty { "https://flagcdn.com/h120/${countryCode.lowercase()}.png" },
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
        contentScale = ContentScale.Crop
    )
}


@Composable
private fun StatusBadge(
    status: EsimStatus,
) {
    val color = MaterialTheme.colorScheme.extended

    val (text, textColor, backgroundColor) = when {
        status == EsimStatus.EXPIRED -> Triple(
            "ვადაგასული",
            color.destructive,
            CelvoRose500Alpha15
        )

        status == EsimStatus.ACTIVE -> Triple(
            "აქტიური",
            color.success,
            CelvoGreen500Alpha15
        )

        status == EsimStatus.PROVISIONED -> Triple(
            "არააქტიური",
            color.textLink,
            CelvoPurple500Alpha15
        )

        else -> Triple(
            "ვადაგასული",
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
            text = text,
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

@Composable
private fun EmptyEsimState(
    onAddClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme.extended

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ჯერ არ გაქვს eSIM",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textPrimary
            )

            Text(
                text = "დაამატე პირველი eSIM და დაიწყე მოგზაურობა",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSans
                ),
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            CelvoButton(
                text = "დამატება",
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                icon = painterResource(Res.drawable.ic_add)
            )
        }
    }
}