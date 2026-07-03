package com.mtislab.celvo.feature.auth.presentation.register

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.auth.presentation.generated.resources.Res as AuthRes
import celvo.feature.auth.presentation.generated.resources.auth_error_title
import celvo.feature.auth.presentation.generated.resources.img_mascot_fox
import celvo.feature.auth.presentation.generated.resources.img_mascot_fox_gift
import celvo.feature.auth.presentation.generated.resources.onboarding_page1_subtitle
import celvo.feature.auth.presentation.generated.resources.onboarding_page1_title
import celvo.feature.auth.presentation.generated.resources.onboarding_page2_subtitle
import celvo.feature.auth.presentation.generated.resources.onboarding_page2_title
// Page 3 temporarily disabled — restore these together with pageCount = 3:
// import celvo.feature.auth.presentation.generated.resources.onboarding_page3_subtitle
// import celvo.feature.auth.presentation.generated.resources.onboarding_page3_title
import celvo.feature.auth.presentation.generated.resources.register_apple_sign_in
import celvo.feature.auth.presentation.generated.resources.register_google_sign_in
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_apple_logo
import com.celvo.core.designsystem.resources.ic_google_logo
import com.celvo.core.designsystem.resources.legal_consent_signin
import com.celvo.core.designsystem.resources.legal_privacy_policy
import com.celvo.core.designsystem.resources.legal_terms_of_service
import com.mtislab.celvo.feature.auth.presentation.platform
import com.mtislab.celvo.feature.auth.presentation.register.components.OnboardingSlidingContent
import com.mtislab.celvo.feature.auth.presentation.register.components.PagerIndicator
import com.mtislab.celvo.feature.auth.presentation.register.components.RegisterTopBar
import com.mtislab.core.designsystem.components.auth.rememberAppleAuthProvider
import com.mtislab.core.designsystem.components.auth.rememberGoogleAuthProvider
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationData
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationType
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.legal.LegalConsentText
import com.mtislab.core.designsystem.legal.LegalLink
import com.mtislab.core.designsystem.legal.LegalLinks
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onSkipClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationState = LocalCelvoNotification.current

    val googleAuthProvider = rememberGoogleAuthProvider()
    val appleAuthProvider = rememberAppleAuthProvider()
    val isAndroid = platform().contains("Android", ignoreCase = true)

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Route auth errors (cancel, network, Supabase rejection) through the
    // app-wide CelvoTopNotification instead of a Material Snackbar.
    val authErrorTitle = stringResource(AuthRes.string.auth_error_title)
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            notificationState.show(
                CelvoNotificationData(
                    message = authErrorTitle,
                    description = error.toString(),
                    type = CelvoNotificationType.Error,
                )
            )
        }
    }

    RegisterScreen(
        state = state,
        isAndroid = isAndroid,
        onGoogleSignInClick = {
            viewModel.onAction(RegisterAction.OnGoogleSignInClick(googleAuthProvider))
        },
        onAppleSignInClick = {
            // Pass the native provider on iOS; the ViewModel tries native
            // first and falls back to the web flow if it returns failure.
            val provider = if (!isAndroid) appleAuthProvider else null
            viewModel.onAction(RegisterAction.OnAppleSignInClick(provider))
        },
        onSkipClick = onSkipClick
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    isAndroid: Boolean,
    onGoogleSignInClick: () -> Unit,
    onAppleSignInClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    // Page 3 temporarily disabled — restore with pageCount = { 3 }.
    val pagerState = rememberPagerState(pageCount = { 2 })


    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RegisterTopBar(onSkipClick = onSkipClick)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                  //  Spacer(modifier = Modifier.weight(0.1f)) // ზედა სივრცე

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = true
                        ) { page ->
                            OnboardingSlidingContent(
                                page = page,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        // 2. MASCOT LAYER 🦊
                        val mascotRes = when (pagerState.currentPage) {
                            0 -> celvo.feature.auth.presentation.generated.resources.Res.drawable.img_mascot_fox
                            1 -> celvo.feature.auth.presentation.generated.resources.Res.drawable.img_mascot_fox
                            else -> celvo.feature.auth.presentation.generated.resources.Res.drawable.img_mascot_fox_gift
                        }

                        Crossfade(
                            targetState = mascotRes,
                            label = "MascotAnimation",
                            animationSpec = tween(400)
                        ) { targetMascot ->
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(targetMascot),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(180.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(
                                            x = 0.dp,
                                            y = 16.dp
                                        )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    PagerIndicator(
                        pageCount = 2,
                        currentPage = pagerState.currentPage)

                    Spacer(modifier = Modifier.height(24.dp))


                    val (title, subtitle) = when (pagerState.currentPage) {
                        0 -> stringResource(AuthRes.string.onboarding_page1_title) to
                                stringResource(AuthRes.string.onboarding_page1_subtitle)
                        // Page 3 temporarily disabled — restore together with pageCount = 3:
                        // 2 -> stringResource(AuthRes.string.onboarding_page3_title) to
                        //         stringResource(AuthRes.string.onboarding_page3_subtitle)
                        else -> stringResource(AuthRes.string.onboarding_page2_title) to
                                stringResource(AuthRes.string.onboarding_page2_subtitle)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            fontFamily = PlusJakartaSans,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.extended.textPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.extended.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontFamily = PlusJakartaSans
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))



                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        // Apple — shown FIRST on iOS, hidden entirely on Android.
                        if (!isAndroid) {
                            CelvoButton(
                                text = stringResource(AuthRes.string.register_apple_sign_in),
                                leadingIcon = painterResource(Res.drawable.ic_apple_logo),
                                onClick = onAppleSignInClick,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.extended.cardBorder),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Google — always present.
                        CelvoButton(
                            text = stringResource(AuthRes.string.register_google_sign_in),
                            leadingIcon = painterResource(Res.drawable.ic_google_logo),
                            onClick = onGoogleSignInClick,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.extended.cardBorder),
                        )

                        Spacer(modifier = Modifier.height(24.dp))


                        // Required before account creation (Apple 5.1.1 + Google Play):
                        // consent with inline tappable Terms / Privacy links.
                        LegalConsentText(
                            template = stringResource(Res.string.legal_consent_signin),
                            links = listOf(
                                LegalLink(
                                    label = stringResource(Res.string.legal_terms_of_service),
                                    url = LegalLinks.TERMS_OF_SERVICE
                                ),
                                LegalLink(
                                    label = stringResource(Res.string.legal_privacy_policy),
                                    url = LegalLinks.PRIVACY_POLICY
                                ),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}