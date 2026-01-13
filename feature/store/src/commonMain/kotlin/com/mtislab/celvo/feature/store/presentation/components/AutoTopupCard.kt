package com.mtislab.celvo.feature.store.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.ic_auto
import com.mtislab.celvo.feature.store.presentation.checkout.TopupOption
import com.mtislab.celvo.feature.store.presentation.checkout.TopupOptions
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource

@Composable
fun AutoTopupCard(
    isEnabled: Boolean,
    selectedOption: TopupOption,
    onToggle: (Boolean) -> Unit,
    onSelectOption: (TopupOption) -> Unit
) {
    // Colors
    val successColor = MaterialTheme.colorScheme.extended.success
    val purpleLinkColor = Color(0xFFA79FEA) // Custom Visual Color


    // Border Logic: თუ ჩართულია - მწვანე, თუ არა - CelvoCard-ის default (გამჭვირვალე/ნაცრისფერი)
    val customBorder = if (isEnabled) {
        BorderStroke(1.dp, successColor.copy(alpha = 0.5f))
    } else {
        null
    }

    CelvoCard(
        border = customBorder,
        // Padding-ს ვთიშავთ კარკასზე, რომ Glow Effect კუთხეში მივიდეს
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // ✨ GLOW EFFECT (მხოლოდ მაშინ, როცა ჩართულია)
         //   if (isEnabled) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-40).dp, y = (-40).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    successColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
          //  }

            // 📦 MAIN CONTENT
            Column(
                modifier = Modifier.padding(16.dp) // Padding-ს ვაბრუნებთ აქ
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_auto),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ავტომატური შევსება",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                            color = MaterialTheme.colorScheme.extended.textPrimary
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Description Text ---
                val descriptionText = buildAnnotatedString {
                    append("ინტერნეტის ამოწურვის შემთხვევაში,\nშევსება მოხდება ავტომატურად. ")
                    withStyle(style = SpanStyle(color = purpleLinkColor, fontWeight = FontWeight.Bold)) {
                        append("გაიგე მეტი")
                    }
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.extended.textSecondary
                    )
                )

                // --- Expandable Content ---
                AnimatedVisibility(
                    visible = isEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "აირჩიე რაოდენობა",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.extended.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TopupOptions.forEach { option ->
                                TopupChip(
                                    option = option,
                                    isSelected = option.id == selectedOption.id,
                                    onClick = { onSelectOption(option) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "თითო შევსება: ${selectedOption.label} - ${selectedOption.price} ${selectedOption.currency}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.extended.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopupChip(
    option: TopupOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Custom Active Color (Purple)
    val activeColor = Color(0xFFA79FEA)
    val inactiveBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)

    val bgColor = if (isSelected) activeColor else inactiveBg
    val textColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.extended.textPrimary

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}