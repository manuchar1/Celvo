package com.mtislab.core.designsystem.components.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.designsystem.theme.titleXSmall
import com.mtislab.core.designsystem.utils.getRegionIcon
import org.jetbrains.compose.resources.painterResource

@Composable
fun CelvoRegionItem(
    name: String,
    priceDisplay: String,
    id: String,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    discountText: String? = null,
    onClick: () -> Unit
) {
    val extendedColors = MaterialTheme.colorScheme.extended

    CelvoCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Square card
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (discountText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            color = extendedColors.warning,
                            shape = RoundedCornerShape(bottomEnd = 16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = discountText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CelvoDark900
                        )
                    )
                }
            }

            // 2. Main Content (Centered)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {


                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val iconResource = getRegionIcon(id)
                    Image(
                        painter = painterResource(iconResource),
                        contentDescription = name,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleXSmall,
                    fontWeight = FontWeight.Medium,
                    color = extendedColors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1 // Ensure it doesn't break layout
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                Text(
                    text = priceDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}