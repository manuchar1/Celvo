package com.mtislab.celvo

import com.mtislab.core.data.networking.safeSupabaseCall
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {
    override suspend fun signInWithGoogle(idToken: String): Resource<Unit, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
        }
    }
}