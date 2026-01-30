package com.mtislab.core.domain.repository

import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Resource<AuthData, DataError.Remote>
    suspend fun signInWithGoogleWeb(): Resource<AuthData, DataError.Remote>
    suspend fun signInWithApple(): Resource<AuthData, DataError.Remote>

    suspend fun signOut()
}