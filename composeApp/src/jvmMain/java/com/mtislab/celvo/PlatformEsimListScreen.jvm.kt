package com.mtislab.celvo

import androidx.compose.runtime.Composable
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim

@Composable
actual fun PlatformEsimListScreen(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit
) {
}