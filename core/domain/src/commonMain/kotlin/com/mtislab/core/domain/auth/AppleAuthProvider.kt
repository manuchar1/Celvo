package com.mtislab.core.domain.auth

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

/**
 * Platform abstraction for native Apple Sign-In.
 *
 * On iOS this uses `ASAuthorizationAppleIDProvider` to obtain an
 * identity token natively (bottom-sheet experience, no browser redirect).
 * A fresh nonce is generated on every call, SHA-256-hashed before being
 * submitted to Apple, and returned RAW in [AppleSignInResult] so the
 * caller can forward it to Supabase.
 *
 * On Android this returns a stub failure — Apple Sign-In is only shown
 * on iOS (see RegisterScreen).
 *
 * The identity token + raw nonce are sent to Supabase via the IDToken
 * provider, exactly like the Google flow.
 */
interface AppleAuthProvider {
    suspend fun getAppleIdToken(): Resource<AppleSignInResult, DataError.Local>
}
