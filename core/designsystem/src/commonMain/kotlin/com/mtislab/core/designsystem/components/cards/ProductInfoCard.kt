package com.mtislab.core.designsystem.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_network
import com.celvo.core.designsystem.resources.ic_speed
import com.celvo.core.designsystem.resources.product_info_networks
import com.celvo.core.designsystem.resources.product_info_speed
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.domain.model.PackageInfoCardData
import org.jetbrains.compose.resources.stringResource

/**
 * Unified eSIM package info card — visible to any feature module via core:designsystem.
 *
 * ## Always rendered:
 *  - Header: [PackageInfoCardData.dataAmountDisplay] + validity / [CountryBadge]
 *  - Speed row   "სიჩქარე / 5G·LTE"    (icon from core resources)
 *  - Networks row operator summary      (icon from core resources, tappable)
 *
 * ## Caller-supplied slots:
 *  - [trailingRow]   Required — last info row, screen-specific:
 *      - feature:store   → "შენი მოწყობილობა / eSIM თავსებადი"
 *      - feature:myesim  → "სტატუსი / აქტიური"
 *  - [bottomContent] Optional — e.g. CelvoUsageGauge beneath all rows.
 *
 * Dividers are owned by this component — callers must not add their own.
 *
 * @param data            Screen-agnostic UI model. Build via your feature's mapper.
 * @param onNetworkClick  Callback for the tappable networks row.
 * @param trailingRow     Required slot — last row, unique per screen.
 * @param bottomContent   Optional slot — rendered beneath the trailing row.
 */
@Composable
fun ProductInfoCard(
    data: PackageInfoCardData,
    modifier: Modifier = Modifier,
    onNetworkClick: () -> Unit = {},
    trailingRow: @Composable () -> Unit,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    CelvoCard(modifier = modifier.fillMaxWidth()) {

        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = data.dataAmountDisplay,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                data.validityDisplay?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textSecondary,
                    )
                }
            }
            CountryBadge(
                isoCode = data.isoCode,
                countryName = data.countryName,
                badgeType = data.badgeType,
                region = data.region,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Speed ─────────────────────────────────────────────────────────
        PackageInfoRow(
            icon = Res.drawable.ic_speed,
            label = stringResource(Res.string.product_info_speed),
            value = "5G / LTE",
        )

        PackageInfoDivider()

        // ── Networks ──────────────────────────────────────────────────────
        // Temporarily hidden on checkout — keep the block intact so it can be
        // restored without re-deriving the operator summary string. The
        // trailing PackageInfoDivider below also stays commented so we don't
        // render two consecutive dividers between Speed and the trailing row.
        /*
        val networksValue = if (data.additionalOperatorCount > 0) {
            "${data.primaryOperator}... +${data.additionalOperatorCount}"
        } else {
            data.primaryOperator
        }
        PackageInfoRow(
            icon = Res.drawable.ic_network,
            label = stringResource(Res.string.product_info_networks),
            value = networksValue,
            isClickable = true,
            onClick = onNetworkClick,
        )

        PackageInfoDivider()
        */

        // ── Trailing row (screen-specific) ────────────────────────────────
        trailingRow()

        // ── Optional bottom content ───────────────────────────────────────
        if (bottomContent != null) {
            Spacer(modifier = Modifier.height(24.dp))
            bottomContent()
        }
    }
}