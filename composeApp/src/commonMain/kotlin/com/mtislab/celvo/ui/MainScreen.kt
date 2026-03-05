package com.mtislab.celvo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.mtislab.core.domain.model.Route.SearchTab
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

    LaunchedEffect(Unit) {
        DeepLinkHandler.events.collect { url ->
            if (url.contains("/payment/result") || url.contains("/payment/status")) {
                val isSuccess = !url.contains("fail", ignoreCase = true) &&
                        !url.contains("error", ignoreCase = true)
                val orderId = url.substringAfter("order_id=", "").substringBefore("&")
                navController.navigate(
                    Route.PaymentResult(
                        isSuccess = isSuccess,
                        orderId = orderId.ifEmpty { null }
                    )
                ) {
                    popUpTo(Route.Home)
                }
            }
        }


    }


    LaunchedEffect(state.isLoggedIn) {
        if (!state.isAuthLoading && !state.isLoggedIn) {
            navController.navigate(Route.Home) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val startColor = MaterialTheme.colorScheme.extended.gradientStart
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(startColor, startColor.copy(alpha = 0f)),
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

                // ── Auth Graph ─────────────────────────────────────────────────────────
                navigation<Route.AuthGraph>(startDestination = Route.Login()) {
                    composable<Route.Login> { backStackEntry ->
                        val args = backStackEntry.toRoute<Route.Login>()
                        RegisterRoot(
                            onLoginSuccess = {
                                val target = when (args.redirectTo) {
                                    "profile" -> Route.Profile
                                    "my_esim" -> Route.MyEsim
                                    else      -> Route.Home
                                }
                                navController.navigate(target) {
                                    popUpTo(navController.graph.id) { inclusive = true }
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

                // ── Store / Home ───────────────────────────────────────────────────────
                composable<Route.Home> {
                    StoreScreenRoot(
                        onNavigateToDetails = { isoCode, countryName, type ->
                            navController.navigate(Route.Packages(isoCode, countryName, type))
                        },
                        onNavigateToLogin = {
                            navController.navigate(Route.Login(redirectTo = null))
                        },
                        onNavigateToSearch = { tab, focus ->
                            navController.navigate(Route.Search(initialTab = tab, focusSearch = focus))
                        }
                    )
                }

                // ── Packages ───────────────────────────────────────────────────────────
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

                // ── Checkout ───────────────────────────────────────────────────────────
                composable<Route.CheckoutRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.CheckoutRoute>()
                    CheckoutScreenRoot(
                        countryName = args.countryName,
                        type = args.type,
                        region = args.region,
                        onClose = { navController.popBackStack() },
                        onNavigateToPaymentResult = { isSuccess, orderId ->}
                    )
                }

                // ── Payment Result ─────────────────────────────────────────────────────
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

                // ── Search ─────────────────────────────────────────────────────────────
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

                // ── My eSIM (auth-guarded) ─────────────────────────────────────────────
                composable<Route.MyEsim> {
                    AuthGuardedContent(
                        isAuthLoading = state.isAuthLoading,
                        isLoggedIn = state.isLoggedIn,
                        onRedirectToLogin = {
                            navController.navigate(Route.Login(redirectTo = "my_esim")) {
                                popUpTo(Route.Home) { saveState = true }
                            }
                        }
                    ) {
                        PlatformEsimListScreen(
                            onEsimClick = { esim ->
                                navController.navigate(Route.EsimDetailsRoute(esimId = esim.id))
                            },
                            onAddEsimClick = {
                                navController.navigate(Route.Search(initialTab = SearchTab.COUNTRY, focusSearch = false))
                            },
                            onTopUpClick = {}
                        )
                    }
                }

                // ── eSIM Details ───────────────────────────────────────────────────────
                composable<Route.EsimDetailsRoute> {
                    EsimDetailsRoot(
                        onBackClick = { navController.popBackStack() },
                        onTopUpClick = { /* TODO */ }
                    )
                }

                // ── Profile (auth-guarded) ─────────────────────────────────────────────
                composable<Route.Profile> {
                    AuthGuardedContent(
                        isAuthLoading = state.isAuthLoading,
                        isLoggedIn = state.isLoggedIn,
                        onRedirectToLogin = {
                            navController.navigate(Route.Login(redirectTo = "profile"))
                        }
                    ) {
                        ProfileRoot(
                            onNavigateToTheme = { navController.navigate(Route.Theme) },
                            onNavigateToLanguage = { navController.navigate(Route.Language) }
                        )
                    }
                }

                // ── Settings ───────────────────────────────────────────────────────────
                composable<Route.Theme> {
                    ThemeScreen(onBackClick = { navController.popBackStack() })
                }

                composable<Route.Language> {
                    LanguageScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}

/**
 * Reusable auth guard for protected destinations.
 *
 * States:
 *  - [isAuthLoading] = true  → SessionManager hasn't emitted yet; show a
 *                              spinner and make NO routing decision. This is
 *                              the key fix: without this guard, the default
 *                              isLoggedIn=false would immediately redirect
 *                              authenticated users to Login before the token
 *                              was verified, preventing the content from ever
 *                              being composed and the API from ever being called.
 *  - [isLoggedIn]   = true  → Compose [content].
 *  - else                   → Fire [onRedirectToLogin] once via LaunchedEffect.
 */
@Composable
private fun AuthGuardedContent(
    isAuthLoading: Boolean,
    isLoggedIn: Boolean,
    onRedirectToLogin: () -> Unit,
    content: @Composable () -> Unit,
) {
    when {
        isAuthLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        isLoggedIn -> content()

        else -> {
            LaunchedEffect(Unit) {
                onRedirectToLogin()
            }
        }
    }
}

@Composable
private fun ScreenPlaceholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onBackground)
    }
}