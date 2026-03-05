package com.mtislab.core.domain.model

/**
 * Screen-agnostic UI model for [ProductInfoCard].
 *
 * Each feature module maps its own domain model into this class via an
 * extension function (e.g. EsimPackage.toPackageInfoCardData() in feature:store).
 * This ensures zero domain-type leakage into core:designsystem.
 *
 * @param dataAmountDisplay  Pre-formatted string e.g. "2 GB".
 * @param validityDisplay    Pre-formatted string.
 * @param countryName        Localised country/region name.
 * @param isoCode            ISO 3166-1 alpha-2 code used to build the flag URL.
 * @param badgeType          Drives [CountryBadge] rendering: [BadgeType.Country] or [BadgeType.Region].
 * @param region             Region key forwarded to [CountryBadge] for icon resolution.
 * @param primaryOperator    First operator name displayed in the networks row.
 * @param additionalOperatorCount Number of operators beyond the first (shown as "+N").
 */
data class PackageInfoCardData(
    val dataAmountDisplay: String,
    val validityDisplay: String,
    val countryName: String,
    val isoCode: String,
    val badgeType: BadgeType,
    val region: String,
    val primaryOperator: String,
    val additionalOperatorCount: Int,
) {
    /** Drives how [CountryBadge] renders the leading flag/icon. */
    enum class BadgeType { Country, Region }
}