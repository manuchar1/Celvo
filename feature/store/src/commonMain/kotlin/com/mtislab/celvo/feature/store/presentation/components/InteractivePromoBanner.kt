package com.mtislab.celvo.feature.store.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_layer_circle
import coil3.compose.AsyncImage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.theme.titleXSmall
import org.jetbrains.compose.resources.painterResource

/**
 * Interactive promo banner with claimed/unclaimed states.
 *
 * Uses the [MarketingBanner.displayTitle] and [MarketingBanner.displayDescription]
 * computed properties which automatically swap text when [MarketingBanner.isClaimed].
 *
 * The CTA button is disabled in claimed state and shows a success indicator.
 *
 * @param banner The banner data (with merged `isClaimed` flag from ViewModel).
 * @param onClaimClick Called when user taps the CTA in unclaimed state.
 */
@Composable
fun InteractivePromoBanner(
    banner: MarketingBanner,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleColor = MaterialTheme.colorScheme.extended.textPrimary
    val descColor = MaterialTheme.colorScheme.extended.textSecondary
    val successColor = MaterialTheme.colorScheme.extended.success

    CelvoCard(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp),
        onClick = if (!banner.isClaimed) ({ onClaimClick() }) else null,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative circle
            Image(
                painter = painterResource(Res.drawable.ic_layer_circle),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .width(100.dp)
                    .fillMaxHeight(0.6f),
                contentScale = ContentScale.FillBounds,
                alpha = 0.5f,
            )

            Row(modifier = Modifier.fillMaxSize()) {
                // ── Left: Text + CTA ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 20.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                ) {
                    // Animated title/description swap
                    AnimatedContent(
                        targetState = banner.isClaimed,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                        },
                        label = "banner_text_swap",
                    ) { isClaimed ->
                        Column {
                            Text(
                                text = if (isClaimed) banner.displayTitle else banner.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color = if (isClaimed) successColor else titleColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isClaimed) banner.displayDescription else banner.description,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = descColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(top = 20.dp))

                    // CTA Button — disabled when claimed
                    Button(
                        onClick = onClaimClick,
                        enabled = !banner.isClaimed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (banner.isClaimed) {
                                successColor.copy(alpha = 0.15f)
                            } else {
                                CelvoPurple300
                            },
                            contentColor = if (banner.isClaimed) successColor else CelvoDark900,
                            disabledContainerColor = successColor.copy(alpha = 0.15f),
                            disabledContentColor = successColor,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (banner.isClaimed) 0.dp else 1.dp,
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(
                            text = if (banner.isClaimed) "✓ Claimed" else banner.ctaText,
                            style = MaterialTheme.typography.titleXSmall,
                            fontSize = 14.sp,
                        )
                    }
                }

                // ── Right: Image ──
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .width(121.dp)
                            .height(134.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}