package com.mtislab.celvo.feature.store.data.mapper

import MarketingBannerDto
import com.mtislab.celvo.feature.store.data.dto.CountriesResponseDto
import com.mtislab.celvo.feature.store.data.dto.DestinationDto
import com.mtislab.celvo.feature.store.data.dto.RegionsResponseDto
import com.mtislab.celvo.feature.store.domain.model.StoreCountriesData
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.model.StoreItemType

import androidx.compose.ui.graphics.Color
import com.mtislab.celvo.feature.store.domain.model.BannerType
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
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


fun MarketingBannerDto.toDomain(): MarketingBanner {
    return MarketingBanner(
        id = id,
        title = title,
        description = description,
        imageUrl = assetUrl,
        ctaText = action.label,
        deepLink = action.deepLink,
        backgroundColor = parseColor(style.backgroundColor) ?: Color(0xFFF3F0FF), // Default fallback
        textColor = parseColor(style.textColor) ?: Color.Black,
        type = when (style.type.uppercase()) {
            "HERO" -> BannerType.HERO
            "SECONDARY" -> BannerType.SECONDARY
            else -> BannerType.UNKNOWN
        }
    )
}

/**
 * Parses a Hex string (e.g., "#F3F0FF" or "#FF0000") into a Compose Color.
 * Handles potential parsing errors gracefully.
 */
private fun parseColor(hex: String): Color? {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = if (cleanHex.length == 6) {
            // Add full alpha (FF) if missing
            "FF$cleanHex".toLong(16)
        } else {
            cleanHex.toLong(16)
        }
        Color(colorInt)
    } catch (e: Exception) {
        null
    }
}