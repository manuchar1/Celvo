package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController // <-- გასწორებული იმპორტი
import com.mtislab.core.designsystem.BuildKonfig
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDSignIn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    val viewController = LocalUIViewController.current

    return remember(viewController) {
        object : GoogleAuthProvider {
            override suspend fun getGoogleIdToken(): Resource<String, DataError.Local> {
                return suspendCancellableCoroutine { continuation ->

                    // TODO: აქ ჩასვი შენი iOS Client ID (Google Cloud Console-დან)
                    val iosClientId = "952537737642-k366s3knk0al2gbnk573ri64rotjbtdv.apps.googleusercontent.com"

                    val config = GIDConfiguration(
                        clientID = iosClientId,
                        serverClientID = BuildKonfig.GOOGLE_WEB_CLIENT_ID // Web ID მოდის BuildKonfig-დან!
                    )

                    GIDSignIn.sharedInstance.configuration = config

                    GIDSignIn.sharedInstance.signInWithPresentingViewController(viewController) { result, error ->
                        if (error != null) {
                            println("Google Auth Error: ${error.localizedDescription} | domain: ${error.domain} | code: ${error.code}")
                            continuation.resume(Resource.Failure(DataError.Local.UNKNOWN))
                            return@signInWithPresentingViewController
                        }

                        val idToken = result?.user?.idToken?.tokenString
                        if (idToken != null) {
                            continuation.resume(Resource.Success(idToken))
                        } else {
                            continuation.resume(Resource.Failure(DataError.Local.UNKNOWN))
                        }
                    }
                }
            }
        }
    }
}