package com.mtislab.celvo.feature.store.data.mapper

import androidx.compose.ui.graphics.Color
import com.mtislab.celvo.feature.store.data.dto.CountriesResponseDto
import com.mtislab.celvo.feature.store.data.dto.DestinationDto
import com.mtislab.celvo.feature.store.data.dto.MarketingBannerDto
import com.mtislab.celvo.feature.store.data.dto.RegionsResponseDto
import com.mtislab.celvo.feature.store.domain.model.BannerPlacement
import com.mtislab.celvo.feature.store.domain.model.BannerType
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreCountriesData
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.model.StoreItemType
import com.mtislab.celvo.feature.store.domain.model.SupportedCountry

fun CountriesResponseDto.toDomain(): StoreCountriesData {
    return StoreCountriesData(
        topPicks = topPicks.map { it.toDomain() },
        allCountries = allDestinations.map { it.toDomain() }
    )
}

fun RegionsResponseDto.toDomain(): List<StoreItem> {
    return regions.map { it.toDomain() }
}

fun DestinationDto.toDomain(): StoreItem {
    val count = coverageCount ?: 0

    return StoreItem(
        id = id,
        name = name,
        imageUrl = flagUrl,
        formattedPrice = "$minPrice $",
        type = if (type.equals("REGION", ignoreCase = true)) StoreItemType.REGION else StoreItemType.COUNTRY,
        supportedCountriesCount = count,
        supportedCountries = supportedCountries?.map { dto ->
            SupportedCountry(
                name = dto.name,
                flagUrl = dto.flagUrl
            )
        } ?: emptyList()
    )
}




/**
 * Maps the flat Supabase DTO (matching `marketing_banners` table) to the domain model.
 *
 * NOTE: [MarketingBanner.isClaimed] is always `false` from this mapper.
 * The ViewModel merges the claimed state from [PromoClaimRepository].
 */
fun MarketingBannerDto.toDomain(): MarketingBanner {
    return MarketingBanner(
        id = id,
        title = title.orEmpty(),
        description = description.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
        ctaText = ctaText ?: "გაიგე მეტი",
        deepLink = ctaLink.orEmpty(),
        backgroundColor = parseColor(backgroundColor) ?: Color(0xFFF3F0FF),
        textColor = parseColor(textColor) ?: Color.Black,
        type = when (type?.uppercase()) {
            "HERO" -> BannerType.HERO
            "SECONDARY" -> BannerType.SECONDARY
            else -> BannerType.UNKNOWN
        },
        placement = when (placement?.uppercase()) {
            "POST_PURCHASE" -> BannerPlacement.POST_PURCHASE
            else -> BannerPlacement.STORE
        },
        promoCode = promoCode?.takeIf { it.isNotBlank() },
        claimedTitle = claimedTitle?.takeIf { it.isNotBlank() },
        claimedDescription = claimedDescription?.takeIf { it.isNotBlank() },
        isClaimed = false, // Always false from API — merged by ViewModel
    )
}

/**
 * Parses a Hex string (e.g., "#F3F0FF" or "#FF0000") into a Compose Color.
 * Handles potential parsing errors gracefully.
 */
private fun parseColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorLong = if (cleanHex.length == 6) {
            "FF$cleanHex".toLong(16)
        } else {
            cleanHex.toLong(16)
        }
        Color(colorLong)
    } catch (_: Exception) {
        null
    }
}