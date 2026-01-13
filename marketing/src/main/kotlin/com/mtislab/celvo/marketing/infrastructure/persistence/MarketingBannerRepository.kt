package com.mtislab.celvo.marketing.infrastructure.persistence


import com.mtislab.celvo.marketing.domain.MarketingBanner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface MarketingBannerRepository : JpaRepository<MarketingBanner, UUID> {

    /**
     * Finds active banners efficiently.
     * Logic:
     * 1. Must be marked as active.
     * 2. 'validFrom' is either null (always valid) or in the past.
     * 3. 'validUntil' is either null (never expires) or in the future.
     */
    @Query("""
        SELECT b FROM MarketingBanner b 
        WHERE b.isActive = true 
        AND (b.validFrom IS NULL OR b.validFrom <= :now)
        AND (b.validUntil IS NULL OR b.validUntil >= :now)
        ORDER BY b.sortOrder ASC
    """)
    fun findActiveBanners(now: Instant): List<MarketingBanner>
}