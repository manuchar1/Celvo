package com.mtislab.celvo

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.presentation.list.MyEsimListRoot

/**
 * Android implementation of PlatformEsimListScreen.
 *
 * Wraps [MyEsimListRoot] with:
 * 1. **Activity Result API launcher** for eSIM consent dialog resolution
 * 2. **READ_PHONE_STATE permission request** for Samsung NPE workaround polling
 *
 * ## Permission Strategy
 *
 * `READ_PHONE_STATE` is requested eagerly when the screen first appears. This
 * permission is required by [AndroidEsimInstaller]'s SubscriptionManager polling
 * to detect successful installations when Samsung's broadcast callback is lost.
 *
 * The permission is requested once on screen entry. If denied, the installer
 * proceeds in "broadcast-only" mode with a hard timeout fallback. This is a
 * degraded experience on Samsung devices but still functional on stock
 * Android/Pixel/OnePlus devices where broadcasts work reliably.
 *
 * ## Samsung Consent Dialog Behavior
 *
 * Samsung's consent dialog (LuiActivity) ALWAYS returns [android.app.Activity.RESULT_CANCELED]
 * regardless of user choice. The actual result arrives via a separate broadcast to
 * [AndroidEsimInstaller]'s BroadcastReceiver.
 *
 * Therefore, this launcher:
 * 1. Launches the PendingIntent's IntentSender
 * 2. Calls `onLaunched()` to clear the pending state in the ViewModel
 * 3. Does NOT interpret the Activity Result — it's unreliable on Samsung
 *
 * The install flow in the ViewModel continues collecting from the installer's Flow,
 * and the BroadcastReceiver (or SubscriptionManager polling fallback) will emit
 * the real Success/Error terminal state.
 */
@Composable
actual fun PlatformEsimListScreen(
    onEsimClick: (UserEsim) -> Unit,
    onAddEsimClick: () -> Unit
) {
    val context = LocalContext.current

    // ── READ_PHONE_STATE Permission ───────────────────────────────────────
    // Track whether permission has been granted. Checked once at composition
    // and updated when the permission launcher returns.
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("✅ READ_PHONE_STATE granted — Samsung polling workaround enabled")
        } else {
            println("⚠️ READ_PHONE_STATE denied — using broadcast-only mode")
        }
    }

    // Request permission eagerly on first composition if not already granted.
    // This ensures the permission is available before the user triggers install.
    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }
    }

    // ── Resolution Launcher ───────────────────────────────────────────────
    // Activity Result launcher for the eSIM consent dialog PendingIntent.
    // We register it but largely ignore the result — see class KDoc above.
    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        // Log for debugging only. Samsung returns RESULT_CANCELED even on approval.
        // The AndroidEsimInstaller's BroadcastReceiver/polling handles the real result.
        println("📱 eSIM Resolution Activity Result: resultCode=${activityResult.resultCode}")
    }

    // ── Stable Resolution Callback ────────────────────────────────────────
    // Called by MyEsimListRoot when the system requires user confirmation
    // (RESOLVABLE_ERROR). Launches the PendingIntent via Activity Result API.
    val onResolutionRequired = remember {
        { resolutionData: Any, onLaunched: () -> Unit ->
            val pendingIntent = resolutionData as? PendingIntent
            if (pendingIntent != null) {
                try {
                    println("🚀 Launching eSIM resolution PendingIntent...")
                    val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    resolutionLauncher.launch(request)
                    // Notify ViewModel that we've launched — clears the pending state
                    // so it doesn't try to re-launch on recomposition.
                    onLaunched()
                } catch (e: Exception) {
                    println("❌ Failed to launch resolution intent: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                println("❌ Resolution data is not a PendingIntent: ${resolutionData::class}")
            }
        }
    }

    MyEsimListRoot(
        onEsimClick = onEsimClick,
        onAddEsimClick = onAddEsimClick,
        onResolutionRequired = onResolutionRequired
    )
}