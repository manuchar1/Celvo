
package com.mtislab.core.data.session

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    fun getAccessToken(): Flow<String?>
    fun getRefreshToken(): Flow<String?>
    fun getUserId(): Flow<String?>

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String)
    suspend fun clearSession()
}