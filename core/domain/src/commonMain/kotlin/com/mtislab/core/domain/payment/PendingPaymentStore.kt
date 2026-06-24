package com.mtislab.core.domain.payment

/**
 * In-memory store for the most recent pending payment's orderId.
 *
 * Why this exists:
 * ────────────────
 * For card payments, the flow is:
 * 1. App calls POST /payments/initiate → gets redirectUrl (opens browser)
 * 2. User completes payment in browser
 * 3. Payment gateway redirects browser → server resolves bank ID → our UUID
 * 4. Server redirects to universal link with order_id=UUID → app opens
 *
 * If Android's intent-filter catches the gateway redirect BEFORE the server
 * processes it, the app receives a URL without our order_id.
 * This store acts as a fallback: the orderId is cached when the payment is
 * initiated, and consumed when the deep link arrives without one.
 *
 * For wallet payments, orderId is returned directly and navigated immediately,
 * so this store isn't needed — but we cache it anyway for consistency.
 *
 * Thread safety: All access is from the main thread (ViewModel + Composable),
 * so no synchronization is needed.
 */
object PendingPaymentStore {
    private var pendingOrderId: String? = null

    fun storeOrderId(orderId: String) {
        pendingOrderId = orderId
    }

    /**
     * Returns and clears the cached orderId.
     * Returns null if no pending payment exists.
     */
    fun consumeOrderId(): String? {
        val id = pendingOrderId
        pendingOrderId = null
        return id
    }

    fun clear() {
        pendingOrderId = null
    }
}
