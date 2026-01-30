package com.mtislab.celvo.feature.store.presentation.checkout

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_card
import celvo.feature.store.generated.resources.ic_info_circle
import celvo.feature.store.generated.resources.ic_network
import celvo.feature.store.generated.resources.ic_payment_method_apple
import celvo.feature.store.generated.resources.ic_payment_method_mastercard
import celvo.feature.store.generated.resources.ic_phone
import celvo.feature.store.generated.resources.ic_promo_code
import celvo.feature.store.generated.resources.ic_speed
import coil3.compose.AsyncImage
import com.celvo.core.designsystem.resources.ic_left_arrow
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.presentation.components.AutoTopupCard
import com.mtislab.core.data.platform
import com.mtislab.core.designsystem.components.auth.rememberGoogleAuthProvider
import com.mtislab.core.designsystem.components.bottomsheets.LoginBottomSheet
import com.mtislab.core.designsystem.components.buttons.CelvoActionIconButton
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.CelvoPurple500
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.utils.rememberBrowserOpener
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import com.celvo.core.designsystem.resources.Res as CoreRes

@Composable
fun CheckoutScreenRoot(
    countryName: String,
    onClose: () -> Unit,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ 1. Google Provider და Browser Opener
    val googleAuthProvider = rememberGoogleAuthProvider()
    val openBrowser = rememberBrowserOpener()

    // ✅ 2. პლატფორმის შემოწმება
    val isAndroid = remember { platform().contains("Android", ignoreCase = true) }

    // ✅ 3. Event Listener (URL-ის გასახსნელად)
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OpenWebUrl -> {
                    // 🚀 ვხსნით ლინკს (Chrome Tabs / Safari VC)
                    openBrowser(event.url)
                    // არ ვხურავთ ეკრანს, ველოდებით DeepLink-ს
                }
                is CheckoutEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
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
            isAndroid = isAndroid,
            snackbarHostState = snackbarHostState,
            onClose = onClose,
            onAction = { action ->
                when (action) {
                    is CheckoutAction.LoginWithGoogle -> {
                        val provider = if (isAndroid) googleAuthProvider else null
                        viewModel.onAction(CheckoutAction.LoginWithGoogle(provider))
                    }
                    else -> viewModel.onAction(action)
                }
            }
        )
    }
}

@Composable
private fun CheckoutContent(
    state: CheckoutState,
    countryName: String,
    isAndroid: Boolean,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
    onAction: (CheckoutAction) -> Unit
) {
    val pkg = state.packageDetails ?: return
    val scrollState = rememberScrollState()

    // ✅ Login Bottom Sheet
    if (state.showLoginSheet) {
        LoginBottomSheet(
            onDismiss = { onAction(CheckoutAction.DismissLoginSheet) },
            onGoogleClick = { onAction(CheckoutAction.LoginWithGoogle()) }, // Provider handled above
            onAppleClick = { onAction(CheckoutAction.LoginWithApple) },
            isIos = !isAndroid
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeGestures)
                    .background(MaterialTheme.colorScheme.background) // ფონი რომ კონტენტი არ ჩანდეს
            ) {
                // ფასის გამოთვლა (პაკეტი + ტოპ-აპი თუ არჩეულია)
                val totalAmount = pkg.price // აქ შეგიძლიათ დაამატოთ logic თუ სხვა რამეს ამატებთ

                CelvoButton(
                    text = "გადახდა • ${formatPrice(totalAmount)} ${pkg.currency}",
                    onClick = { onAction(CheckoutAction.PayClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 16.dp), // Safe Area-სთვის
                    isLoading = state.isLoading
                )
            }
        }
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
            ProductInfoCard(pkg, countryName)

            Spacer(modifier = Modifier.height(16.dp))

            AutoTopupCard(
                isEnabled = state.isAutoTopupEnabled,
                selectedOption = state.selectedTopupOption,
                onToggle = { enabled -> onAction(CheckoutAction.ToggleAutoTopup(enabled)) },
                onSelectOption = { option -> onAction(CheckoutAction.SelectTopupOption(option)) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            ValidityStartInfoBox()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "გადახდის მეთოდი",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            // ჯერჯერობით მხოლოდ ბარათი (ან Apple Pay მომავალში)
            PaymentMethodsSection(
                selectedMethod = "NEW_CARD",
                onSelect = { /* Logic for selection */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PromoCodeCard(
                promoCode = "",
                onClick = { /* Open Dialog */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OrderSummary(
                price = pkg.price,
                discount = 0.0, // Backend logic needed
                promoDiscount = 0.0,
                currencySymbol = pkg.currency
            )

            // ადგილი Footer-ისთვის რომ არ გადაეფაროს
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ==========================================
// Sub-Components
// ==========================================

@Composable
fun PaymentMethodsSection(selectedMethod: String, onSelect: (String) -> Unit) {
    CelvoCard(contentPadding = PaddingValues(0.dp)) {
        // Apple Pay (მხოლოდ iOS-ზე ან თუ მხარდაჭერილია)
        /*
        PaymentMethodItem(
            icon = Res.drawable.ic_payment_method_apple,
            title = "Apple Pay",
            isSelected = selectedMethod == "APPLE_PAY",
            onClick = { onSelect("APPLE_PAY") },
            isApplePay = true
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        */

        PaymentMethodItem(
            icon = Res.drawable.ic_card,
            title = "საბანკო ბარათი",
            subtitle = "Visa, Mastercard, Amex",
            isSelected = true, // ჯერჯერობით მხოლოდ ეს გვაქვს
            onClick = { onSelect("NEW_CARD") }
        )
    }
}

@Composable
fun PaymentMethodItem(
    icon: DrawableResource,
    title: String,
    subtitle: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    isApplePay: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 24.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
                .clip(RoundedCornerShape(6.dp))
                .background(if (isApplePay) Color.Black else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(width = 24.dp, height = 14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textPrimary)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textSecondary)
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
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CelvoPurple500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(Res.drawable.ic_promo_code), contentDescription = null, tint = Color.Unspecified)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "პრომოკოდი", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.extended.textSecondary)
                Text(
                    text = promoCode?.takeIf { it.isNotEmpty() } ?: "შეიყვანეთ კოდი",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (!promoCode.isNullOrEmpty()) MaterialTheme.colorScheme.extended.textPrimary else MaterialTheme.colorScheme.extended.textTertiary
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.extended.textSecondary)
        }
    }
}

@Composable
fun OrderSummary(price: Double, discount: Double, promoDiscount: Double, currencySymbol: String) {
    val total = (price - discount - promoDiscount).coerceAtLeast(0.0)
    Column(modifier = Modifier.fillMaxWidth()) {
        SummaryRow("პაკეტის საფასური", "${formatPrice(price)}$currencySymbol", isTotal = false)
        if (discount > 0) SummaryRow("ფასდაკლება", "-${formatPrice(discount)}$currencySymbol", valueColor = CelvoPurple500, isTotal = false)
        if (promoDiscount > 0) SummaryRow("პრომოკოდი", "-${formatPrice(promoDiscount)}$currencySymbol", valueColor = MaterialTheme.colorScheme.extended.destructive, isTotal = false)
        Spacer(modifier = Modifier.height(8.dp))
        SummaryRow("ჯამური თანხა", "${formatPrice(total)}$currencySymbol", isTotal = true)
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color? = null, isTotal: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isTotal) MaterialTheme.colorScheme.extended.textPrimary else MaterialTheme.colorScheme.extended.textSecondary
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp) else MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.extended.textPrimary
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

@Composable
fun CheckoutHeader(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        CelvoActionIconButton(
            icon = vectorResource(CoreRes.drawable.ic_left_arrow),
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "ყიდვა",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.extended.textPrimary,
            style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp)
        )
    }
}

@Composable
fun ProductInfoCard(pkg: EsimPackage, countryName: String) {
    CelvoCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = pkg.dataAmountDisplay,
                    style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = pkg.validityDisplay, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textSecondary)
            }
            CountryBadge(isoCode = pkg.isoCode, countryName = countryName)
        }
        Spacer(modifier = Modifier.height(24.dp))
        CheckoutInfoRow(icon = Res.drawable.ic_speed, label = "სიჩქარე", value = "5G / LTE")
        CheckoutDivider()
        val firstOperator = pkg.operators.firstOrNull()?.name ?: "Network"
        val extraCount = (pkg.operators.size - 1).coerceAtLeast(0)
        CheckoutInfoRow(icon = Res.drawable.ic_network, label = "ქსელები", value = if (extraCount > 0) "$firstOperator... +$extraCount" else firstOperator, isClickable = true)
        CheckoutDivider()
        CheckoutInfoRow(icon = Res.drawable.ic_phone, label = "შენი მოწყობილობა", value = "eSIM თავსებადი")
    }
}

@Composable
fun ValidityStartInfoBox() {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(CircleShape).background(CelvoPurple500.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(painter = painterResource(Res.drawable.ic_info_circle), contentDescription = null, tint = CelvoPurple500)
        Text(text = "მოქმედების ვადა დაიწყება eSIM-ის ქსელთან დაკავშირების მომენტიდან.", style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = MaterialTheme.colorScheme.extended.textPrimary)
    }
}

@Composable
fun CheckoutDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
}

@Composable
fun CheckoutInfoRow(icon: DrawableResource, label: String, value: String, isClickable: Boolean = false, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(40.dp), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.extended.textSecondary))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textSecondary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.extended.textPrimary)
        }
        if (isClickable) Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Details", tint = MaterialTheme.colorScheme.extended.textSecondary, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun CountryBadge(isoCode: String, countryName: String) {
    val flagUrl = remember(isoCode) { "https://flagcdn.com/h240/${isoCode.lowercase()}.png" }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.extended.textPrimary.copy(alpha = 0.05f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = flagUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(20.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = countryName, style = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp), color = MaterialTheme.colorScheme.extended.textPrimary)
        }
    }
}

