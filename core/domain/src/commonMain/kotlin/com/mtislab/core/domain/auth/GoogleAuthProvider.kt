package com.mtislab.core.domain.auth

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface GoogleAuthProvider {
    suspend fun getGoogleIdToken(): Resource<String, DataError.Local>
}