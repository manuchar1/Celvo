package com.mtislab.celvo.feature.store.presentation.packages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_fire
import celvo.feature.store.generated.resources.ic_left_arrow
import coil3.compose.AsyncImage
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.presentation.checkout.CheckoutScreenRoot
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.switchers.CelvoTabSwitcher
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PackagesScreenRoot(
    isoCode: String,
    countryName: String,
    onBackClick: () -> Unit,
    viewModel: PackagesScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(isoCode) {
        viewModel.onAction(PackagesScreenAction.LoadPackages(isoCode))
    }

    PackagesScreenScreen(
        state = state,
        isoCode = isoCode,
        countryName = countryName,
        onAction = { action ->
            when (action) {
                is PackagesScreenAction.BackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class) // ეს დაამატე, რადგან ModalBottomSheet ამას ითხოვს
@Composable
fun PackagesScreenScreen(
    state: PackagesScreenState,
    isoCode: String,
    countryName: String,
    onAction: (PackagesScreenAction) -> Unit,
) {
    // [NEW] 1. ვქმნით ლოკალურ State-ს არჩეული პაკეტისთვის
    var selectedPackage by remember { mutableStateOf<EsimPackage?>(null) }

    val flagUrl = remember(isoCode) {
        "https://flagcdn.com/h240/${isoCode.lowercase()}.png"
    }

    // [NEW] 2. Sheet State, რომ სრულად გაიშალოს
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PackagesTopBar(
                countryName = countryName,
                flagUrl = flagUrl,
                onBackClick = { onAction(PackagesScreenAction.BackClick) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Switcher ---
            if (state.showCategorySwitcher) {
                val selectedIndex = when (state.selectedCategory) {
                    PackageCategory.DATA -> 0
                    PackageCategory.UNLIMITED -> 1
                }

                CelvoTabSwitcher(
                    options = listOf("გიგაბაიტები", "ულიმიტო"),
                    selectedIndex = selectedIndex,
                    onOptionSelected = { index ->
                        val newCategory =
                            if (index == 0) PackageCategory.DATA else PackageCategory.UNLIMITED
                        onAction(PackagesScreenAction.SelectCategory(newCategory))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Title ---
            Text(
                text = "აირჩიე პაკეტი",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // --- List ---
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.extended.success)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(state.filteredPackages) { pkg ->
                        PackageCard(
                            pkg = pkg,
                            onClick = {
                                // [CHANGED] 3. ნავიგაციის ნაცვლად, ვანიჭებთ მნიშვნელობას selectedPackage-ს
                                selectedPackage = pkg
                            }
                        )
                    }
                }
            }
        }

        // [NEW] 4. ModalBottomSheet-ის ინტეგრაცია
        if (selectedPackage != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedPackage = null },
                sheetState = sheetState,

                contentColor = MaterialTheme.colorScheme.extended.textPrimary,
                //modifier = Modifier.fillMaxHeight(0.85f)
            ) {
                CheckoutScreenRoot(
                    pkg = selectedPackage!!,
                    countryName = countryName,
                    onClose = { selectedPackage = null }
                )
                // პატარა დაშორება ბოლოში, რომ Gesture Bar-მა არ დაფაროს
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PackageCard(
    pkg: EsimPackage,
    onClick: () -> Unit
) {
    val isPopular = pkg.isBestValue

    // Colors
    val successColor = MaterialTheme.colorScheme.extended.success
    // ბორდერის ფერი ჩვეულებრივ რეჟიმში ავტომატურად მოდის CelvoCard-იდან,
    // ჩვენ მხოლოდ Popular რეჟიმის ფერი გვჭირდება.

    // Border Logic: თუ Popular-ია, ვაწვდით მწვანე ბორდერს, თუ არა - null-ს (და CelvoCard თავისას იზამს)
    val customBorder = if (isPopular) {
        BorderStroke(1.dp, successColor)
    } else {
        null // გამოიყენებს CelvoCard-ის default 0.5dp transparent/gray ბორდერს
    }

    CelvoCard(
        onClick = onClick,
        border = customBorder,
        // 👇 მნიშვნელოვანია: Padding-ს ვთიშავთ კარკასზე, რათა Glow კუთხეში მივიდეს
        contentPadding = PaddingValues(0.dp)
    ) {
        // Box გვჭირდება Glow-ს და Content-ის დასალაგებლად
        Box(modifier = Modifier.fillMaxSize()) {

            // ✨ GLOW EFFECT (მხოლოდ პოპულარულზე)
            // ეს ახლა ზუსტად კუთხეში დაჯდება, რადგან contentPadding 0-ია
            if (isPopular) {
                BestValueGlow(
                    color = successColor,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // 📦 CONTENT CONTAINER
            // აქ ვამატებთ Padding-ს, რადგან კარკასზე გავთიშეთ
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // --- Popular Badge ---
                if (isPopular) {
                    PopularBadge()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // თუ popular არაა, Spacer-ი არ გვჭირდება, განლაგება ავტომატურად დაჯდება

                // --- Main Info Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Data & Validity
                    Column {
                        Text(
                            text = pkg.dataAmountDisplay,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.extended.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pkg.validityDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.extended.textSecondary
                        )
                    }

                    // Right Side: Price & Discount
                    PriceSection(pkg)
                }
            }
        }
    }
}

@Composable
fun BestValueGlow(
    color: Color,
    modifier: Modifier = Modifier
) {
    // Figma Glow Simulation using Radial Gradient
    // ეს არის ყველაზე ეფექტური გზა Compose-ში Blur-ის სიმულაციისთვის
    Box(
        modifier = modifier
            .size(120.dp)
            .offset(x = 30.dp, y = (-30).dp) // გადავწიოთ კუთხეში
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.32f), // ცენტრი (Figma Opacity)
                        Color.Transparent // ნაპირები
                    ),
                    radius = 160f
                )
            )
    )
}

@Composable
fun PriceSection(pkg: EsimPackage) {
    // Logic: ფასდაკლება გვაქვს მხოლოდ მაშინ, თუ originalPrice არსებობს და მეტია ახლანდელ ფასზე
    val hasDiscount = pkg.originalPrice != null && pkg.originalPrice > pkg.price

    Row(verticalAlignment = Alignment.CenterVertically) {
        // --- Discount Badge ---
        if (hasDiscount) {
            val percentText = pkg.discountPercent?.let { "-$it%" } ?: ""
            if (percentText.isNotEmpty()) {
                DiscountBadge(text = percentText)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            // Current Price
            Text(
                text = "${pkg.price} ${pkg.currency}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )

            // Old Price (Strikethrough)
            if (hasDiscount) {
                val oldPrice = pkg.originalPrice
                // ვაფორმატებთ ფასს, რომ ზედმეტი ნულები არ გამოჩნდეს
                val formattedOld = if (oldPrice != null && oldPrice % 1.0 == 0.0) {
                    "${oldPrice.toInt()}"
                } else {
                    "$oldPrice"
                }

                Text(
                    text = "$formattedOld ${pkg.currency}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }
    }
}

@Composable
fun PopularBadge() {
    val successColor = MaterialTheme.colorScheme.extended.success
    val onSurface = MaterialTheme.colorScheme.onSurface

    BadgeContainer(
        color = successColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, successColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_fire),
                contentDescription = null,
                tint = onSurface,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "პოპულარული",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = onSurface
            )
        }
    }
}

@Composable
fun DiscountBadge(text: String) {
    val warningColor = MaterialTheme.colorScheme.extended.warning

    BadgeContainer(
        color = warningColor,
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = Color.Black, // ყვითელზე შავი ტექსტი
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BadgeContainer(
    color: Color,
    shape: Shape,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(color, shape)
            .border(border ?: BorderStroke(0.dp, Color.Transparent), shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesTopBar(
    countryName: String,
    flagUrl: String,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = flagUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = countryName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_left_arrow),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.extended.textPrimary
                )
            }
        }
    )
}