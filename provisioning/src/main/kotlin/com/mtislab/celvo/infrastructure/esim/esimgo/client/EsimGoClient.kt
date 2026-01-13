package com.mtislab.celvo.infrastructure.esim.esimgo.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.mtislab.celvo.api.dto.CatalogueResponse
import com.mtislab.celvo.infrastructure.esim.esimgo.config.EsimGoProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Low-level HTTP client for eSIMGo API v2.5.
 * Handles authentication, request construction, and error handling.
 */
@Component
class EsimGoClient(
    private val properties: EsimGoProperties,
    private val objectMapper: ObjectMapper,
    restClientBuilder: RestClient.Builder
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient: RestClient = restClientBuilder
        .baseUrl(properties.baseUrl)
        .defaultHeader("X-API-KEY", properties.apiKey)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultStatusHandler(HttpStatusCode::isError) { _, response ->
            try {
                val errorBody = response.body.readAllBytes()
                val error = objectMapper.readValue(errorBody, ErrorResponse::class.java)

                log.error("EsimGo API Error: ${error.message}, Code: ${error.code}")

                throw EsimGoApiException(
                    message = error?.message ?: "Unknown error from EsimGo",
                    code = error?.code,
                    statusCode = response.statusCode.value()
                )
            } catch (e: Exception) {
                if (e is EsimGoApiException) throw e
                throw EsimGoApiException("Failed to parse error response: ${response.statusCode}")
            }
        }
        .build()

    /**
     * Fetches the catalogue with pagination support.
     * @param page The page number to fetch (starts at 1)
     * @param perPage Number of items per page (default: 100)
     */
    fun getCatalogue(page: Int = 1, perPage: Int = 100): CatalogueResponse {
        log.debug("Fetching catalogue from eSIMGo (page: {}, perPage: {})", page, perPage)
        return restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/catalogue")
                    .queryParam("page", page)
                    .queryParam("perPage", perPage)
                    .build()
            }
            .retrieve()
            .body<CatalogueResponse>()
            ?: throw EsimGoApiException("Empty catalogue response")
    }





}

class EsimGoApiException(
    message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)

data class ErrorResponse(
    val message: String?,
    val code: String?
)