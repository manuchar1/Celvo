package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OperatorDto(
    val name: String,
    val networks: List<String>
)