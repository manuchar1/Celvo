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
 * Carousel of informational marketing banners.
 *
 * @param banners Banners to display.
 * @param onBannerClick Deep link handler invoked when a banner is tapped.
 */
@Composable
fun MarketingBannerCarousel(
    banners: List<MarketingBanner>,
    onBannerClick: (deepLink: String) -> Unit,
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
            MarketingBannerItem(
                banner = banners[page],
                onBannerClick = onBannerClick,
            )
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
