package com.mtislab.core.domain.auth

/**
 * Result of a native Apple Sign-In flow.
 *
 * Apple requires a nonce that is hashed (SHA-256) before being sent to
 * Apple as part of the sign-in request. The RAW nonce is later forwarded
 * to Supabase together with the identity token so the backend can verify
 * that the token was minted for this exact sign-in session.
 *
 * @property idToken JWT identity token returned by Apple (ASAuthorizationAppleIDCredential.identityToken).
 * @property nonce   The UN-HASHED nonce that was used for this sign-in.
 */
data class AppleSignInResult(
    val idToken: String,
    val nonce: String,
)
