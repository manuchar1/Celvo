package com.mtislab.celvo.feature.store.domain.model

/**
 * Value object representing a promo code the user claimed from a
 * POST_PURCHASE marketing banner.
 *
 * Persisted locally via DataStore so it survives app restarts.
 * The [bannerId] links the claim back to a specific banner for UI
 * state mutation (title swap, CTA disabled, etc.).
 *
 * @property code       The promo code string (e.g. "FRIEND15", "WELCOME15").
 * @property bannerId   The marketing_banners.id that sourced this code.
 * @property claimedAtMillis Epoch millis of the claim — used for TTL checks.
 */
data class ClaimedPromo(
    val code: String,
    val bannerId: String,
    val claimedAtMillis: Long,
)