package com.mtislab.domain

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Resource<Unit, DataError.Remote>
}