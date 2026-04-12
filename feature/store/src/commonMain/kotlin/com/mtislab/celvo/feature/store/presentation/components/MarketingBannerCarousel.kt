package com.mtislab.celvo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/**
 * Carousel that renders both standard and interactive banners.
 *
 * Routing logic uses [MarketingBanner.isInteractive] (derived from `promoCode != null`)
 * to determine which composable is rendered. The `isClaimed` flag on each banner
 * is already merged by the ViewModel — no additional state needed here.
 *
 * @param banners List of banners with claimed state pre-merged by ViewModel.
 * @param onBannerClick Deep link handler for standard (non-interactive) banners.
 * @param onClaimPromo Claim handler for interactive promo banners.
 */
@Composable
fun MarketingBannerCarousel(
    banners: List<MarketingBanner>,
    onBannerClick: (deepLink: String) -> Unit,
    onClaimPromo: (banner: MarketingBanner) -> Unit,
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> banners.getOrNull(page)?.id ?: page.toString() },
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val banner = banners[page]

            if (banner.isInteractive) {
                InteractivePromoBanner(
                    banner = banner,
                    onClaimClick = { onClaimPromo(banner) },
                )
            } else {
                MarketingBannerItem(
                    banner = banner,
                    onBannerClick = onBannerClick,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dots indicator
        if (banners.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(banners.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    }
                    val dotSize = if (isSelected) 8.dp else 6.dp

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(dotSize),
                    )
                }
            }
        }
    }
}