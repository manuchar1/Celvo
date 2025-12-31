package com.mtislab.domain

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

/**
 * ეს ინტერფეისი განსაზღვრავს კონტრაქტს.
 * Android-ზე ის გამოიყენებს CredentialManager-ს.
 * iOS-ზე ის გამოიყენებს GIDSignIn-ს ან ASAuthorizationController-ს.
 */
interface GoogleAuthProvider {
    suspend fun getGoogleIdToken(): Resource<String, DataError.Local>
}