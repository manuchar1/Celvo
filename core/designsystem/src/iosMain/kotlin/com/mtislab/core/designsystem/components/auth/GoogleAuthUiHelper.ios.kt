package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    return remember {
        object : GoogleAuthProvider {
            override suspend fun getGoogleIdToken(): Resource<String, DataError.Local> {
                return Resource.Failure(DataError.Local.UNKNOWN)
            }
        }
    }
}