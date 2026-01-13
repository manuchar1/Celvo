package com.mtislab.celvo.infrastructure.esim.esimgo.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

/**
 * Spring configuration for eSIMGo infrastructure.
 */
@Configuration
@EnableConfigurationProperties(EsimGoProperties::class)
class EsimGoConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun restClientBuilder(properties: EsimGoProperties): RestClient.Builder {
        return RestClient.builder()
            .requestFactory(
                JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds.toLong()))
                        .build()
                )
            )
            .requestInterceptor(loggingInterceptor())
    }


    private fun loggingInterceptor() = ClientHttpRequestInterceptor { request, body, execution ->
        log.info("┌─── EsimGo HTTP Request ───")
        log.info("│ Method: ${request.method}")
        log.info("│ URI: ${request.uri}")
        log.info("│ Headers:")
        request.headers.forEach { name, values ->
            val displayValue = if (name.equals("X-API-KEY", ignoreCase = true)) {
                values.map { it.take(8) + "***" }
            } else {
                values
            }
            log.info("│   $name: $displayValue")
        }
        log.info("└───────────────────────────")

        execution.execute(request, body)
    }
}
