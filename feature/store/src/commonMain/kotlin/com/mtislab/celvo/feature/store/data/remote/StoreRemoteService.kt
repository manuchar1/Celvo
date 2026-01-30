package com.mtislab.celvo.feature.store.data.remote

import MarketingBannerDto
import com.mtislab.celvo.feature.store.data.dto.CountriesResponseDto
import com.mtislab.celvo.feature.store.data.dto.PackageDto
import com.mtislab.celvo.feature.store.data.dto.PaymentInitiateRequestDto
import com.mtislab.celvo.feature.store.data.dto.PaymentInitiateResponseDto
import com.mtislab.celvo.feature.store.data.dto.RegionsResponseDto
import com.mtislab.core.data.networking.get
import com.mtislab.core.data.networking.post
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.HttpClient
import io.ktor.client.utils.EmptyContent.contentType

class StoreRemoteService(
    private val httpClient: HttpClient
) {
    suspend fun getCountries(): Resource<CountriesResponseDto, DataError.Remote> {
        return httpClient.get(route = "/api/v1/destinations/countries")
    }

    suspend fun getRegions(): Resource<RegionsResponseDto, DataError.Remote> {
        return httpClient.get(route = "/api/v1/destinations/regions")
    }

    suspend fun getMarketingBanners(): Resource<List<MarketingBannerDto>, DataError.Remote> {
        return httpClient.get(route = "/api/v1/marketing/banners")
    }

    suspend fun getPackages(destination: String): Resource<List<PackageDto>, DataError.Remote> {
        return httpClient.get(route = "/api/v1/provisioning/packages/$destination")
    }


    suspend fun initiatePayment(request: PaymentInitiateRequestDto): Resource<PaymentInitiateResponseDto, DataError.Remote> {
        return httpClient.post(
            route = "/api/v1/payments/initiate",
            body = request
        )
    }
}