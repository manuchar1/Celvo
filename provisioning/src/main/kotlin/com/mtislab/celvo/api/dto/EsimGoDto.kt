package com.mtislab.celvo.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CatalogueResponse(
    val bundles: List<BundleDto>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BundleDto(
    val name: String, // ID
    val description: String?,
    val price: Double?,
    val currency: String?,

    @JsonProperty("dataAmount")
    val dataAmount: Long?,

    val duration: Int?,
    val imageUrl: String?,


    @JsonProperty("roamingList")
    val roamingList: List<RoamingLocationDto>?,

    @JsonProperty("countries")
    val countries: List<LocationDto>?,

    @JsonProperty("network")
    val network: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RoamingLocationDto(
    val iso: String,
    val bands: List<String>?,
    @JsonProperty("network")
    val network: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocationDto(
    val iso: String,
    val name: String?
)