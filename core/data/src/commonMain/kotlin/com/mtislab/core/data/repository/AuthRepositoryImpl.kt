package com.mtislab.core.data.repository

import com.mtislab.core.data.networking.safeSupabaseCall
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {

            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
            fetchAuthData()
        }
    }

    override suspend fun signInWithGoogleWeb(): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(Google)
            fetchAuthData()
        }
    }

    override suspend fun signInWithApple(): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(Apple)
            fetchAuthData()
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }


    private suspend fun fetchAuthData(): AuthData {
        val session = supabase.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Session not found after login")

        val user = session.user
            ?: throw IllegalStateException("User not found in session")

        return AuthData(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = user.id
        )
    }
}