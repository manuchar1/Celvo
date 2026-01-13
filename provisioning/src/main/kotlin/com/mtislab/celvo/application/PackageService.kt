package com.mtislab.celvo.application

import com.mtislab.celvo.api.dto.AppBundleDto
import com.mtislab.celvo.api.dto.toDto
import com.mtislab.celvo.domain.esim.models.DataUnit
import com.mtislab.celvo.domain.esim.models.EsimBundle
import com.mtislab.celvo.domain.esim.ports.EsimProvider
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PackageService(
    private val esimProvider: EsimProvider
) {

    fun getPackages(destinationId: String): List<AppBundleDto> { // 👈 აქაც შევცვალეთ ტიპი
        val identifier = destinationId.trim()


        val filteredBundles = if (identifier.length == 2) {
            filterPackagesForCountry(identifier)
        } else {
            filterPackagesForRegion(identifier)
        }

        val bestValueBundleId = filteredBundles
            .filter { it.price.amount > BigDecimal.ZERO }
            .minByOrNull { bundle ->
                val mbAmount = if (bundle.dataAmount.unit == DataUnit.GB) {
                    bundle.dataAmount.value * 1024
                } else {
                    bundle.dataAmount.value
                }

                if (mbAmount == 0L) return@minByOrNull BigDecimal.valueOf(Double.MAX_VALUE)

                bundle.price.amount.divide(BigDecimal(mbAmount), 10, RoundingMode.HALF_UP)
            }?.id

        return filteredBundles
            .map { bundle ->
                val targetForFlag = if (identifier.length == 2) identifier else "GLOBAL"

                bundle.toDto(
                    targetIso = targetForFlag,
                    isBestValueOverride = (bundle.id == bestValueBundleId)
                )
            }
            .sortedBy { it.price }
    }

    private fun filterPackagesForCountry(isoCode: String): List<EsimBundle> {
        val targetIso = isoCode.uppercase()
        return esimProvider.getCatalogue().filter { bundle ->
            bundle.coverage.countries.any { it.isoCode.equals(targetIso, ignoreCase = true) }
        }
    }

    private fun filterPackagesForRegion(regionId: String): List<EsimBundle> {
        val lowerRegionId = regionId.lowercase()
        return esimProvider.getCatalogue().filter { bundle ->
            isBundleForRegion(bundle.name, bundle.description, lowerRegionId)
        }
    }

    private fun isBundleForRegion(name: String, desc: String?, regionId: String): Boolean {
        val text = (name + " " + (desc ?: "")).lowercase()
        return when (regionId) {
            "europe" -> text.contains("europe") || text.contains("euro") || text.contains("eu bundle")
            "asia" -> text.contains("asia") || text.contains("apac") || text.contains("pacific")
            "africa" -> text.contains("africa")
            "north-america" -> text.contains("north america") || (text.contains("usa") && text.contains("canada"))
            "latin-america" -> text.contains("latin") || text.contains("latam") || text.contains("south america")
            "middle-east" -> text.contains("middle east") || text.contains("gulf")
            "global" -> text.contains("global") || text.contains("world")
            else -> false
        }
    }
}