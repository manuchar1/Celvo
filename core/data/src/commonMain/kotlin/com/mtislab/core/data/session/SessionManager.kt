package com.mtislab.core.data.session

import com.mtislab.core.domain.auth.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val tokenStorage: TokenStorage,
    private val supabase: SupabaseClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // 1. Mutex დავიცვათ Refresh პროცესი ერთდროული გამოძახებებისგან
    private val refreshMutex = Mutex()

    // 2. აღარ გვჭირდება ცალკე cached variables. StateFlow თავად არის cache.
    // ვიყენებთ stateIn-ს რომ ყოველთვის გვქონდეს ბოლო მნიშვნელობა.
    val state: StateFlow<AuthState> = combine(
        tokenStorage.getAccessToken(),
        tokenStorage.getRefreshToken(),
        tokenStorage.getUserId()
    ) { accessToken, refreshToken, userId ->
        if (accessToken != null && userId != null) {
            AuthState.Authenticated(userId)
        } else {
            AuthState.Guest
        }
    }.stateIn(scope, SharingStarted.Eagerly, AuthState.Initial)

    // 3. Helper რომ მივიღოთ ტოკენები პირდაპირ Flow-დან სინქრონულად (StateFlow.value)
    // მაგრამ DataStore-ს სჭირდება დრო ინიციალიზაციისთვის, ამიტომ suspend ჯობია.

    suspend fun getAccessToken(): String? {
        // თუ Flow ჯერ Initial state-შია, ველოდებით მონაცემს
        val token = tokenStorage.getAccessToken().first()
        return token
    }

    suspend fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken().first()
    }

    fun onLoginSuccess(accessToken: String, refreshToken: String, userId: String) {
        scope.launch {
            tokenStorage.saveSession(accessToken, refreshToken, userId)
        }
    }

    fun logout() {
        scope.launch {
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
                // Ignore network errors during logout
            }
            tokenStorage.clearSession()
        }
    }

    suspend fun refreshSession(): Pair<String, String>? {
        // Mutex უზრუნველყოფს რომ მხოლოდ ერთი Refresh მოხდეს ერთდროულად
        return refreshMutex.withLock {
            try {
                // ვამოწმებთ, ხომ არ განახლდა უკვე ტოკენი სანამ რიგში ვიდექით?
                val currentAccess = getAccessToken()
                // აქ შეიძლება შემოწმება: თუ currentAccess ვალიდურია, დააბრუნე ის.

                // ვცდილობთ Supabase Refresh-ს
                supabase.auth.refreshCurrentSession()

                val session = supabase.auth.currentSessionOrNull() ?: return null
                val newAccess = session.accessToken
                val newRefresh = session.refreshToken
                val userId = session.user?.id ?: return null

                // ვინახავთ ლოკალურად
                tokenStorage.saveSession(newAccess, newRefresh, userId)

                newAccess to newRefresh
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}