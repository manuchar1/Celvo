package com.mtislab.celvo.navigation



import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.home
import com.celvo.core.designsystem.resources.my_esims
import com.celvo.core.designsystem.resources.nav_home
import com.celvo.core.designsystem.resources.nav_my_esim
import com.celvo.core.designsystem.resources.nav_profile
import com.celvo.core.designsystem.resources.nav_search
import com.celvo.core.designsystem.resources.profile
import com.celvo.core.designsystem.resources.search
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class TopLevelRoute<T : Route>(
    val name: StringResource,
    val route: T,
    val icon: DrawableResource
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