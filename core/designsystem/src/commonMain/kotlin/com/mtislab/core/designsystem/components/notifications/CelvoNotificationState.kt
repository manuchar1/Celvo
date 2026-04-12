package com.mtislab.core.designsystem.components.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Immutable snapshot of a single notification to display.
 *
 * [id] is auto-generated to guarantee unique animation keys,
 * preventing the stale-`LaunchedEffect` bug where re-showing
 * the same text wouldn't re-trigger the auto-dismiss timer.
 */
@OptIn(ExperimentalUuidApi::class)
data class CelvoNotificationData(
    val id: String = Uuid.random().toString(),
    val message: String,
    val description: String? = null,
    val type: CelvoNotificationType = CelvoNotificationType.Success,
    val durationMillis: Long = 4000L,
)

/**
 * Thread-safe, queue-backed state holder for [CelvoNotificationHost].
 *
 * Guarantees that rapid-fire calls to [show] never drop notifications;
 * each one is enqueued and displayed sequentially after the previous
 * one is dismissed (either by timeout or user action).
 *
 * Create via [rememberCelvoNotificationState] inside a Composable scope.
 */
@Stable
class CelvoNotificationState {

    var currentData: CelvoNotificationData? by mutableStateOf(null)
        private set

    private val queue = ArrayDeque<CelvoNotificationData>()
    private val mutex = Mutex()

    /**
     * Enqueue a notification. If nothing is currently displayed,
     * it appears immediately; otherwise it waits its turn.
     */
    suspend fun show(data: CelvoNotificationData) {
        mutex.withLock {
            if (currentData == null) {
                currentData = data
            } else {
                queue.addLast(data)
            }
        }
    }

    /**
     * Non-suspending convenience overload for fire-and-forget calls
     * from coroutine scopes where you don't need to await the queue.
     *
     * NOTE: If called concurrently from multiple threads this is racy.
     * Prefer the suspending [show] from structured coroutines.
     */
    fun showImmediate(data: CelvoNotificationData) {
        if (currentData == null) {
            currentData = data
        } else {
            queue.addLast(data)
        }
    }

    /**
     * Dismiss the current notification. Automatically promotes the
     * next queued item, if any.
     */
    suspend fun dismiss() {
        mutex.withLock {
            currentData = queue.removeFirstOrNull()
        }
    }

    /** Non-suspending variant for click callbacks. */
    fun dismissImmediate() {
        currentData = queue.removeFirstOrNull()
    }
}

/**
 * Remember a [CelvoNotificationState] scoped to the current composition.
 * Hoist this at your Scaffold / root level and pass it down or provide
 * via CompositionLocal.
 */
@Composable
fun rememberCelvoNotificationState(): CelvoNotificationState {
    return remember { CelvoNotificationState() }
}


/**
 * CompositionLocal providing [CelvoNotificationState] throughout the tree.
 * Set at the root in App.kt, consumed by any feature screen.
 */
val LocalCelvoNotification = compositionLocalOf<CelvoNotificationState> {
    error("CelvoNotificationState not provided. Wrap your root in CompositionLocalProvider.")
}