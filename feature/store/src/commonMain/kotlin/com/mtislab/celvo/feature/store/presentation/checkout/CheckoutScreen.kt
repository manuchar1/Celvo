package com.mtislab.celvo.feature.store.presentation.checkout

import CheckoutState
import PaymentMethod
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.action_continue
import celvo.feature.store.generated.resources.card
import celvo.feature.store.generated.resources.checkout_discount
import celvo.feature.store.generated.resources.checkout_esim_compatible
import celvo.feature.store.generated.resources.checkout_package_price
import celvo.feature.store.generated.resources.checkout_payment_method
import celvo.feature.store.generated.resources.checkout_promo_code_label
import celvo.feature.store.generated.resources.checkout_title
import celvo.feature.store.generated.resources.checkout_total
import celvo.feature.store.generated.resources.checkout_validity_info
import celvo.feature.store.generated.resources.checkout_your_device
import celvo.feature.store.generated.resources.enter_code
import celvo.feature.store.generated.resources.ic_card
import celvo.feature.store.generated.resources.ic_credit_card
import celvo.feature.store.generated.resources.ic_info_circle
import celvo.feature.store.generated.resources.ic_phone
import celvo.feature.store.generated.resources.ic_promo_code
import com.celvo.core.designsystem.resources.ic_apple_logo
import com.celvo.core.designsystem.resources.ic_google_pay_mark
import com.celvo.core.designsystem.resources.ic_left_arrow
import com.celvo.core.designsystem.resources.legal_consent_checkout
import com.celvo.core.designsystem.resources.legal_terms_of_service
import com.mtislab.celvo.feature.store.data.mapper.toPackageInfoCardData
import com.mtislab.celvo.feature.store.domain.model.WalletType
import com.mtislab.core.data.platform
import com.mtislab.core.designsystem.components.auth.rememberAppleAuthProvider
import com.mtislab.core.designsystem.components.auth.rememberGoogleAuthProvider
import com.mtislab.core.designsystem.components.bottomsheets.LoginBottomSheet
import com.mtislab.core.designsystem.components.buttons.CelvoActionIconButton
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.cards.PackageInfoRow
import com.mtislab.core.designsystem.components.cards.ProductInfoCard
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationData
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationType
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.components.payment.NativePayButton
import com.mtislab.core.designsystem.components.payment.rememberNativePayLauncher
import com.mtislab.core.designsystem.legal.LegalConsentText
import com.mtislab.core.designsystem.legal.LegalLink
import com.mtislab.core.designsystem.legal.LegalLinks
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.CelvoPurple500
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.rememberBrowserOpener
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun CheckoutScreenRoot(
    countryName: String,
    type: String,
    region: String,
    onClose: () -> Unit,
    onNavigateToPaymentResult: (isSuccess: Boolean, orderId: String?) -> Unit,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationState = LocalCelvoNotification.current

    val googleAuthProvider = rememberGoogleAuthProvider()
    val appleAuthProvider = rememberAppleAuthProvider()
    val openBrowser = rememberBrowserOpener()

    val isAndroid = remember { platform().contains("Android", ignoreCase = true) }

    // NEW: Native wallet payment launcher (Google Pay on Android, Apple Pay on iOS)
    val nativePayLauncher = rememberNativePayLauncher(
        onTokenReceived = { token ->
            val walletType = if (isAndroid) WalletType.GOOGLE_PAY else WalletType.APPLE_PAY
            viewModel.onAction(CheckoutAction.WalletTokenReceived(token, walletType))
        },
        onCancelled = {
            viewModel.onAction(CheckoutAction.WalletPaymentCancelled)
        },
        onError = { error ->
            viewModel.onAction(CheckoutAction.WalletPaymentFailed(error))
        }
    )

    // One-shot event collector
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OpenWebUrl -> {
                    openBrowser(event.url)
                }
                is CheckoutEvent.ShowError -> {
                    notificationState.show(
                        CelvoNotificationData(
                            message = event.message.asStringAsync(),
                            type = CelvoNotificationType.Error
                        )
                    )
                }
                is CheckoutEvent.NavigateToPaymentResult -> {
                    onNavigateToPaymentResult(event.isSuccess, event.orderId)
                }
                //  Launch native wallet payment sheet
                is CheckoutEvent.LaunchNativeWalletPayment -> {
                    nativePayLauncher.launch(
                        amountCents = event.amountCents,
                        currencyCode = event.currencyCode,
                        merchantName = "Celvo"
                    )
                }
            }
        }
    }

    if (state.packageDetails == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        CheckoutContent(
            state = state,
            countryName = countryName,
            type = type,
            region = region,
            isAndroid = isAndroid,
            onClose = onClose,
            onAction = { action ->
                when (action) {
                    is CheckoutAction.LoginWithGoogle -> {
                        val provider = if (isAndroid) googleAuthProvider else null
                        viewModel.onAction(CheckoutAction.LoginWithGoogle(provider))
                    }
                    is CheckoutAction.LoginWithApple -> {
                        // Native Apple provider on iOS; null on Android routes
                        // the ViewModel to the web fallback.
                        val provider = if (!isAndroid) appleAuthProvider else null
                        viewModel.onAction(CheckoutAction.LoginWithApple(provider))
                    }
                    else -> viewModel.onAction(action)
                }
            }
        )
    }
}

// =============================================================================
// Content (stateless composable driven entirely by CheckoutState)
// =============================================================================

@Composable
private fun CheckoutContent(
    state: CheckoutState,
    countryName: String,
    type: String,
    region: String,
    isAndroid: Boolean,
    onClose: () -> Unit,
    onAction: (CheckoutAction) -> Unit
) {
    val pkg = state.packageDetails ?: return
    val scrollState = rememberScrollState()

    // Login Bottom Sheet
    if (state.showLoginSheet) {
        LoginBottomSheet(
            onDismiss = { onAction(CheckoutAction.DismissLoginSheet) },
            onGoogleClick = { onAction(CheckoutAction.LoginWithGoogle()) },
            onAppleClick = { onAction(CheckoutAction.LoginWithApple()) },
            isIos = !isAndroid
        )
    }



    // Promo Code Bottom Sheet
    if (state.promo.showSheet) {
        PromoCodeBottomSheet(
            code = state.promo.code,
            isValidating = state.promo.isValidating,
            errorMessage = state.promo.errorMessage?.asString(),
            onCodeChanged = { onAction(CheckoutAction.PromoCodeChanged(it)) },
            onApplyClick = { onAction(CheckoutAction.ApplyPromoCode) },
            onDismiss = { onAction(CheckoutAction.DismissPromoSheet) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // Errors surface via the global CelvoNotificationHost (top banner),
        // not a local Snackbar — consistent with the rest of the app.
        // NOTE: No bottomBar. The payment button lives inside the scrollable Column.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))


            CheckoutHeader(onClose = onClose)


            ProductInfoCard(
                data = pkg.toPackageInfoCardData(
                    countryName = countryName,
                    type = type,
                    region = region
                ),
                trailingRow = {
                    PackageInfoRow(
                        icon = Res.drawable.ic_phone,
                        label = stringResource(Res.string.checkout_your_device),
                        value = stringResource(Res.string.checkout_esim_compatible),
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Auto Top-Up (commented out for future use) ---
            /*
            AutoTopupCard(
                isEnabled = state.isAutoTopupEnabled,
                selectedOption = state.selectedTopupOption,
                onToggle = { enabled -> onAction(CheckoutAction.ToggleAutoTopup(enabled)) },
                onSelectOption = { option -> onAction(CheckoutAction.SelectTopupOption(option)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            */

            // --- Validity Info ---
            ValidityStartInfoBox()
            Spacer(modifier = Modifier.height(24.dp))

            // --- Payment Method Selection ---
            Text(
                text = stringResource(Res.string.checkout_payment_method),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            PaymentMethodsSection(
                selectedMethod = state.selectedPaymentMethod,
                isWalletAvailable = state.isWalletAvailable,
                isAndroid = isAndroid,
                onSelect = { method -> onAction(CheckoutAction.SelectPaymentMethod(method)) }
            )

            Spacer(modifier = Modifier.height(16.dp))


            PromoCodeCard(
                promoCode = state.promo.appliedCodeDisplay,
                onClick = { onAction(CheckoutAction.OpenPromoSheet) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Order Summary ---
            // --- Order Summary ---
            OrderSummary(
                price = pkg.price,
                discount = 0.0, // Backend package-level discount (future)
                promoDiscount = state.promoDiscount,
                currencySymbol = pkg.currency
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Digital-goods purchase consent (EU consumer law). Links the Terms and
            // states the withdrawal-right waiver mirroring Terms §9. Sits directly
            // above the Pay button so it is seen before purchase.
            LegalConsentText(
                template = stringResource(CoreRes.string.legal_consent_checkout),
                links = listOf(
                    LegalLink(
                        label = stringResource(CoreRes.string.legal_terms_of_service),
                        url = LegalLinks.TERMS_OF_SERVICE
                    ),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // --- Dynamic Payment Button (non-sticky, end of scroll) ---
            PaymentButton(
                selectedMethod = state.selectedPaymentMethod,
                isWalletAvailable = state.isWalletAvailable,
                isAndroid = isAndroid,
                totalAmount = state.effectivePrice,
                currency = pkg.currency,
                isLoading = state.isLoading,
                onPayClick = { onAction(CheckoutAction.PayClicked) }
            )

            // Bottom safe-area spacer so the button never collides with system nav
            Spacer(
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                    )
                    .height(16.dp)
            )
        }
    }
}

// =============================================================================
// Dynamic Payment Button
// =============================================================================

/**
 * Renders a SINGLE primary button whose label changes based on the selected payment method.
 *
 * - [PaymentMethod.NATIVE_WALLET] + available -> "Pay with Google Pay" / "Pay with Apple Pay"
 * - [PaymentMethod.CARD] (or wallet unavailable) -> Continue (localized via action_continue)
 */
/**
 * Renders either the branded native wallet button (Google Pay / Apple Pay)
 * or a CelvoButton for card payments.
 *
 * Google and Apple REQUIRE their official branded buttons.
 * CelvoButton is only used for the CARD payment method.
 */
@Composable
private fun PaymentButton(
    selectedMethod: PaymentMethod,
    isWalletAvailable: Boolean,
    isAndroid: Boolean,
    totalAmount: Double,
    currency: String,
    isLoading: Boolean,
    onPayClick: () -> Unit
) {
    val useWallet = selectedMethod == PaymentMethod.NATIVE_WALLET && isWalletAvailable

    if (useWallet) {
        // Official branded Google Pay / Apple Pay button
        NativePayButton(
            onClick = onPayClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    } else {
        // Card payments — same primary purple style as the "Claim promo" button.
        CelvoButton(
            text = stringResource(Res.string.action_continue),
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = isLoading,
            containerColor = CelvoPurple300,
            contentColor = CelvoDark900
        )
    }
}





// =============================================================================
// Payment Methods Section
// =============================================================================

@Composable
fun PaymentMethodsSection(
    selectedMethod: PaymentMethod,
    isWalletAvailable: Boolean,
    isAndroid: Boolean,
    onSelect: (PaymentMethod) -> Unit
) {
    CelvoCard(contentPadding = PaddingValues(0.dp)) {

        // --- Native Wallet (Google Pay / Apple Pay) ---
        if (isWalletAvailable) {
            PaymentMethodItem(
                // Official Google Pay mark (developers.google.com brand assets) —
                // it ships with its own white pill + contour, so render it unframed.
                icon = if (isAndroid) CoreRes.drawable.ic_google_pay_mark else CoreRes.drawable.ic_apple_logo,
                title = if (isAndroid) "Google Pay" else "Apple Pay",
                subtitle = null,
                isSelected = selectedMethod == PaymentMethod.NATIVE_WALLET,
                onClick = { onSelect(PaymentMethod.NATIVE_WALLET) },
                framed = !isAndroid,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // --- Card ---
        PaymentMethodItem(
            icon = Res.drawable.ic_credit_card,
            title = stringResource(Res.string.card),
            subtitle = "Visa, Mastercard, Amex",
            isSelected = selectedMethod == PaymentMethod.CARD,
            onClick = { onSelect(PaymentMethod.CARD) }
        )
    }
}

// =============================================================================
// Sub-Components (unchanged signatures, cleaned up)
// =============================================================================

@Composable
fun PaymentMethodItem(
    icon: DrawableResource,
    title: String,
    subtitle: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    framed: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (framed) {
            // Same badge treatment as the official Google Pay mark above:
            // white stadium pill + #747775 contour (Google's mark stroke color),
            // fixed colors in both themes so the two rows always match.
            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 18.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF747775),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF3C4043)),
                    modifier = Modifier.size(width = 16.dp, height = 11.dp)
                )
            }
        } else {
            // Payment marks (e.g. Google Pay) come with their own contour.
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(width = 34.dp, height = 18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.extended.success,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun PromoCodeCard(promoCode: String?, onClick: () -> Unit) {
    CelvoCard(onClick = onClick, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CelvoPurple500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_promo_code),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.checkout_promo_code_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
                Text(
                    text = promoCode?.takeIf { it.isNotEmpty() }
                        ?: stringResource(Res.string.enter_code),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (!promoCode.isNullOrEmpty()) {
                        MaterialTheme.colorScheme.extended.textPrimary
                    } else {
                        MaterialTheme.colorScheme.extended.textTertiary
                    }
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.extended.textSecondary
            )
        }
    }
}

@Composable
fun OrderSummary(price: Double, discount: Double, promoDiscount: Double, currencySymbol: String) {
    val total = (price - discount - promoDiscount).coerceAtLeast(0.0)
    Column(modifier = Modifier.fillMaxWidth()) {
        SummaryRow(
            label = stringResource(Res.string.checkout_package_price),
            value = "$currencySymbol ${formatPrice(price)}",
            isTotal = false
        )
        if (discount > 0) {
            SummaryRow(
                label = stringResource(Res.string.checkout_discount),
                value = "-$currencySymbol ${formatPrice(discount)}",
                valueColor = CelvoPurple500,
                isTotal = false
            )
        }
        if (promoDiscount > 0) {
            SummaryRow(
                label = stringResource(Res.string.checkout_promo_code_label),
                value = "-$currencySymbol ${formatPrice(promoDiscount)}",
                valueColor = MaterialTheme.colorScheme.extended.destructive,
                isTotal = false
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SummaryRow(
            label = stringResource(Res.string.checkout_total),
            value = "$currencySymbol ${formatPrice(total)}",
            isTotal = true
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color? = null, isTotal: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            color = if (isTotal) MaterialTheme.colorScheme.extended.textPrimary
            else MaterialTheme.colorScheme.extended.textSecondary
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
            else MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}

@Composable
fun CheckoutHeader(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        CelvoActionIconButton(
            icon = vectorResource(CoreRes.drawable.ic_left_arrow),
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = stringResource(Res.string.checkout_title),
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
fun ValidityStartInfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(CelvoPurple500.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_info_circle),
            contentDescription = null,
            tint = CelvoPurple500
        )
        Text(
            text = stringResource(Res.string.checkout_validity_info),
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}

private fun formatPrice(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    val str = rounded.toString()
    return if (str.contains(".")) {
        val parts = str.split(".")
        if (parts[1].length == 1) "${str}0" else str
    } else {
        "$str.00"
    }
}