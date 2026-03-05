package com.mtislab.celvo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.mtislab.celvo.PlatformEsimListScreen
import com.mtislab.celvo.feature.auth.presentation.register.RegisterRoot
import com.mtislab.celvo.feature.myesim.presentation.details.EsimDetailsRoot
import com.mtislab.celvo.feature.profile.presentation.ProfileRoot
import com.mtislab.celvo.feature.profile.presentation.settings.LanguageScreen
import com.mtislab.celvo.feature.profile.presentation.settings.ThemeScreen
import com.mtislab.celvo.feature.store.presentation.store.StoreScreenRoot
import com.mtislab.celvo.feature.store.presentation.checkout.CheckoutScreenRoot
import com.mtislab.celvo.feature.store.presentation.checkout.PaymentResultScreen
import com.mtislab.celvo.feature.store.presentation.packages.PackagesScreenRoot
import com.mtislab.celvo.feature.store.presentation.search.SearchRoot
import com.mtislab.celvo.navigation.bottomNavRoutes
import com.mtislab.celvo.ui.components.CelvoBottomBar
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.utils.DeepLinkHandler
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

    // 🚀 Deep Link Listener
    LaunchedEffect(Unit) {
        DeepLinkHandler.events.collect { url ->
            println("🔗 DeepLink caught in UI: $url")

            if (url.contains("/payment/result") || url.contains("/payment/status")) {
                val isSuccess = !url.contains("fail", ignoreCase = true) &&
                        !url.contains("error", ignoreCase = true)

                val orderId = url.substringAfter("order_id=", "").substringBefore("&")

                navController.navigate(
                    Route.PaymentResult(
                        isSuccess = isSuccess,
                        orderId = if (orderId.isNotEmpty()) orderId else null
                    )
                ) {
                    popUpTo(Route.Home)
                }
            }
        }
    }

    val startColor = MaterialTheme.colorScheme.extended.gradientStart

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            startColor,
            startColor.copy(alpha = 0f)
        ),
        startY = 0f,
        endY = 1200f
    )

    val showBottomBar = bottomNavRoutes.any {
        currentDestination?.hasRoute(it.route::class) == true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(brush = backgroundBrush)
    ) {
        Scaffold(
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
                startDestination = Route.Home,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {

                // Auth Graph
                navigation<Route.AuthGraph>(startDestination = Route.Login()) {
                    composable<Route.Login> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.Login>()
                        val redirectTo = args.redirectTo
                        RegisterRoot(
                            onLoginSuccess = {
                                if (redirectTo != null) {
                                    val targetRoute = when (redirectTo) {
                                        "profile" -> Route.Profile
                                        "my_esim" -> Route.MyEsim
                                        else -> Route.Home
                                    }
                                    navController.navigate(targetRoute) {
                                        popUpTo(Route.AuthGraph) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Route.Home) {
                                        popUpTo(Route.Home) { inclusive = true }
                                    }
                                }
                            },
                            onSkipClick = {
                                navController.navigate(Route.Home) {
                                    popUpTo(Route.Home) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable<Route.Register> {
                        ScreenPlaceholder("Manual Register Screen")
                    }
                }

                composable<Route.Home> {
                    StoreScreenRoot(
                        onNavigateToDetails = { isoCode, countryName, type ->
                            navController.navigate(Route.Packages(isoCode, countryName, type))
                        },
                        onNavigateToLogin = {
                            navController.navigate(Route.Login(redirectTo = null))

                        },

                        onNavigateToSearch = { tab, focus ->
                            navController.navigate(
                                Route.Search(
                                    initialTab = tab,
                                    focusSearch = focus
                                )
                            )
                        }
                    )
                }

                // Packages
                composable<Route.Packages> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.Packages>()
                    PackagesScreenRoot(
                        isoCode = args.isoCode,
                        countryName = args.countryName,
                        type = args.type,
                        onBackClick = { navController.popBackStack() },
                        onPackageSelected = { selectedPkg ->
                            navController.navigate(
                                Route.CheckoutRoute(
                                    packageId = selectedPkg.id,
                                    countryName = args.countryName,
                                    type = args.type,
                                    region = args.isoCode

                                )
                            )
                        }
                    )
                }

                // Checkout
                composable<Route.CheckoutRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.CheckoutRoute>()
                    CheckoutScreenRoot(
                        countryName = args.countryName,
                        type = args.type,
                        region = args.region,
                        onClose = { navController.popBackStack() }
                    )
                }

                // Payment Result
                composable<Route.PaymentResult> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.PaymentResult>()
                    PaymentResultScreen(
                        isSuccess = args.isSuccess,
                        orderId = args.orderId,
                        onHomeClick = {
                            navController.navigate(Route.Home) {
                                popUpTo(Route.Home) { inclusive = true }
                            }
                        }
                    )
                }

                // Search Screen
                composable<Route.Search> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.Search>()

                    SearchRoot(
                        initialTab = args.initialTab,
                        focusSearch = args.focusSearch,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToDetails = { isoCode, countryName, type ->
                            navController.navigate(Route.Packages(isoCode, countryName, type))
                        }
                    )
                }


                composable<Route.MyEsim> {
                    if (state.isLoggedIn) {
                        PlatformEsimListScreen(
                            onEsimClick = { esim ->
                                navController.navigate(Route.EsimDetailsRoute(esimId = esim.id))
                            },
                            onAddEsimClick = {
                                navController.navigate(Route.Home)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.navigate(Route.Login(redirectTo = "my_esim")) {
                                popUpTo(Route.Home) { saveState = true }
                            }
                        }
                    }
                }

                // eSIM Details
                composable<Route.EsimDetailsRoute> {
                    EsimDetailsRoot(
                        onBackClick = { navController.popBackStack() },
                        onTopUpClick = { esimId -> /* TODO: Navigate to top-up */ }
                    )
                }

                // Profile
                composable<Route.Profile> {
                    if (state.isLoggedIn) {
                        ProfileRoot(
                            onNavigateToTheme = { navController.navigate(Route.Theme) },
                            onNavigateToLanguage = { navController.navigate(Route.Language) }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.navigate(Route.Login(redirectTo = "profile"))
                        }
                    }
                }

                // Theme Settings
                composable<Route.Theme> {
                    ThemeScreen(onBackClick = { navController.popBackStack() })
                }

                // Language Settings
                composable<Route.Language> {
                    LanguageScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun ScreenPlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}