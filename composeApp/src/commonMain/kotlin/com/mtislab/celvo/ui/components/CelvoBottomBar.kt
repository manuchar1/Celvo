package com.mtislab.celvo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mtislab.celvo.navigation.TopLevelRoute

/**
 * Cross-platform bottom navigation bar.
 *
 * State and routing decisions live in [com.mtislab.celvo.ui.MainScreenScreen]
 * (which owns the NavController). This composable is purely presentational:
 * the platform actual decides *how* to render — Material3 NavigationBar on
 * Android, UIVisualEffectView-backed glassmorphism on iOS.
 *
 * @param destinations top-level tab definitions (label, icon, [TopLevelRoute.route]).
 * @param currentRoute the current NavDestination's route string (qualified
 *  serialization name); used only to compute selection.
 * @param onNavigate fired when the user taps a tab — the host is responsible
 *  for invoking [androidx.navigation.NavController.navigate] with the right
 *  popUpTo / restoreState semantics.
 */
@Composable
expect fun CelvoBottomBar(
    destinations: List<TopLevelRoute<*>>,
    currentRoute: String?,
    onNavigate: (TopLevelRoute<*>) -> Unit,
    modifier: Modifier = Modifier,
)
