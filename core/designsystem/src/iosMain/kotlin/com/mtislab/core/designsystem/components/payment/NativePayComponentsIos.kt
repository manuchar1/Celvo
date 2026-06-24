package com.mtislab.core.designsystem.components.payment

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.payment_apple_pay_unavailable
import org.jetbrains.compose.resources.stringResource
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.native.Platform
import platform.Foundation.NSData
import platform.Foundation.NSSelectorFromString
import platform.PassKit.PKPayment
import platform.PassKit.PKPaymentAuthorizationController
import platform.PassKit.PKPaymentAuthorizationControllerDelegateProtocol
import platform.PassKit.PKPaymentAuthorizationResult
import platform.PassKit.PKPaymentAuthorizationStatus
import platform.PassKit.PKPaymentButton
import platform.PassKit.PKPaymentButtonStyleBlack
import platform.PassKit.PKPaymentButtonTypeBuy
import platform.PassKit.PKPaymentToken
import platform.UIKit.UIControlEventTouchUpInside
import platform.darwin.NSObject

/**
 * iOS implementation of the native-pay contract.
 *
 * Produces:
 *  - A real Apple-branded [PKPaymentButton] via UIKit interop (required by
 *    Apple's Human Interface Guidelines — custom buttons are rejected).
 *  - A [PKPaymentAuthorizationController]-backed launcher that presents the
 *    system Apple Pay sheet and extracts the encrypted payment token for
 *    forwarding to Bank of Georgia.
 *
 * See [ApplePayConfig] for merchant configuration and `NativePayManagerIos`
 * in `core/data` for the availability check.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun NativePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    // Hold the *latest* click action across recompositions without forcing a
    // UIView rebuild. The ObjC target is stable; only the lambda behind it
    // tracks state.
    val latestOnClick by rememberUpdatedState(onClick)

    val target = remember {
        ApplePayButtonTarget(action = { latestOnClick() })
    }

    UIKitView(
        factory = {
            PKPaymentButton(
                paymentButtonType = PKPaymentButtonTypeBuy,
                paymentButtonStyle = PKPaymentButtonStyleBlack
            ).apply {
                addTarget(
                    target = target,
                    action = NSSelectorFromString(APPLE_PAY_TAP_SELECTOR),
                    forControlEvents = UIControlEventTouchUpInside
                )
                setEnabled(enabled)
            }
        },
        modifier = modifier.height(48.dp),
        update = { button ->
            button.setEnabled(enabled)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberNativePayLauncher(
    onTokenReceived: (token: String) -> Unit,
    onCancelled: () -> Unit,
    onError: (message: String) -> Unit
): NativePayLauncher {
    // Snapshot callbacks so the launcher can read the latest refs on every
    // invocation without being recreated each recomposition.
    val tokenCallback by rememberUpdatedState(onTokenReceived)
    val cancelCallback by rememberUpdatedState(onCancelled)
    val errorCallback by rememberUpdatedState(onError)

    // Resolve localized strings in @Composable scope; the launcher captures
    // them for use in PassKit completion handlers off the main thread.
    val applePayUnavailableMessage = stringResource(Res.string.payment_apple_pay_unavailable)

    return remember {
        object : NativePayLauncher {
            // Retain controller + delegate across the sheet's async lifecycle.
            // PassKit keeps the delegate as a weak ref — if we don't hold it,
            // it gets garbage-collected before didAuthorizePayment fires.
            private var currentController: PKPaymentAuthorizationController? = null
            private var currentDelegate: ApplePayDelegate? = null

            override fun launch(
                amountCents: Int,
                currencyCode: String,
                merchantName: String
            ) {
                val request = ApplePayConfig.buildPaymentRequest(
                    amountCents = amountCents,
                    currencyCode = currencyCode,
                    merchantName = merchantName
                )

                val delegate = ApplePayDelegate(
                    onTokenReceived = { token -> tokenCallback(token) },
                    onCancelled = { cancelCallback() },
                    onError = { message -> errorCallback(message) }
                )

                val controller = PKPaymentAuthorizationController(
                    paymentRequest = request
                )
                controller.delegate = delegate

                // Hold strong references for the duration of the sheet.
                currentController = controller
                currentDelegate = delegate

                controller.presentWithCompletion { presented ->
                    if (!presented) {
                        errorCallback(applePayUnavailableMessage)
                        currentController = null
                        currentDelegate = null
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal: ObjC-bridged target used by PKPaymentButton.addTarget.
// ─────────────────────────────────────────────────────────────────────────────

private const val APPLE_PAY_TAP_SELECTOR = "handleApplePayTap"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class ApplePayButtonTarget(
    private val action: () -> Unit
) : NSObject() {

    @ObjCAction
    fun handleApplePayTap() {
        action()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal: PKPaymentAuthorizationControllerDelegate bridge.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class ApplePayDelegate(
    private val onTokenReceived: (String) -> Unit,
    private val onCancelled: () -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), PKPaymentAuthorizationControllerDelegateProtocol {

    private var didAuthorize: Boolean = false

    /**
     * Called after the user authenticates (Face ID / Touch ID / passcode).
     * We extract the encrypted token payload and forward it to BOG.
     *
     * The `handler` MUST be invoked or the sheet hangs. Apple enforces a
     * ~30 second timeout, so we always respond synchronously — Success when
     * we successfully constructed the BOG payload, Failure when token
     * construction blew up (e.g. empty paymentData on the simulator).
     */
    override fun paymentAuthorizationController(
        controller: PKPaymentAuthorizationController,
        didAuthorizePayment: PKPayment,
        handler: (PKPaymentAuthorizationResult?) -> Unit
    ) {
        didAuthorize = true

        runCatching { didAuthorizePayment.token.toCelvoWalletTokenString() }
            .onSuccess { walletTokenString ->
                onTokenReceived(walletTokenString)
                handler(
                    PKPaymentAuthorizationResult(
                        status = PKPaymentAuthorizationStatus.PKPaymentAuthorizationStatusSuccess,
                        errors = null
                    )
                )
            }
            .onFailure { e ->
                onError(e.message ?: "Apple Pay token construction failed")
                handler(
                    PKPaymentAuthorizationResult(
                        status = PKPaymentAuthorizationStatus.PKPaymentAuthorizationStatusFailure,
                        errors = null
                    )
                )
            }
    }

    /**
     * Called when the sheet is dismissed for ANY reason (success, user swipe
     * to cancel, authorization failure). We must explicitly dismiss or the
     * controller leaks its underlying window scene.
     */
    override fun paymentAuthorizationControllerDidFinish(
        controller: PKPaymentAuthorizationController
    ) {
        controller.dismissWithCompletion(null)
        if (!didAuthorize) {
            onCancelled()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal: PKPaymentToken → Celvo wallet-pay token string.
//
// Our backend's POST /api/v1/payments/wallet-pay expects `walletToken` to be a
// stringified JSON object with exactly these three top-level keys (camelCase):
//
//   {
//     "paymentData":           { "version", "data", "signature", "header" },
//     "paymentMethod":         { "network", "displayName", "type" },
//     "transactionIdentifier": "..."
//   }
//
// The backend forwards this on to Bank of Georgia / Georgian Card, re-wrapping
// it as the gateway requires — so we MUST NOT add the outer `token` envelope
// from BOG's own docs here.
//
// Apple's PKPaymentToken.paymentData is itself a UTF-8 JSON blob (per the
// Apple Pay spec), so we parse it and embed it as a nested object — not as a
// Base64 string. `paymentMethod.type` must be the integer rawValue of
// PKPaymentMethodType (0=unknown, 1=debit, 2=credit, 3=prepaid, 4=store), not
// a string label — Georgian Card validates the JSON type strictly.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toUtf8StringOrEmpty(): String {
    val len = length.toInt()
    if (len == 0) return ""
    val bytesPtr = this.bytes ?: return ""
    return bytesPtr.readBytes(len).decodeToString()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun PKPaymentToken.toCelvoWalletTokenString(): String {
    // paymentData: NSData of UTF-8 JSON bytes per the Apple Pay spec. Empty
    // bytes indicate a non-production environment (simulator without a fully
    // provisioned test card) or a Wallet provisioning failure — forwarding
    // that to BOG yields a "code 199 Unknown Response" with no diagnostic
    // trail, so we fail loudly here instead.
    val rawPaymentData = paymentData.toUtf8StringOrEmpty()
    check(rawPaymentData.isNotBlank()) {
        "Apple Pay paymentData was empty. Apple Pay only works on physical " +
            "devices with a provisioned card — the simulator returns an empty token."
    }

    val parsedPaymentData: JsonObject = try {
        Json.parseToJsonElement(rawPaymentData).jsonObject
    } catch (e: Throwable) {
        throw IllegalStateException(
            "Apple Pay paymentData was not valid JSON: ${e.message}",
            e
        )
    }

    // PKPaymentMethodType is NSUInteger-typed in Kotlin/Native; .toInt() is
    // safe here because the enum's range (0..4 today, 5 for eMoney on newer
    // SDKs) fits comfortably in Int. We pass the raw integer through — the
    // payment gateway expects a JSON number, not a string label.
    val walletToken = buildCelvoWalletTokenJson(
        paymentData = parsedPaymentData,
        network = paymentMethod.network ?: "",
        displayName = paymentMethod.displayName ?: "",
        type = paymentMethod.type.toInt(),
        transactionIdentifier = transactionIdentifier
    )

    val serialized = walletToken.toString()
    logWalletTokenDiagnostics(serialized, walletToken.keys)
    return serialized
}

// Pure JSON builder split out from [toCelvoWalletTokenString] so it can be
// covered by a unit test without instantiating PKPaymentToken (no public
// initializer; can only be produced by the Apple Pay sheet).
internal fun buildCelvoWalletTokenJson(
    paymentData: JsonObject,
    network: String,
    displayName: String,
    type: Int,
    transactionIdentifier: String
): JsonObject = buildJsonObject {
    put("paymentData", paymentData)
    putJsonObject("paymentMethod") {
        put("network", network)
        put("displayName", displayName)
        put("type", type)
    }
    put("transactionIdentifier", transactionIdentifier)
}

// Debug-only diagnostic. Prints only the wallet-token *length* and *top-level
// keys* — never the body, because `paymentData.data` is encrypted card data
// and leaking it in logs is a PCI concern.
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
private fun logWalletTokenDiagnostics(serialized: String, topKeys: Set<String>) {
    if (!Platform.isDebugBinary) return
    println(
        "🍎 ApplePay walletToken | length=${serialized.length} " +
            "| topKeys=${topKeys.joinToString(",", "[", "]")}"
    )
}

