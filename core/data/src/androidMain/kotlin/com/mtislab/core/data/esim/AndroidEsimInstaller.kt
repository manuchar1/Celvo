package com.mtislab.core.data.esim

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.euicc.DownloadableSubscription
import android.telephony.euicc.EuiccManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.mtislab.core.domain.esim.EsimInstallStatus
import com.mtislab.core.domain.esim.EsimInstallationData
import com.mtislab.core.domain.esim.EsimInstaller
import com.mtislab.core.domain.esim.InstallError
import com.mtislab.core.domain.logging.CelvoLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * Android implementation of eSIM installer using [EuiccManager] API.
 *
 * ## Architecture
 *
 * The install flow uses [callbackFlow] to bridge the callback-based [EuiccManager]
 * API into a reactive [Flow] of [EsimInstallStatus] states. A [BroadcastReceiver]
 * captures the download result, and a parallel polling mechanism handles Samsung's
 * known firmware bugs.
 *
 * ## Samsung Resolution Flow & NPE Workaround
 *
 * Samsung's consent dialog (LuiActivity) has a known firmware bug:
 *
 * 1. **Lost Callback PendingIntent**: Samsung's `continueOperation()` crashes with
 *    NPE because the callback PendingIntent is lost during the resolution handoff.
 *    The download proceeds via Samsung's LPA service, but no result broadcast is
 *    sent back to our app.
 *
 * 2. **Always RESULT_CANCELED**: The Activity Result from the consent dialog ALWAYS
 *    returns `RESULT_CANCELED`, regardless of user choice. The actual result (if any)
 *    arrives via a separate broadcast.
 *
 * ### Workaround: Dual-Path Result Detection
 *
 * After emitting [EsimInstallStatus.ResolutionRequired], we start a parallel polling
 * loop that checks [SubscriptionManager] for newly installed profiles. This races
 * against the BroadcastReceiver:
 *
 * - **Non-Samsung devices**: Broadcast arrives → cancels verification → normal flow
 * - **Samsung (NPE case)**: No broadcast → verification detects profile → emits Success
 * - **Actual failure**: Neither broadcast nor profile within timeout → emits Error(Timeout)
 *
 * ## Stable PendingIntent Strategy
 *
 * Uses `activationCode.hashCode()` for deterministic PendingIntent request codes.
 * This prevents Samsung's resolution loop where new UUIDs on each retry cause the
 * OS to re-prompt or reject cached permission grants.
 *
 * ## Post-Install Activation
 *
 * We pass `switchAfterDownload = true` to [EuiccManager.downloadSubscription], which
 * tells the system to automatically activate the new profile after download completes.
 *
 * NOTE: [EuiccManager.switchToSubscription] requires `WRITE_EMBEDDED_SUBSCRIPTIONS`,
 * a signature|privileged permission that only system apps / carrier apps can hold.
 * Regular Play Store apps CANNOT call it. The `switchAfterDownload` flag is the
 * correct mechanism for non-system apps.
 *
 * ## Required Permission
 *
 * `android.permission.READ_PHONE_STATE` is needed for [SubscriptionManager] polling.
 * The permission check is performed at runtime — if not granted, the verification
 * fallback is skipped (broadcast-only mode), and the caller is expected to have
 * already requested this permission at the UI layer before triggering install.
 */
class AndroidEsimInstaller(
    private val context: Context,
    private val logger: CelvoLogger
) : EsimInstaller {

    companion object {
        private const val TAG = "AndroidEsimInstaller"
        private const val ACTION_DOWNLOAD_PREFIX = "com.mtislab.celvo.ESIM_DOWNLOAD_"

        private const val EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT =
            "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT"

        /** Max time to wait for post-resolution result (broadcast or profile detection). */
        private const val POST_RESOLUTION_TIMEOUT_MS = 120_000L

        /** Interval between SubscriptionManager polling checks. */
        private const val VERIFICATION_POLL_INTERVAL_MS = 3_000L

        /** Initial delay before first poll — give Samsung's LPA service time to start. */
        private const val VERIFICATION_INITIAL_DELAY_MS = 5_000L

        /** Timeout for the initial download broadcast (before resolution). */
        private const val INITIAL_DOWNLOAD_TIMEOUT_MS = 60_000L
    }

    private val euiccManager: EuiccManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.getSystemService(Context.EUICC_SERVICE) as? EuiccManager
        } else {
            null
        }
    }

    override fun isEsimSupported(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val isEnabled = euiccManager?.isEnabled == true
            logger.debug(
                "[$TAG] eSIM Support: SDK=${Build.VERSION.SDK_INT}, " +
                        "EuiccManager=${euiccManager != null}, enabled=$isEnabled"
            )
            isEnabled
        } else {
            logger.debug("[$TAG] eSIM not supported — SDK ${Build.VERSION.SDK_INT} < 28")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun install(data: EsimInstallationData): Flow<EsimInstallStatus> = callbackFlow {
        logger.info("[$TAG] ═══ Starting eSIM installation ═══")
        logger.debug("[$TAG] SMDP: ${data.smdpAddress}")
        logger.debug("[$TAG] ActivationCode: ${data.activationCode.take(20)}...")

        // ── Step 1: Validate eSIM support ─────────────────────────────────
        val manager = euiccManager
        if (manager == null || !manager.isEnabled) {
            logger.error("[$TAG] Device does not support eSIM")
            trySend(EsimInstallStatus.Error(InstallError.DeviceNotSupported))
            close()
            return@callbackFlow
        }

        // ── Step 2: Build stable action ID ────────────────────────────────
        val activationCode = buildActivationCode(data)
        val stableId = abs(activationCode.hashCode())
        val actionId = "$ACTION_DOWNLOAD_PREFIX$stableId"

        logger.debug("[$TAG] Stable action ID: $actionId (hash=$stableId)")
        logger.debug("[$TAG] Activation code: ${activationCode.take(60)}...")

        // ── Step 3: Check READ_PHONE_STATE permission for polling ─────────
        val hasReadPhoneState = hasReadPhoneStatePermission()
        logger.debug("[$TAG] READ_PHONE_STATE granted: $hasReadPhoneState")

        // ── Step 4: Snapshot current subscriptions for diff-based detection ─
        val preInstallIccids = if (hasReadPhoneState) getActiveIccids() else emptySet()
        logger.debug("[$TAG] Pre-install subscriptions: ${preInstallIccids.size} profiles")

        // ── Step 5: Emit installing state ─────────────────────────────────
        trySend(EsimInstallStatus.Installing)

        // ── Step 6: Track state ───────────────────────────────────────────
        // AtomicBoolean ensures thread-safe terminal state across broadcast thread
        // (main thread) and coroutine threads (polling/timeout).
        val terminalStateReached = AtomicBoolean(false)
        var resolutionEmitted = false
        var verificationJob: Job? = null
        var timeoutJob: Job? = null
        var receiverRegistered = false

        /**
         * Thread-safe terminal state emission.
         *
         * Uses [AtomicBoolean.compareAndSet] to ensure exactly one terminal state
         * is emitted, even if broadcast receiver and verification poll race to
         * completion on different threads.
         */
        fun emitTerminal(status: EsimInstallStatus, source: String) {
            if (!terminalStateReached.compareAndSet(false, true)) {
                logger.debug("[$TAG] Terminal state already reached — ignoring from $source")
                return
            }
            verificationJob?.cancel()
            timeoutJob?.cancel()
            logger.info("[$TAG] ═══ Terminal state from $source: $status ═══")
            trySend(status)
            close()
        }

        // ── Step 7: Register download BroadcastReceiver ───────────────────
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != actionId) return

                val detailedCode = intent.getIntExtra(
                    EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1
                )

                logger.info(
                    "[$TAG] Download broadcast: resultCode=$resultCode, " +
                            "detailedCode=$detailedCode, postResolution=$resolutionEmitted"
                )

                when (resultCode) {
                    EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK -> {
                        logger.info("[$TAG] Download succeeded via broadcast")
                        emitTerminal(EsimInstallStatus.Success, "broadcast (OK)")
                    }

                    EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR -> {
                        if (resolutionEmitted) {
                            // Second RESOLVABLE_ERROR after resolution = fatal
                            logger.error("[$TAG] Repeated RESOLVABLE_ERROR after resolution")
                            emitTerminal(
                                EsimInstallStatus.Error(InstallError.SystemError),
                                "broadcast (repeated RESOLVABLE_ERROR)"
                            )
                        } else {
                            handleResolvableError(intent)
                        }
                    }

                    EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_ERROR -> {
                        val error = mapDetailedCodeToError(detailedCode)
                        emitTerminal(EsimInstallStatus.Error(error), "broadcast (error)")
                    }

                    else -> {
                        logger.warn("[$TAG] Unknown result code: $resultCode")
                        emitTerminal(
                            EsimInstallStatus.Error(InstallError.SystemError),
                            "broadcast (unknown code=$resultCode)"
                        )
                    }
                }
            }

            /**
             * Handles RESOLVABLE_ERROR by extracting the resolution PendingIntent
             * and emitting [EsimInstallStatus.ResolutionRequired].
             *
             * After emission:
             * - The flow remains OPEN (do NOT close)
             * - The UI layer launches the PendingIntent via Activity Result API
             * - A verification polling job starts in parallel with this receiver
             * - Whichever detects the result first (broadcast or poll) wins via [emitTerminal]
             */
            private fun handleResolvableError(intent: Intent) {
                val resolutionIntent = extractResolutionIntent(intent)

                if (resolutionIntent != null) {
                    logger.debug("[$TAG] Resolution PendingIntent extracted successfully")
                    resolutionEmitted = true

                    // Cancel the initial download timeout — we're now in resolution phase
                    timeoutJob?.cancel()

                    // Emit to UI layer — flow stays open
                    trySend(EsimInstallStatus.ResolutionRequired(resolutionIntent))
                    logger.debug("[$TAG] Flow remains open — waiting for post-resolution result")

                    // ── Samsung NPE Workaround ────────────────────────────────
                    // Start polling SubscriptionManager in parallel with the receiver.
                    // If broadcast arrives first → verificationJob is cancelled.
                    // If verification detects profile first → emit Success via emitTerminal.
                    if (hasReadPhoneState) {
                        verificationJob = launch {
                            runPostResolutionVerification(preInstallIccids) { detected ->
                                if (detected) {
                                    emitTerminal(
                                        EsimInstallStatus.Success,
                                        "verification (profile detected)"
                                    )
                                } else {
                                    emitTerminal(
                                        EsimInstallStatus.Error(InstallError.Timeout),
                                        "verification (timeout)"
                                    )
                                }
                            }
                        }
                    } else {
                        // No READ_PHONE_STATE → can't poll, rely on broadcast + hard timeout
                        logger.warn(
                            "[$TAG] READ_PHONE_STATE not granted — " +
                                    "Samsung workaround disabled, broadcast-only mode"
                        )
                        timeoutJob = launch {
                            delay(POST_RESOLUTION_TIMEOUT_MS)
                            emitTerminal(
                                EsimInstallStatus.Error(InstallError.Timeout),
                                "post-resolution hard timeout (no permission)"
                            )
                        }
                    }
                } else {
                    logger.error("[$TAG] Failed to extract resolution PendingIntent from broadcast")
                    emitTerminal(
                        EsimInstallStatus.Error(InstallError.SystemError),
                        "resolution (no PendingIntent)"
                    )
                }
            }
        }

        // Register with RECEIVER_EXPORTED for Android 13+ (Tiramisu)
        val intentFilter = IntentFilter(actionId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, intentFilter)
        }
        receiverRegistered = true
        logger.debug("[$TAG] BroadcastReceiver registered for: $actionId")

        // ── Step 8: Build callback PendingIntent ──────────────────────────
        val callbackIntent = Intent(actionId).apply {
            setPackage(context.packageName)
        }

        // FLAG_MUTABLE is required on Android 12+ (API 31) for EuiccManager
        // because the system needs to write extras (result codes) into the Intent.
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            stableId,
            callbackIntent,
            pendingIntentFlags
        )

        // ── Step 9: Create subscription and initiate download ─────────────
        val subscription = DownloadableSubscription.forActivationCode(activationCode)

        try {
            logger.info("[$TAG] Calling EuiccManager.downloadSubscription()...")

            // switchAfterDownload = true → system activates the profile post-download.
            //
            // NOTE: We use this instead of calling switchToSubscription() manually,
            // because switchToSubscription() requires WRITE_EMBEDDED_SUBSCRIPTIONS
            // (a signature|privileged permission only system/carrier apps can hold).
            // The switchAfterDownload flag is the correct approach for regular apps.
            manager.downloadSubscription(subscription, true, pendingIntent)
            logger.info("[$TAG] downloadSubscription() dispatched — awaiting callback")

            // Start a safety timeout for the initial download phase (pre-resolution).
            // If no broadcast arrives at all, something is fundamentally broken.
            timeoutJob = launch {
                delay(INITIAL_DOWNLOAD_TIMEOUT_MS)
                if (!resolutionEmitted) {
                    emitTerminal(
                        EsimInstallStatus.Error(InstallError.Timeout),
                        "initial download timeout"
                    )
                }
                // If resolution was emitted, this timeout was already cancelled
                // and replaced by the post-resolution timeout/verification.
            }
        } catch (e: SecurityException) {
            logger.error("[$TAG] SecurityException during download: ${e.message}")
            emitTerminal(EsimInstallStatus.Error(InstallError.NotAllowed), "exception")
        } catch (e: Exception) {
            logger.error("[$TAG] Exception during download: ${e.message}")
            emitTerminal(EsimInstallStatus.Error(InstallError.SystemError), "exception")
        }

        // ── Step 10: Cleanup on flow cancellation/close ───────────────────
        awaitClose {
            logger.debug("[$TAG] Flow closing — cleaning up")
            verificationJob?.cancel()
            timeoutJob?.cancel()

            if (receiverRegistered) {
                try {
                    context.unregisterReceiver(receiver)
                    logger.debug("[$TAG] BroadcastReceiver unregistered")
                } catch (e: IllegalArgumentException) {
                    logger.debug("[$TAG] Receiver already unregistered: ${e.message}")
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Post-Resolution Verification (Samsung NPE Workaround)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Polls [SubscriptionManager] to detect a newly installed eSIM profile.
     *
     * Compares current ICCIDs against [preInstallIccids] snapshot taken before
     * installation started. If a new ICCID appears, the profile was installed
     * successfully despite Samsung's lost broadcast.
     *
     * This runs in parallel with the BroadcastReceiver. The caller wraps it in
     * a [Job] that gets cancelled if the broadcast arrives first (via `emitTerminal`).
     *
     * @param preInstallIccids ICCIDs present before installation started
     * @param onResult Callback: `true` if new profile detected, `false` if timeout
     */
    @SuppressLint("MissingPermission")
    private suspend fun runPostResolutionVerification(
        preInstallIccids: Set<String>,
        onResult: (detected: Boolean) -> Unit
    ) {
        logger.info("[$TAG] Starting post-resolution verification (Samsung NPE workaround)")

        // Validate we can actually read subscriptions
        if (!hasReadPhoneStatePermission()) {
            logger.error(
                "[$TAG] READ_PHONE_STATE not granted — cannot poll SubscriptionManager. " +
                        "Falling back to broadcast-only mode."
            )
            // Don't call onResult — let the hard timeout handle it
            return
        }

        logger.debug(
            "[$TAG] Polling config: interval=${VERIFICATION_POLL_INTERVAL_MS}ms, " +
                    "timeout=${POST_RESOLUTION_TIMEOUT_MS}ms, " +
                    "initialDelay=${VERIFICATION_INITIAL_DELAY_MS}ms"
        )

        // Give Samsung's LPA service time to start the actual download
        delay(VERIFICATION_INITIAL_DELAY_MS)

        val startTime = System.currentTimeMillis()
        var pollCount = 0

        while (System.currentTimeMillis() - startTime < POST_RESOLUTION_TIMEOUT_MS) {
            pollCount++
            val currentIccids = getActiveIccids()
            val newProfiles = currentIccids - preInstallIccids

            if (newProfiles.isNotEmpty()) {
                logger.info(
                    "[$TAG] New eSIM profile detected via SubscriptionManager! " +
                            "(poll #$pollCount, elapsed=${System.currentTimeMillis() - startTime}ms)"
                )
                logger.debug("[$TAG] New ICCIDs: $newProfiles")
                onResult(true)
                return
            }

            if (pollCount % 5 == 0) {
                val elapsed = System.currentTimeMillis() - startTime
                logger.debug(
                    "[$TAG] Verification poll #$pollCount: " +
                            "no new profile (elapsed=${elapsed}ms, " +
                            "current=${currentIccids.size} subs)"
                )
            }

            delay(VERIFICATION_POLL_INTERVAL_MS)
        }

        logger.error(
            "[$TAG] Post-resolution verification timed out " +
                    "after ${POST_RESOLUTION_TIMEOUT_MS}ms ($pollCount polls)"
        )
        onResult(false)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Checks whether `READ_PHONE_STATE` runtime permission is granted.
     */
    private fun hasReadPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns all known subscription ICCIDs from [SubscriptionManager].
     *
     * Used for diff-based detection of newly installed profiles.
     * Returns empty set if permission is not granted or API is unavailable.
     */
    @SuppressLint("MissingPermission")
    private fun getActiveIccids(): Set<String> {
        if (!hasReadPhoneStatePermission()) return emptySet()

        return try {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                    as? SubscriptionManager ?: return emptySet()

            val subscriptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                subManager.completeActiveSubscriptionInfoList
            } else {
                @Suppress("DEPRECATION")
                subManager.activeSubscriptionInfoList
            }

            subscriptions
                ?.mapNotNull { it.iccId }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()
        } catch (e: SecurityException) {
            logger.warn("[$TAG] SecurityException reading subscriptions: ${e.message}")
            emptySet()
        } catch (e: Exception) {
            logger.warn("[$TAG] Error reading subscriptions: ${e.message}")
            emptySet()
        }
    }

    /**
     * Extracts the resolution [PendingIntent] from a RESOLVABLE_ERROR broadcast.
     *
     * Uses the typed getParcelableExtra on Android 13+ (Tiramisu) for SDK compliance.
     */
    private fun extractResolutionIntent(intent: Intent): PendingIntent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT,
                PendingIntent::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_EMBEDDED_SUBSCRIPTION_RESOLUTION_INTENT)
        }
    }

    /**
     * Builds the LPA activation code string from [EsimInstallationData].
     *
     * Handles multiple input formats:
     * - `LPA:1$<SMDP>$<ActivationCode>` → used as-is (already in LPA format)
     * - Raw code containing `$` → used as-is (assumed to be a complete code)
     * - SMDP + activation code → assembled into `LPA:1$smdp$code`
     * - Fallback: raw activation code or manual install code
     */
    private fun buildActivationCode(data: EsimInstallationData): String {
        // Priority 1: Manual code already in LPA format
        if (data.manualCode.startsWith("LPA:")) {
            return data.manualCode
        }

        // Priority 2: Manual code with other formats
        if (data.manualCode.isNotBlank()) {
            return if (data.manualCode.contains("$")) {
                data.manualCode
            } else if (data.smdpAddress.isNotBlank()) {
                "LPA:1\$${data.smdpAddress}\$${data.manualCode}"
            } else {
                data.manualCode
            }
        }

        // Priority 3: SMDP + activation code → LPA format
        if (data.smdpAddress.isNotBlank() && data.activationCode.isNotBlank()) {
            return "LPA:1\$${data.smdpAddress}\$${data.activationCode}"
        }

        // Priority 4: Raw activation code or manual code fallback
        return data.activationCode.ifBlank { data.manualCode }
    }

    /**
     * Maps [EuiccManager] detailed error codes to domain [InstallError].
     *
     * Reference: https://developer.android.com/reference/android/telephony/euicc/EuiccManager
     *
     * The detailed codes provide more specificity than the top-level result codes,
     * enabling user-friendly error messages in the UI layer.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun mapDetailedCodeToError(detailedCode: Int): InstallError {
        logger.debug("[$TAG] Mapping detailedCode=$detailedCode to InstallError")

        return when (detailedCode) {
            // Carrier restrictions
            EuiccManager.ERROR_CARRIER_LOCKED -> InstallError.CarrierLocked
            EuiccManager.ERROR_INCOMPATIBLE_CARRIER -> InstallError.NotAllowed
            EuiccManager.ERROR_DISALLOWED_BY_PPR -> InstallError.NotAllowed

            // Invalid profile data
            EuiccManager.ERROR_INVALID_ACTIVATION_CODE -> InstallError.InvalidActivationCode
            EuiccManager.ERROR_INVALID_CONFIRMATION_CODE -> InstallError.InvalidActivationCode

            // Device/hardware issues
            EuiccManager.ERROR_EUICC_INSUFFICIENT_MEMORY -> InstallError.InsufficientMemory
            EuiccManager.ERROR_EUICC_MISSING -> InstallError.DeviceNotSupported
            EuiccManager.ERROR_UNSUPPORTED_VERSION -> InstallError.DeviceNotSupported

            // Timeouts
            EuiccManager.ERROR_TIME_OUT -> InstallError.Timeout

            // Catch-all
            else -> {
                logger.warn("[$TAG] Unmapped detailed error code: $detailedCode")
                InstallError.SystemError
            }
        }
    }
}