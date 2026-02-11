package com.mtislab.core.designsystem.components.headers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtislab.core.designsystem.components.buttons.CelvoCircleButton
import org.jetbrains.compose.resources.painterResource


@Composable
fun CelvoMainHeader(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Painter

) {


    Row(
        modifier = modifier
            .fillMaxWidth()
            //.statusBarsPadding()
            .height(68.dp),
            //.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = (-0.5).sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.widthIn(min = 16.dp))

        CelvoCircleButton(
            onClick = onClick,
            icon = icon,
        )
    }


}