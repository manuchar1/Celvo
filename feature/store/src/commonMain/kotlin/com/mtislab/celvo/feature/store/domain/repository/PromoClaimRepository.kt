package com.mtislab.celvo.feature.store.domain.repository

import com.mtislab.celvo.feature.store.domain.model.ClaimedPromo
import kotlinx.coroutines.flow.Flow

/**
 * Local-only repository for persisting claimed promo codes from
 * POST_PURCHASE marketing banners.
 *
 * This is intentionally separate from [StoreRepository] because:
 * 1. It's purely local (DataStore), not remote (API).
 * 2. It crosses presentation boundaries — both StoreViewModel
 *    (banner UI) and CheckoutViewModel (auto-apply) observe it.
 * 3. It has a different lifecycle — data survives sessions,
 *    unlike the in-memory caches in StoreRepositoryImpl.
 *
 * Implementation: [PromoClaimRepositoryImpl] in the data layer.
 */
interface PromoClaimRepository {

    /**
     * Reactive stream of all currently claimed promos.
     * Emits a new list whenever claims change (add/clear).
     * The flow never completes under normal conditions.
     */
    fun observeClaims(): Flow<List<ClaimedPromo>>

    /**
     * Convenience reactive stream that emits the set of banner IDs
     * that have been claimed. Useful for quick `contains()` lookups
     * in the banner carousel without mapping the full list.
     */
    fun observeClaimedBannerIds(): Flow<Set<String>>

    /**
     * Returns the most recently claimed promo code, or `null` if none.
     * This is the code that should be auto-filled in Checkout.
     *
     * NOTE: This is a one-shot suspend call, not a flow. The Checkout
     * ViewModel calls this once on init, not reactively.
     */
    suspend fun getActivePromoCode(): String?

    /**
     * Persist a new claim. Overwrites any existing claim for the same
     * [ClaimedPromo.bannerId] (idempotent).
     */
    suspend fun claimPromo(bannerId: String, code: String)

    /**
     * Remove a specific claim (e.g. when user manually clears the promo).
     */
    suspend fun removeClaim(bannerId: String)

    /**
     * Nuclear option — clear everything. Called on logout to prevent
     * cross-session data leakage (same singleton caching issue you've
     * hit before with countries/regions caches).
     */
    suspend fun clearAll()
}