// File: ui/MainScreen.kt
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
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.nav_my_esim
import com.celvo.core.designsystem.resources.nav_search
import com.mtislab.celvo.feature.auth.presentation.register.RegisterRoot
import com.mtislab.celvo.feature.profile.presentation.ProfileRoot
import com.mtislab.celvo.feature.profile.presentation.settings.LanguageScreen
import com.mtislab.celvo.feature.profile.presentation.settings.ThemeScreen
import com.mtislab.celvo.feature.store.presentation.StoreScreenRoot
import com.mtislab.celvo.feature.store.presentation.checkout.CheckoutScreenRoot
import com.mtislab.celvo.feature.store.presentation.checkout.PaymentResultScreen // ✅ ახალი იმპორტი
import com.mtislab.celvo.feature.store.presentation.packages.PackagesScreenRoot
import com.mtislab.celvo.navigation.bottomNavRoutes
import com.mtislab.celvo.ui.components.CelvoBottomBar
import com.mtislab.core.designsystem.theme.extended
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.utils.DeepLinkHandler // ✅ ახალი იმპორტი
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

    // 🚀 Deep Link Listener (გლობალური მოსმენა)
    LaunchedEffect(Unit) {
        DeepLinkHandler.events.collect { url ->
            println("🔗 DeepLink caught in UI: $url")

            // ვამოწმებთ, არის თუ არა ეს გადახდის შედეგი
            if (url.contains("/payment/result") || url.contains("/payment/status")) {

                // 🛑 ლოგიკა: როგორ გავიგოთ წარმატებული იყო თუ არა?
                // ეს დამოკიდებულია იმაზე, რას აბრუნებს ბანკი URL-ში.
                // როგორც წესი, ეს არის `status=success` ან `status=fail`.
                // აქ შეგიძლია URL პარსერი გამოიყენო ან უბრალო string check.

                // მაგალითად (თუ ბანკი აბრუნებს ?status=XXX):
                val isSuccess = !url.contains("fail", ignoreCase = true) &&
                        !url.contains("error", ignoreCase = true)

                // ამოვიღოთ Order ID (მარტივი string მანიპულაციით)
                // მაგ: ...?order_id=12345&...
                val orderId = url.substringAfter("order_id=", "").substringBefore("&")

                // ✅ გადავდივართ Result ეკრანზე
                navController.navigate(
                    Route.PaymentResult(
                        isSuccess = isSuccess,
                        orderId = if (orderId.isNotEmpty()) orderId else null
                    )
                ) {
                    // Checkout და Home სქრინების გასუფთავება სტეკიდან (სურვილისამებრ)
                    // აქ ვტოვებთ Home-ს, მაგრამ ვაგდებთ Checkout-ს
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

                // ... (Auth Graph იგივე რჩება) ...
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

                // ... (Home, Packages, Checkout იგივე რჩება) ...
                composable<Route.Home> {
                    StoreScreenRoot(
                        onNavigateToDetails = { isoCode, countryName ->
                            navController.navigate(Route.Packages(isoCode, countryName))
                        },
                        onNavigateToLogin = {
                            navController.navigate(Route.Login(redirectTo = null))
                        }
                    )
                }

                composable<Route.Packages> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.Packages>()
                    PackagesScreenRoot(
                        isoCode = args.isoCode,
                        countryName = args.countryName,
                        onBackClick = { navController.popBackStack() },
                        onPackageSelected = { selectedPkg ->
                            navController.navigate(
                                Route.CheckoutRoute(
                                    packageId = selectedPkg.id,
                                    countryName = args.countryName
                                )
                            )
                        }
                    )
                }

                composable<Route.CheckoutRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.CheckoutRoute>()
                    CheckoutScreenRoot(
                        countryName = args.countryName,
                        onClose = { navController.popBackStack() }
                    )
                }

                // 👇 ✅ ახალი: Payment Result Screen
                composable<Route.PaymentResult> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.PaymentResult>()

                    PaymentResultScreen(
                        isSuccess = args.isSuccess,
                        orderId = args.orderId,
                        onHomeClick = {
                            // სრულად ვასუფთავებთ სტეკს და მივდივართ Home-ზე
                            navController.navigate(Route.Home) {
                                popUpTo(Route.Home) { inclusive = true }
                            }
                        }
                    )
                }

                // ... (Search, MyEsim, Profile იგივე რჩება) ...
                composable<Route.Search> {
                    ScreenPlaceholder(stringResource(Res.string.nav_search))
                }

                composable<Route.MyEsim> {
                    if (state.isLoggedIn) {
                        ScreenPlaceholder(stringResource(Res.string.nav_my_esim))
                    } else {
                        LaunchedEffect(Unit) {
                            navController.navigate(Route.Login(redirectTo = "my_esim")) {
                                popUpTo(Route.Home) { saveState = true }
                            }
                        }
                    }
                }

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

@Composable
private fun ScreenPlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}