package com.mtislab.celvo.feature.store.presentation.components

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
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.theme.titleXSmall
import org.jetbrains.compose.resources.painterResource

@Composable
fun MarketingBannerItem(
    banner: MarketingBanner,
    onBannerClick: (String) -> Unit
) {


    val titleColor = MaterialTheme.colorScheme.extended.textPrimary
    val descColor = MaterialTheme.colorScheme.extended.textSecondary


    CelvoCard(
        modifier = Modifier.padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(180.dp),
        onClick = { onBannerClick(banner.deepLink) },
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 20.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column {
                        Text(
                            text = banner.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            color = titleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,

                            )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = banner.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = descColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.padding(top = 20.dp))

                    Button(
                        onClick = { onBannerClick(banner.deepLink) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CelvoPurple300,
                            contentColor = CelvoDark900
                        ),
                        elevation = ButtonDefaults.buttonElevation(1.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = banner.ctaText,
                            style = MaterialTheme.typography.titleXSmall,
                            fontSize = 14.sp
                        )
                    }
                }

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
}