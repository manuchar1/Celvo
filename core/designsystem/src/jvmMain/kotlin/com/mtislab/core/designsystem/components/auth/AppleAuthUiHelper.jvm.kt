package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.AppleSignInResult
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

/** Desktop/JVM target does not surface Apple Sign-In — always fails. */
@Composable
actual fun rememberAppleAuthProvider(): AppleAuthProvider {
    return remember {
        object : AppleAuthProvider {
            override suspend fun getAppleIdToken(): Resource<AppleSignInResult, DataError.Local> {
                return Resource.Failure(DataError.Local.UNKNOWN)
            }
        }
    }
}
