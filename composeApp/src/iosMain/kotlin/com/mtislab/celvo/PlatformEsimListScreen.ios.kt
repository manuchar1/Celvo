package com.mtislab.celvo

import androidx.compose.runtime.Composable
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.presentation.list.MyEsimListRoot

/**
 * iOS implementation of PlatformEsimListScreen.
 *
 * On iOS, eSIM installation is handled entirely by CTCellularPlanProvisioning.
 * The system shows its own consent dialog and returns the result directly
 * in the completion handler — no resolution callback needed.
 */
@Composable
actual fun PlatformEsimListScreen(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit
) {
    MyEsimListRoot(
        onEsimClick = onEsimClick,
        onAddEsimClick = onAddEsimClick,
        onResolutionRequired = null
    )
}