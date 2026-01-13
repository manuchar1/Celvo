package com.mtislab.celvo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.nav_my_esim
import com.celvo.core.designsystem.resources.nav_profile
import com.celvo.core.designsystem.resources.nav_search

import com.mtislab.celvo.feature.store.presentation.StoreScreenRoot
import com.mtislab.celvo.feature.store.presentation.packages.PackagesScreenRoot
import com.mtislab.celvo.navigation.Route
import com.mtislab.celvo.navigation.bottomNavRoutes
import com.mtislab.celvo.ui.components.CelvoBottomBar
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

    val startColor = MaterialTheme.colorScheme.extended.gradientStart

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            // Start: ვიღებთ პირდაპირ თემიდან (20% Opacity)
            startColor,

            // End: იგივე ფერი, ოღონდ სრულიად გამჭვირვალე (0% Opacity)
            startColor.copy(alpha = 0f)
        ),
        startY = 0f,
        endY = 1200f
    )

    // განსაზღვრავს, სად გამოჩნდეს Bottom Bar
    val showBottomBar = bottomNavRoutes.any {
        currentDestination?.hasRoute(it.route::class) == true
    }

    // 📦 2. მთავარი კონტეინერი (Box) - ფენების პრინციპი
    Box(
        modifier = Modifier
            .fillMaxSize()
            // ✅ ფენა 1: მყარი ფონი (Base Layer)
            // ეს აუცილებელია! ამის გარეშე Light Mode-ზე გრადიენტი თეთრად/ნაცრისფრად გამოჩნდება.
            // Dark Mode-ზე ეს იქნება #0A0B0C, Light-ზე White.
            .background(MaterialTheme.colorScheme.background)

            // ✨ ფენა 2: გრადიენტი (Gradient Layer)
            // ეს ედება მყარ ფონს ზემოდან
            .background(brush = backgroundBrush)
    ) {
        // 🏗️ 3. სკაფოლდი (Transparent)
        Scaffold(
            // 🛑 კრიტიკულია: სკაფოლდი უნდა იყოს გამჭვირვალე, რომ უკანა Box გამოჩნდეს
            containerColor = Color.Transparent,

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
                    // BottomBar-ის პადინგს ვითვალისწინებთ მხოლოდ ქვემოდან
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {

                // --- Auth Flow ---
                navigation<Route.AuthGraph>(startDestination = Route.Login) {
                    composable<Route.Login> {
                        // Login ეკრანიც გამჭვირვალეა Box-ით
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Button(onClick = {
                                navController.navigate(Route.Home) {
                                    popUpTo(Route.AuthGraph) { inclusive = true }
                                }
                            }) {
                                Text("Login (Click to go to Store)")
                            }
                        }
                    }
                    composable<Route.Register> {
                        ScreenPlaceholder("Register Screen")
                    }
                }

                // --- Main Tabs ---

                // 🏠 Home (Store) Tab
                composable<Route.Home> {
                    StoreScreenRoot(
                        onNavigateToDetails = { isoCode, countryName ->
                            navController.navigate(
                                Route.Packages(
                                    isoCode = isoCode,
                                    countryName = countryName
                                )
                            )
                        }
                    )
                }


                composable<Route.Packages> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.Packages>()

                    PackagesScreenRoot(
                        isoCode = args.isoCode,
                        countryName = args.countryName,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // 🔍 Search Tab
                composable<Route.Search> {
                    ScreenPlaceholder(stringResource(Res.string.nav_search))
                }

                // 📶 My eSIMs Tab
                composable<Route.MyEsim> {
                    ScreenPlaceholder(stringResource(Res.string.nav_my_esim))
                }

                // 👤 Profile Tab
                composable<Route.Profile> {
                    ScreenPlaceholder(stringResource(Res.string.nav_profile))
                }
            }
        }
    }
}

// 🛑 Placeholder-იც აუცილებლად გამჭვირვალე უნდა იყოს!
@Composable
private fun ScreenPlaceholder(text: String) {
    // აქ არ ვიყენებთ .background()-ს, რომ MainScreen-ის გრადიენტი გამოჩნდეს
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}