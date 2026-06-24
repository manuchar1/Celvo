package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.AppleSignInResult
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

/**
 * Apple Sign-In is not supported on Android — the Apple button is never
 * rendered on this platform (see RegisterScreen), so this provider is
 * only a type-level placeholder and always returns failure.
 */
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
