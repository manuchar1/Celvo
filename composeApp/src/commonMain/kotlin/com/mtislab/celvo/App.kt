package com.mtislab.celvo

import androidx.compose.runtime.Composable
import com.mtislab.celvo.ui.MainScreenRoot
import com.mtislab.core.designsystem.theme.CelvoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    CelvoTheme {
        MainScreenRoot()
    }
}