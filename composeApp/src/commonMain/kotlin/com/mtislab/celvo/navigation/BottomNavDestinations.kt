package com.mtislab.celvo.navigation

import celvo.core.designsystem.generated.resources.Res
import celvo.core.designsystem.generated.resources.home
import celvo.core.designsystem.generated.resources.my_esims
import celvo.core.designsystem.generated.resources.nav_home
import celvo.core.designsystem.generated.resources.nav_my_esim
import celvo.core.designsystem.generated.resources.nav_profile
import celvo.core.designsystem.generated.resources.nav_search
import celvo.core.designsystem.generated.resources.profile
import celvo.core.designsystem.generated.resources.search

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class TopLevelRoute<T : Route>(
    val name: StringResource,   // 👈 მულტიენოვანი ტექსტი
    val route: T,
    val icon: DrawableResource  // 👈 SVG იკონი
)

val bottomNavRoutes = listOf(
    TopLevelRoute(
        name = Res.string.nav_home,
        route = Route.Home,
        icon = Res.drawable.home
    ),
    TopLevelRoute(
        name = Res.string.nav_search,
        route = Route.Search,
        icon = Res.drawable.search
    ),
    TopLevelRoute(
        name = Res.string.nav_my_esim,
        route = Route.MyEsim,
        icon = Res.drawable.my_esims
    ),
    TopLevelRoute(
        name = Res.string.nav_profile,
        route = Route.Profile,
        icon = Res.drawable.profile
    )
)