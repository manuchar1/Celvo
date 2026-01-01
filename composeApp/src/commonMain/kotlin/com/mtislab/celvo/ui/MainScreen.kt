package com.mtislab.celvo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.mtislab.celvo.navigation.Route
import com.mtislab.celvo.navigation.bottomNavRoutes
import com.mtislab.celvo.ui.components.CelvoBottomBar
import com.mtislab.core.designsystem.theme.CelvoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

import org.jetbrains.compose.resources.stringResource
import celvo.core.designsystem.generated.resources.Res
import celvo.core.designsystem.generated.resources.nav_home
import celvo.core.designsystem.generated.resources.nav_search
import celvo.core.designsystem.generated.resources.nav_my_esim
import celvo.core.designsystem.generated.resources.nav_profile

@Composable
fun MainScreenRoot(
    viewModel: MainScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreenScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun MainScreenScreen(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavRoutes.any {
        currentDestination?.hasRoute(it.route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CelvoBottomBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.AuthGraph,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding())
        ) {

            // Auth Graph
            navigation<Route.AuthGraph>(startDestination = Route.Login) {
                composable<Route.Login> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Button(onClick = {
                            navController.navigate(Route.Home) {
                                popUpTo(Route.AuthGraph) { inclusive = true }
                            }
                        }) {
                            Text("Login (Click to go Home)")
                        }
                    }
                }
                composable<Route.Register> {
                    Text("Register Screen Placeholder")
                }
            }

            // 🏠 Main Tabs (აქ ვიყენებთ რესურსებს სწორად)
            composable<Route.Home> {
                val title = stringResource(Res.string.nav_home)
                ScreenPlaceholder("$title \n Param: ${state.paramOne}")
            }
            composable<Route.Search> {
                ScreenPlaceholder(stringResource(Res.string.nav_search))
            }
            composable<Route.MyEsim> {
                ScreenPlaceholder(stringResource(Res.string.nav_my_esim))
            }
            composable<Route.Profile> {
                ScreenPlaceholder(stringResource(Res.string.nav_profile))
            }
        }
    }
}

@Composable
private fun ScreenPlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text) // აქ ტექსტი უკვე ლოკალიზებული მოვა
    }
}

@Preview
@Composable
private fun Preview() {
    CelvoTheme {
        MainScreenScreen(
            state = MainScreenState(),
            onAction = {}
        )
    }
}