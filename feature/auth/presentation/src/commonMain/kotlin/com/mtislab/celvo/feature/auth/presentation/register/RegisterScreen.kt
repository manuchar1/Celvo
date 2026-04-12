package com.mtislab.celvo.feature.auth.presentation.register

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.auth.presentation.generated.resources.img_mascot_fox
import celvo.feature.auth.presentation.generated.resources.img_mascot_fox_gift
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.ic_apple_logo
import com.celvo.core.designsystem.resources.ic_google_logo
import com.mtislab.celvo.feature.auth.presentation.platform
import com.mtislab.celvo.feature.auth.presentation.register.components.OnboardingSlidingContent
import com.mtislab.celvo.feature.auth.presentation.register.components.PagerIndicator
import com.mtislab.celvo.feature.auth.presentation.register.components.RegisterTopBar
import com.mtislab.core.designsystem.components.auth.rememberAppleAuthProvider
import com.mtislab.core.designsystem.components.auth.rememberGoogleAuthProvider
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.theme.PlusJakartaSans
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onSkipClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val googleAuthProvider = rememberGoogleAuthProvider()
    val appleAuthProvider = rememberAppleAuthProvider()
    val isAndroid = platform().contains("Android", ignoreCase = true)

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar("$it")
        }
    }

    RegisterScreen(
        state = state,
        isAndroid = isAndroid,
        snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState,
    onGoogleSignInClick: () -> Unit,
    onAppleSignInClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })


    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RegisterTopBar(onSkipClick = onSkipClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                // ეს Box აუცილებელია, რომ Image-მა შეავსოს სივრცე და align იმუშაოს
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(targetMascot),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(180.dp)
                                        .align(Alignment.BottomEnd) // 🛑 ეს სვამს მარჯვნივ!
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
                        pageCount = 3,
                        currentPage = pagerState.currentPage)

                    Spacer(modifier = Modifier.height(24.dp))


                    val (title, subtitle) = when (pagerState.currentPage) {
                        0 -> "უსაზღვრო კავშირი\nნებისმიერ ქვეყანაში" to "აირჩიე ქვეყანა და გაააქტიურე eSIM\nრამდენიმე წამში."
                        1 -> "მარტივი ინსტალაცია\nQR კოდით" to "დაასკანერე და ჩაერთე ქსელში\nზედმეტი ძალისხმევის გარეშე."
                        else -> "საუკეთესო ფასები\nროუმინგის გარეშე" to "დაზოგე თანხა და ისარგებლე\nადგილობრივი ტარიფებით."
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
                        // Google
                        CelvoButton(
                            text = "Google-ით შესვლა",
                            leadingIcon = painterResource(Res.drawable.ic_google_logo),
                            onClick = onGoogleSignInClick,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                           // borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                        // Apple
                        if (!isAndroid) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CelvoButton(
                                text = "Apple-ით შესვლა",
                                leadingIcon = painterResource(Res.drawable.ic_apple_logo),
                                onClick = onAppleSignInClick,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                               // borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))


                        Text(
                            text = "გაგრძელებით თქვენ ეთანხმებით წესებს და პირობებს.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.extended.textTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}