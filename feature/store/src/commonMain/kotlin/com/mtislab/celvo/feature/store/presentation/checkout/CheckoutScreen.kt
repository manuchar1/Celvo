package com.mtislab.celvo.feature.store.presentation.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_network
import celvo.feature.store.generated.resources.ic_phone
import celvo.feature.store.generated.resources.ic_speed
import coil3.compose.AsyncImage
import com.celvo.core.designsystem.resources.ic_cancel
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.presentation.components.AutoTopupCard
import com.mtislab.core.designsystem.components.buttons.CelvoActionIconButton
import com.mtislab.core.designsystem.components.cards.CelvoCard // ახალი იმპორტი
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutScreenRoot(
    pkg: EsimPackage,
    countryName: String,
    onClose: () -> Unit,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(pkg) {
        viewModel.onAction(CheckoutAction.Init(pkg))
    }

    CheckoutContent(
        state = state,
        countryName = countryName,
        onClose = onClose,
        onAction = viewModel::onAction
    )
}

@Composable
private fun CheckoutContent(
    state: CheckoutState,
    countryName: String,
    onClose: () -> Unit,
    onAction: (CheckoutAction) -> Unit
) {
    val pkg = state.packageDetails ?: return

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CheckoutHeader(onClose = onClose)

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Refactored Card
            ProductInfoCard(pkg, countryName)

            Spacer(modifier = Modifier.height(16.dp))

            AutoTopupCard(
                isEnabled = state.isAutoTopupEnabled,
                selectedOption = state.selectedTopupOption,
                onToggle = { enabled ->
                    onAction(CheckoutAction.ToggleAutoTopup(enabled))
                },
                onSelectOption = { option ->
                    onAction(CheckoutAction.SelectTopupOption(option))
                }
            )
        }
    }
}


@Composable
fun CheckoutHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {

        CelvoActionIconButton(
            icon = vectorResource(com.celvo.core.designsystem.resources.Res.drawable.ic_cancel),
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )

        Text(
            text = "ყიდვა",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.extended.textPrimary,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )
    }
}

@Composable
fun ProductInfoCard(
    pkg: EsimPackage,
    countryName: String
) {
    // --- Senior Note: ---
    // ყველა სტილისტიკა (Shape, Background, Border, Shadow) ახლა იმართება CelvoCard-ში.
    // კოდი გახდა დეკლარაციული და მარტივად წასაკითხი.

    CelvoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        // შიდა Padding (16.dp) უკვე აქვს CelvoCard-ს, ამიტომ აქ აღარ ვწერთ.

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // "2 GB"
                Text(
                    text = pkg.dataAmountDisplay,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                // "15 დღით"
                Text(
                    text = pkg.validityDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }

            CountryBadge(
                isoCode = pkg.isoCode,
                countryName = countryName
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CheckoutInfoRow(
            icon = Res.drawable.ic_speed,
            label = "სიჩქარე",
            value = "5G / LTE"
        )

        CheckoutDivider()

        val firstOperator = pkg.operators.firstOrNull()?.name ?: "Network"
        val extraCount = (pkg.operators.size - 1).coerceAtLeast(0)
        val networksText = if (extraCount > 0) "$firstOperator... +$extraCount" else firstOperator

        CheckoutInfoRow(
            icon = Res.drawable.ic_network,
            label = "ქსელები",
            value = networksText,
            isClickable = true,
            onClick = { /* TODO: Open networks */ }
        )

        CheckoutDivider()

        CheckoutInfoRow(
            icon = Res.drawable.ic_phone,
            label = "შენი მოწყობილობა",
            value = "eSIM თავსებადი"
        )
    }
}

@Composable
fun CheckoutDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

@Composable
fun CheckoutInfoRow(
    icon: DrawableResource,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    val iconTint = MaterialTheme.colorScheme.extended.textSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(iconTint)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
        }

        if (isClickable) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.extended.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CountryBadge(
    isoCode: String,
    countryName: String
) {
    val flagUrl = remember(isoCode) { "https://flagcdn.com/h240/${isoCode.lowercase()}.png" }
    val badgeBg = MaterialTheme.colorScheme.extended.textPrimary.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(badgeBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = flagUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = countryName,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
        }
    }
}