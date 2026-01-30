package com.mtislab.core.designsystem.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.theme.extended

@Composable
fun CelvoDialog(
    title: String,
    description: String,
    icon: Painter,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmContainerColor: Color = Color(0xFFE59CA8), // ვარდისფერი ფიგმადან
    confirmContentColor: Color = Color.Black
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // ✅ აქ ვიყენებთ Surface-ს და არა CelvoCard-ს
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // გვერდითი დაშორებები ეკრანიდან
            shape = RoundedCornerShape(32.dp), // ფიგმას სტილის მომრგვალება
            color = MaterialTheme.colorScheme.surfaceContainerHigh, // მუქი ფონი (Dark900 ან მსგავსი)
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp) // შიდა პადინგი
            ) {
                // 1. Icon
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Buttons (ვერტიკალურად, ფიგმას მიხედვით)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Confirm Button (Pink)
                    CelvoButton(
                        text = confirmText,
                        onClick = onConfirm,
                        containerColor = confirmContainerColor, // ვარდისფერი
                        contentColor = confirmContentColor,     // შავი ტექსტი
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dismiss Button (Transparent/Dark)
                    CelvoButton(
                        text = dismissText,
                        onClick = onDismiss,
                        // ფონი იგივე რაც დიალოგის, ან ოდნავ განსხვავებული
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}