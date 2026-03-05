package com.mtislab.core.designsystem.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_log_in

import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.theme.CelvoPurple500
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.vectorResource
// import celvo.core.designsystem.generated.resources.ic_google (დაამატე ეს რესურსი)
// import celvo.core.designsystem.generated.resources.ic_apple (დაამატე ეს რესურსი)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    onDismiss: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    isIos: Boolean = false // 👈 ამას მივანიჭებთ მნიშვნელობას გამოძახებისას
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface, // ან სპეციფიური ფერი დიზაინიდან
        dragHandle = {
            // Custom Drag Handle თუ გვინდა, ან დეფოლტს დავტოვებთ
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp), // Safe Area ქვემოთ
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Icon Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.extended.inputBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_log_in),
                    contentDescription = null,
                    tint = CelvoPurple500,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Texts
            Text(
                text = "ანგარიშზე შესვლა",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "აირჩიეთ შესვლის მეთოდი",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans
                ),
                color = MaterialTheme.colorScheme.extended.textSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Buttons Logic (Platform Specific) 🍏🤖

            if (isIos) {
                // iOS: Apple First
                CelvoButton(
                    text = "Apple-ით შესვლა",
                    onClick = onAppleClick,
                    // icon = painterResource(Res.drawable.ic_apple), // 👈 დარწმუნდი რომ გაქვს
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.extended.inputBackground
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Google (Always visible)
            CelvoButton(
                text = "Google-ით შესვლა",
                onClick = onGoogleClick,
                // icon = painterResource(Res.drawable.ic_google), // 👈 დარწმუნდი რომ გაქვს
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.extended.inputBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Terms Footer
            Text(
                text = "გაგრძელებით თქვენ ეთანხმებით წესებს და პირობებს.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.extended.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}