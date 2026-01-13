package com.mtislab.celvo.feature.store.presentation.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_layer_circle

import coil3.compose.AsyncImage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import org.jetbrains.compose.resources.painterResource

@Composable
fun MarketingBannerItem(
    banner: MarketingBanner,
    onBannerClick: (String) -> Unit
) {
    // 🎨 ფერები თემიდან
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val descColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    // ღილაკის ფერები: Primary ფერი 10% გამჭვირვალობით ფონზე
    val buttonContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val buttonContentColor = MaterialTheme.colorScheme.primary

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable { onBannerClick(banner.deepLink) }
    ) {
        // 1. Background Pattern (Left Top)
        Image(
            painter = painterResource(Res.drawable.ic_layer_circle),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-10).dp, y = (-10).dp)
                .width(100.dp)
                .fillMaxHeight(0.6f),
            contentScale = ContentScale.FillBounds,
            alpha = 0.5f
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Left Side: Texts & Button ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, top = 24.dp, bottom = 24.dp), // ოდნავ გავზარდეთ პადინგი
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    // Title
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, // Bold-ის ნაცვლად SemiBold
                        fontSize = 16.sp,
                        lineHeight = 22.sp, // ✅ გასწორდა: 140% Line Height
                        color = titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = banner.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = descColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onBannerClick(banner.deepLink) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainerColor,
                        contentColor = buttonContentColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                ) {
                    Text(
                        text = banner.ctaText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // --- Right Side: Mascot ---
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(121.dp)
                        .height(134.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}