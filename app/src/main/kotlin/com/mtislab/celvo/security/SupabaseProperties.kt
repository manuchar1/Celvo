package com.mtislab.celvo.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "supabase.jwt")
class SupabaseProperties {
    lateinit var issuerUri: String
    lateinit var jwkSetUri: String
    lateinit var audience: String
}