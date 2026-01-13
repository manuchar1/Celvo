package com.mtislab.celvo.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(SupabaseProperties::class)
class SecurityConfig(private val supabaseProperties: SupabaseProperties) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/error").permitAll()

                    // Actuator: Only health is public, others require authentication
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    .requestMatchers("/actuator/**").authenticated()

                    // Swagger/OpenAPI documentation
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                    ).permitAll()

                    // Public API endpoints (e.g., eSIM catalogue browsing)
                    .requestMatchers("/api/v1/esims/public/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()

                    // Destination browsing endpoints (public for home screen)
                    .requestMatchers("/api/v1/destinations/**").permitAll()

                    .requestMatchers("/api/v1/marketing/**").permitAll()
                    .requestMatchers( "/api/v1/provisioning/**").permitAll()

                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder())
                }
            }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(supabaseProperties.jwkSetUri).build()

        // Add issuer validation for Supabase
        val issuerValidator = JwtIssuerValidator(supabaseProperties.issuerUri)
        val timestampValidator = JwtTimestampValidator()

        val validators: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
            issuerValidator,
            timestampValidator
        )

        decoder.setJwtValidator(validators)
        return decoder
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Allow frontend origins - configure based on environment
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",      // Local development
            "http://localhost:5173",      // Vite default port
            "https://app.celvo.com",      // Production frontend
            "https://celvo.com"           // Production domain
        )

        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        )

        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        )

        configuration.exposedHeaders = listOf(
            "Authorization"
        )

        configuration.allowCredentials = true
        configuration.maxAge = 3600L // 1 hour preflight cache

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}
