package com.mtislab.celvo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mtislab.celvo.navigation.TopLevelRoute

@Composable
actual fun CelvoBottomBar(
    destinations: List<TopLevelRoute<*>>,
    currentRoute: String?,
    onNavigate: (TopLevelRoute<*>) -> Unit,
    modifier: Modifier
) {
}