package com.mtislab.celvo.infrastructure.esim.esimgo.adapter

import com.mtislab.celvo.api.dto.BundleDto
import com.mtislab.celvo.domain.esim.models.*
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class EsimGoMapper {

    fun toDomain(dto: BundleDto): EsimBundle {
        val safeDataAmount = dto.dataAmount ?: 0L
        val safeDuration = dto.duration ?: 0


        val roamingMap = mutableMapOf<String, MutableList<NetworkInfo>>()

        dto.roamingList?.forEach { roaming ->
            val iso = roaming.iso
            val netName = roaming.network ?: "Unknown Network"
            val bands = roaming.bands ?: listOf("4G")

            roamingMap.putIfAbsent(iso, mutableListOf())
            roamingMap[iso]?.add(NetworkInfo(netName, bands))
        }

        val allRawCountries = mutableSetOf<String>()
        dto.countries?.forEach { allRawCountries.add(it.iso) }
        dto.roamingList?.forEach { allRawCountries.add(it.iso) }

        val coverageCountries = allRawCountries.map { iso ->
            val locale = Locale("", iso)
            val name = locale.displayCountry.ifBlank { iso }

            CountryInfo(
                isoCode = iso.uppercase(),
                name = name,
                flagEmoji = getFlagEmoji(iso),
                networks = roamingMap[iso] ?: emptyList()
            )
        }

        val networks = mutableSetOf("4G")
        val has5G = dto.roamingList?.any { roaming ->
            roaming.bands?.any { band -> band.contains("5G", ignoreCase = true) } == true
        } == true

        if (has5G) networks.add("5G")

        val derivedCategory = if (coverageCountries.size > 1) BundleCategory.REGIONAL else BundleCategory.SINGLE_COUNTRY

        return EsimBundle(
            id = dto.name,
            name = dto.name,
            description = dto.description,
            dataAmount = DataAmount(safeDataAmount, DataUnit.MB),
            validity = ValidityPeriod(safeDuration, TimeUnit.DAYS),
            price = Price(BigDecimal.valueOf(dto.price ?: 0.0), dto.currency ?: "USD"),
            coverage = Coverage(coverageCountries, emptyList()),
            imageUrl = dto.imageUrl,
            type = BundleType.NEW,
            category = derivedCategory,
            networkTypes = networks.toList()
        )
    }

    private fun getFlagEmoji(iso: String): String {
        if (iso.length != 2) return iso
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41
        val firstChar = Character.codePointAt(iso, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(iso, 1) - asciiOffset + flagOffset
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}