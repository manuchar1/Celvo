package com.mtislab.celvo

import androidx.compose.runtime.Composable
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim

/**
 * Platform-specific eSIM List Screen.
 *
 * On Android: Wraps MyEsimListRoot with Activity Result API for eSIM resolution
 * On iOS: Uses MyEsimListRoot directly (iOS handles resolution differently)
 */
@Composable
expect fun PlatformEsimListScreen(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit
)