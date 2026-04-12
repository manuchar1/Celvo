package com.mtislab.core.domain.repository

import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Resource<AuthData, DataError.Remote>
    suspend fun signInWithGoogleWeb(): Resource<AuthData, DataError.Remote>

    /** Web-based Apple OAuth (current — opens browser). */
    suspend fun signInWithApple(): Resource<AuthData, DataError.Remote>

    /**
     * Native Apple Sign-In via ASAuthorization identity token.
     * Same pattern as [signInWithGoogle] — sends the token to Supabase
     * via the IDToken provider, no browser redirect needed.
     *
     * TODO: Implement when native Apple Sign-In is ready.
     */
    suspend fun signInWithAppleNative(idToken: String): Resource<AuthData, DataError.Remote>

    suspend fun signOut()
}