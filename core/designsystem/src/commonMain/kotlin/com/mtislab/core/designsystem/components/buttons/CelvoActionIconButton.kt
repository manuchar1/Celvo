package com.mtislab.core.designsystem.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mtislab.core.designsystem.theme.CelvoTheme
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CelvoActionIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.extended.textPrimary,
    contentDescription: String? = null
) {
    CelvoIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}



@Composable
fun CelvoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        content = content
    )
}

@Preview
@Composable
private fun CelvoActionIconButtonPreview() {
    CelvoTheme {
        CelvoActionIconButton(
            icon = Icons.Default.Close,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun CelvoBackArrowPreview() {
    CelvoTheme {
        CelvoActionIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = {}
        )
    }
}