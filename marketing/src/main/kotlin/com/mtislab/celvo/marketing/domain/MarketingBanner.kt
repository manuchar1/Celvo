package com.mtislab.celvo.marketing.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "marketing_banners")
data class MarketingBanner(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    // The transparent mascot image URL from Supabase Storage
    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    // Action Button & Deep Link
    @Column(name = "cta_text")
    val ctaText: String?,

    @Column(name = "cta_link", nullable = false)
    val ctaLink: String,

    // Server-Driven UI Properties
    @Column(name = "background_color", nullable = false)
    val backgroundColor: String,

    @Column(name = "text_color", nullable = false)
    val textColor: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "banner_type")
    val type: BannerType,

    // Business Logic Fields
    @Column(name = "sort_order")
    val sortOrder: Int = 0,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "valid_from")
    val validFrom: Instant? = null,

    @Column(name = "valid_until")
    val validUntil: Instant? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: Instant? = null
)