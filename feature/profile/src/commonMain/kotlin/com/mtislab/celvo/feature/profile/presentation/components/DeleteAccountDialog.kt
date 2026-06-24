package com.mtislab.celvo.feature.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.celvo.core.designsystem.resources.Res as CoreRes
import com.celvo.core.designsystem.resources.legal_privacy_delete_notice
import com.celvo.core.designsystem.resources.legal_privacy_policy
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.legal.LegalConsentText
import com.mtislab.core.designsystem.legal.LegalLink
import com.mtislab.core.designsystem.legal.LegalLinks
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteAccountDialog(
    title: String,
    warning: String,
    consequence: String,
    cancelText: String,
    confirmText: String,
    inlineErrorText: String?,
    isLoading: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        // Back press maps to Cancel — never accidentally trigger Delete.
        onDismissRequest = { if (!isLoading) onCancel() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                WarningIcon()

                Spacer(modifier = Modifier.height(16.dp))

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

                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = consequence,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Policy (data-deletion section) so the user can review exactly
                // what data is removed before confirming. Opens in an in-app browser.
                LegalConsentText(
                    template = stringResource(CoreRes.string.legal_privacy_delete_notice),
                    links = listOf(
                        LegalLink(
                            label = stringResource(CoreRes.string.legal_privacy_policy),
                            url = LegalLinks.PRIVACY_POLICY
                        ),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (inlineErrorText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = inlineErrorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel — visually primary (filled). Sits on top to make the
                    // safer action the easier tap; mirrors the existing logout
                    // dialog where the more prominent button is on top.
                    CelvoButton(
                        text = cancelText,
                        onClick = onCancel,
                        enabled = !isLoading,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = CelvoDark900,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Delete — destructive. Lower visual prominence (subtle fill,
                    // red text). Loading spinner inline keeps the dialog open.
                    CelvoButton(
                        text = confirmText,
                        onClick = onConfirm,
                        isLoading = isLoading,
                        enabled = !isLoading,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningIcon() {
    val tint = MaterialTheme.colorScheme.error
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f))
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = tint
        )
    }
}
