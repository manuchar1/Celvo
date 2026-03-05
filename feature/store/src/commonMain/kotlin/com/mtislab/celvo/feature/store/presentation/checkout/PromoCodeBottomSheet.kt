package com.mtislab.celvo.feature.store.presentation.checkout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.enter_code
import celvo.feature.store.generated.resources.have_promo_or_referral_code
import celvo.feature.store.generated.resources.promo_code
import com.celvo.core.designsystem.resources.ic_cancel
import com.mtislab.core.designsystem.components.buttons.CelvoActionIconButton
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.inputs.CelvoTextField
import com.mtislab.core.designsystem.theme.CelvoDark900
import com.mtislab.core.designsystem.theme.CelvoPurple300
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import com.celvo.core.designsystem.resources.Res as CoreRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCodeBottomSheet(
    code: String,
    isValidating: Boolean,
    errorMessage: String?,
    onCodeChanged: (String) -> Unit,
    onApplyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                CelvoActionIconButton(
                    icon = vectorResource(CoreRes.drawable.ic_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = stringResource(Res.string.promo_code),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.have_promo_or_referral_code),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))


            CelvoTextField(
                value = code,
                onValueChange = onCodeChanged,
                placeholder = stringResource(Res.string.enter_code),
                forceUppercase = true,
                isError = errorMessage != null,
                errorMessage = errorMessage,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))


            CelvoButton(
                text = "Claim this Promo",
                onClick = onApplyClick,
                modifier = Modifier.fillMaxWidth(),
                isLoading = isValidating,
                enabled = code.isNotBlank() && !isValidating,
                containerColor = CelvoPurple300,
                contentColor = CelvoDark900
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}