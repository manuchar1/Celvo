package com.mtislab.celvo.feature.store.presentation.store

import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem

sealed interface StoreAction {
    data class OnItemClick(val item: StoreItem) : StoreAction
    data class OnRegionClick(val item: StoreItem) : StoreAction
    data class OnBannerClick(val deepLink: String) : StoreAction
    data object OnSearchClick : StoreAction
    data object OnRetry : StoreAction
    data object OnRefresh : StoreAction

    // eSIM Switcher
    data object OnEsimSwitcherClick : StoreAction
    data object OnEsimSwitcherDismiss : StoreAction
    data class OnEsimSelected(val index: Int) : StoreAction

    // Active eSIM actions
    data object OnInstallClick : StoreAction
    data object OnTopUpClick : StoreAction
    data object OnDetailsClick : StoreAction
    data object OnSupportClick : StoreAction
    data object OnRetryLoadPackages : StoreAction

    /**
     * User tapped "Claim" CTA on an interactive promo banner.
     * The banner carries its own [MarketingBanner.promoCode] —
     * the ViewModel reads it from the banner object and persists
     * the claim via [PromoClaimRepository].
     */
    data class ClaimBannerPromo(val banner: MarketingBanner) : StoreAction

    // DEPRECATED: Replaced by ClaimBannerPromo. Kept temporarily to avoid
    // breaking compilation in other files. Remove once all references are updated.
    @Deprecated("Use ClaimBannerPromo instead", ReplaceWith("ClaimBannerPromo(banner)"))
    data class OnClaimPromoCode(val code: String) : StoreAction
}