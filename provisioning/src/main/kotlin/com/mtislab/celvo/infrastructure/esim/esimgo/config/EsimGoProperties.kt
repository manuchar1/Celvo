package com.mtislab.celvo.infrastructure.esim.esimgo.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for eSIMGo API.
 * Binds to application.yml properties prefixed with 'esimgo'.
 */
@ConfigurationProperties(prefix = "esimgo")
data class EsimGoProperties(
    val baseUrl: String = "https://api.esim-go.com/v2.5",
    val apiKey: String,
    val timeoutSeconds: Int = 30
)
