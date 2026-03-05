package com.mtislab.celvo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner

@Composable
fun MarketingBannerCarousel(
    banners: List<MarketingBanner>,
    claimedPromoCode: String?, // Passed down from your screen's StateFlow
    onBannerClick: (String) -> Unit, // For deep links (standard banners)
    onClaimCode: (String) -> Unit    // For interactive promo banners
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Horizontal Pager
        HorizontalPager(
            state = pagerState,
            key = { page -> banners.getOrNull(page)?.id ?: page.toString() },
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val banner = banners[page]

            // 🎯 ROUTING LOGIC
            if (banner.promoCode != null) {
                // If the backend attached a promo code, it's an interactive reward banner
                val isClaimed = banner.promoCode == claimedPromoCode

                MascotPromoBanner(
                    banner = banner,
                    isClaimed = isClaimed,
                    onClaimClicked = { onClaimCode(banner.promoCode) }
                )
            } else {
                // Otherwise, fallback to your standard MarketingBannerItem
                MarketingBannerItem(
                    banner = banner,
                    onBannerClick = onBannerClick
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Dots Indicator
        if (banners.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    }
                    val size = if (isSelected) 8.dp else 6.dp

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(size)
                    )
                }
            }
        }
    }
}