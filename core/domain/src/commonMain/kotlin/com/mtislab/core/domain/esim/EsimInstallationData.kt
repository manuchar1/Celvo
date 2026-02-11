package com.mtislab.core.domain.esim

data class EsimInstallationData(
    val smdpAddress: String,
    val activationCode: String,
    val manualCode: String
)