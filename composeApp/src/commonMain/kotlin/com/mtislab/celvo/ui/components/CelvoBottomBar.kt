package com.mtislab.celvo.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.mtislab.celvo.navigation.bottomNavRoutes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CelvoBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    val containerColor = Color(0xFF25252D)
    val indicatorColor = Color(0xFF4A4458)
    val activeColor = Color(0xFFD0BCFF)
    val inactiveColor = Color(0xFFC4C4C4)

    NavigationBar(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), // მრგვალი კუთხეები
        containerColor = containerColor,
        tonalElevation = 0.dp
    ) {
        bottomNavRoutes.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // მთავარ ტაბებზე გადასვლისას სტეკის გასუფთავება
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon), // 👈 SVG-ს ხატავს
                        contentDescription = stringResource(item.name)
                    )
                },
                label = {
                    Text(stringResource(item.name)) // 👈 ენას ცვლის ავტომატურად
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = indicatorColor,
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = inactiveColor,
                    unselectedTextColor = inactiveColor
                )
            )
        }
    }
}