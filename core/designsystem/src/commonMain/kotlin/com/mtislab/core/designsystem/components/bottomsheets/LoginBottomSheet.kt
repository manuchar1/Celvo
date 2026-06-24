package com.mtislab.core.designsystem.components.bottomsheets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_apple_logo
import com.celvo.core.designsystem.resources.ic_google_logo
import com.celvo.core.designsystem.resources.ic_log_in
import com.celvo.core.designsystem.resources.legal_consent_signin
import com.celvo.core.designsystem.resources.legal_privacy_policy
import com.celvo.core.designsystem.resources.legal_terms_of_service
import com.celvo.core.designsystem.resources.login_sheet_apple
import com.celvo.core.designsystem.resources.login_sheet_google
import com.celvo.core.designsystem.resources.login_sheet_subtitle
import com.celvo.core.designsystem.resources.login_sheet_title

import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.legal.LegalConsentText
import com.mtislab.core.designsystem.legal.LegalLink
import com.mtislab.core.designsystem.legal.LegalLinks
import com.mtislab.core.designsystem.theme.CelvoPurple500
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource


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
                text = stringResource(Res.string.login_sheet_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.login_sheet_subtitle),
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
                    text = stringResource(Res.string.login_sheet_apple),
                    leadingIcon = painterResource(Res.drawable.ic_apple_logo),
                    onClick = onAppleClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.extended.cardBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Google (Always visible)
            CelvoButton(
                text = stringResource(Res.string.login_sheet_google),
                leadingIcon = painterResource(Res.drawable.ic_google_logo),
                onClick = onGoogleClick,
                modifier = Modifier.fillMaxWidth(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.extended.cardBorder)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Terms + Privacy footer with inline tappable links (opens in-app browser).
            LegalConsentText(
                template = stringResource(Res.string.legal_consent_signin),
                links = listOf(
                    LegalLink(
                        label = stringResource(Res.string.legal_terms_of_service),
                        url = LegalLinks.TERMS_OF_SERVICE
                    ),
                    LegalLink(
                        label = stringResource(Res.string.legal_privacy_policy),
                        url = LegalLinks.PRIVACY_POLICY
                    ),
                ),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}