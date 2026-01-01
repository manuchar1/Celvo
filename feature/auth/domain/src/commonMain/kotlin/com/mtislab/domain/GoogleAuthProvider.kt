package com.mtislab.domain

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource


interface GoogleAuthProvider {
    suspend fun getGoogleIdToken(): Resource<String, DataError.Local>
}