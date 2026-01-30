package com.mtislab.celvo.feature.profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding // 👈 ეს დავამატეთ
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import celvo.feature.profile.generated.resources.Res
import celvo.feature.profile.generated.resources.ic_message_text_circle
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileHeader(
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding() // სისტემური აიქონების ადგილი
            .height(68.dp) // ✅ ფიქსირებული სიმაღლე ჰედერს სტაბილურობას მატებს (ფიგმაში ~60-70dp ჩანს)
            .padding(horizontal = 20.dp), // ✅ 16dp -> 20dp (უფრო ჰაეროვანია)
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // ✅ ტექსტი და ღილაკი ზუსტად ცენტრში!
    ) {
        Text(
            text = "პროფილი",
            style = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = (-0.5).sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Spacer აღარ გვჭირდება, რადგან SpaceBetween და CenterVertically საქმეს აკეთებს,
        // მაგრამ უსაფრთხოებისთვის (რომ ტექსტი არ მიეწებოს) დავტოვოთ მცირე დაშორება.
        Spacer(modifier = Modifier.widthIn(min = 16.dp))

        CelvoCircleButton(
            onClick = onSupportClick,
            icon = painterResource(Res.drawable.ic_message_text_circle),
        )
    }
}