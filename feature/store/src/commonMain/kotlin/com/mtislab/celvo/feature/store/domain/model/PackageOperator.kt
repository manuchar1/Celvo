package com.mtislab.celvo.feature.store.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PackageOperator(
    val name: String,
    val networks: List<String>
)