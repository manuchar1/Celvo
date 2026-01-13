package com.mtislab.celvo.marketing.api

import com.mtislab.celvo.marketing.infrastructure.persistence.MarketingBannerRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/marketing")
@Tag(
    name = "Marketing Module",
    description = "Endpoints for managing marketing banners, promotional campaigns, and special offers."
)
class MarketingController(
    private val repository: MarketingBannerRepository
) {

    @GetMapping("/banners")
    @Operation(
        summary = "Retrieve active marketing banners",
        description = "Returns a list of banners that are marked as active (`isActive = true`) and fall within the valid date range (`validFrom` / `validUntil`). The results are ordered by priority."
    )
    fun getBanners(): List<BannerDto> {
        val entities = repository.findActiveBanners(Instant.now())
        return entities.map { it.toDto() }
    }
}