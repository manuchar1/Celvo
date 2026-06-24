package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.AppleSignInResult
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Native "Sign in with Apple" on iOS using Apple's AuthenticationServices.
 *
 * Flow:
 *   1. Generate a random raw nonce (UUID).
 *   2. Hash it with SHA-256 -> hex string and submit that to Apple as `nonce`.
 *   3. Apple returns an identity JWT whose `nonce` claim equals the hashed value.
 *   4. We return the JWT **and the RAW nonce** so Supabase can re-hash and verify.
 */
@Composable
actual fun rememberAppleAuthProvider(): AppleAuthProvider {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        object : AppleAuthProvider {
            override suspend fun getAppleIdToken(): Resource<AppleSignInResult, DataError.Local> {
                return suspendCancellableCoroutine { continuation ->
                    val rawNonce = NSUUID().UUIDString
                    val hashedNonce = sha256Hex(rawNonce.encodeToByteArray())

                    val request = ASAuthorizationAppleIDProvider().createRequest().apply {
                        setRequestedScopes(
                            listOf(
                                ASAuthorizationScopeFullName,
                                ASAuthorizationScopeEmail,
                            )
                        )
                        setNonce(hashedNonce)
                    }

                    val anchor: UIWindow? = viewController.view?.window

                    val delegate = AppleAuthDelegate(
                        rawNonce = rawNonce,
                        anchor = anchor,
                        onResult = { result ->
                            if (!continuation.isCompleted) continuation.resume(result)
                        },
                    )

                    val controller = ASAuthorizationController(
                        authorizationRequests = listOf(request),
                    ).apply {
                        setDelegate(delegate)
                        setPresentationContextProvider(delegate)
                    }

                    // Hold strong refs until the callback resumes the continuation.
                    continuation.invokeOnCancellation {
                        delegate.toString()
                        controller.toString()
                    }

                    controller.performRequests()
                }
            }
        }
    }
}

/**
 * ASAuthorizationController delegate + presentation-context provider.
 * Lives for the duration of one sign-in request.
 */
private class AppleAuthDelegate(
    private val rawNonce: String,
    private val anchor: UIWindow?,
    private val onResult: (Resource<AppleSignInResult, DataError.Local>) -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val tokenData = credential?.identityToken
        val token = tokenData?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
        }
        if (token.isNullOrBlank()) {
            onResult(Resource.Failure(DataError.Local.UNKNOWN))
        } else {
            onResult(Resource.Success(AppleSignInResult(idToken = token, nonce = rawNonce)))
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        // Code 1001 = user-canceled; others = failed / not-handled / invalid response.
        // All map to a generic local failure — the ViewModel surfaces it via the
        // custom CelvoTopNotification.
        println(
            "Apple Auth Error: ${didCompleteWithError.localizedDescription} | " +
                "domain=${didCompleteWithError.domain} code=${didCompleteWithError.code}"
        )
        onResult(Resource.Failure(DataError.Local.UNKNOWN))
    }

    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController,
    ): ASPresentationAnchor {
        return anchor
            ?: UIApplication.sharedApplication.keyWindow
            ?: UIWindow()
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Pure-Kotlin SHA-256 (FIPS 180-4).
//
// CommonCrypto is not available in this project's Kotlin/Native platform libs,
// and CryptoKit would require cinterop setup. Since we hash exactly one short
// string (a UUID) per sign-in, a pure-Kotlin implementation is negligible cost
// and removes all native dependency risk.
// ────────────────────────────────────────────────────────────────────────────

private val SHA256_K = intArrayOf(
    0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
    0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
    0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
    0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
    0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
    0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
    0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
    0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
    0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
    0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
    0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
    0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
    0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
    0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
    0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
    0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt(),
)

private fun sha256Hex(input: ByteArray): String {
    val h = intArrayOf(
        0x6a09e667.toInt(), 0xbb67ae85.toInt(), 0x3c6ef372.toInt(), 0xa54ff53a.toInt(),
        0x510e527f.toInt(), 0x9b05688c.toInt(), 0x1f83d9ab.toInt(), 0x5be0cd19.toInt(),
    )

    // Pad: append 0x80, then zeros, then 8-byte big-endian bit length.
    val bitLength = input.size.toLong() * 8L
    val padLen = ((56 - (input.size + 1) % 64) + 64) % 64
    val buf = ByteArray(input.size + 1 + padLen + 8)
    input.copyInto(buf)
    buf[input.size] = 0x80.toByte()
    for (i in 0 until 8) {
        buf[buf.size - 8 + i] = ((bitLength ushr ((7 - i) * 8)) and 0xff).toByte()
    }

    val w = IntArray(64)
    var offset = 0
    repeat(buf.size / 64) {
        for (i in 0 until 16) {
            w[i] = ((buf[offset].toInt() and 0xff) shl 24) or
                ((buf[offset + 1].toInt() and 0xff) shl 16) or
                ((buf[offset + 2].toInt() and 0xff) shl 8) or
                (buf[offset + 3].toInt() and 0xff)
            offset += 4
        }
        for (i in 16 until 64) {
            val s0 = w[i - 15].rotateRight(7) xor w[i - 15].rotateRight(18) xor (w[i - 15] ushr 3)
            val s1 = w[i - 2].rotateRight(17) xor w[i - 2].rotateRight(19) xor (w[i - 2] ushr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
        var e = h[4]; var f = h[5]; var g = h[6]; var hh = h[7]

        for (i in 0 until 64) {
            val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val ch = (e and f) xor (e.inv() and g)
            val t1 = hh + s1 + ch + SHA256_K[i] + w[i]
            val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val mj = (a and b) xor (a and c) xor (b and c)
            val t2 = s0 + mj
            hh = g; g = f; f = e; e = d + t1
            d = c; c = b; b = a; a = t1 + t2
        }
        h[0] += a; h[1] += b; h[2] += c; h[3] += d
        h[4] += e; h[5] += f; h[6] += g; h[7] += hh
    }

    val out = StringBuilder(64)
    val hexChars = "0123456789abcdef"
    for (i in 0 until 8) {
        val word = h[i]
        for (shift in intArrayOf(24, 16, 8, 0)) {
            val b = (word ushr shift) and 0xff
            out.append(hexChars[b ushr 4])
            out.append(hexChars[b and 0x0f])
        }
    }
    return out.toString()
}
