package com.mtislab.celvo.feature.store.data.repository

import com.mtislab.celvo.feature.store.data.dto.PaymentInitiateRequestDto
import com.mtislab.celvo.feature.store.data.dto.PaymentInitiateResponseDto
import com.mtislab.celvo.feature.store.data.mapper.toDomain
import com.mtislab.celvo.feature.store.data.remote.StoreRemoteService
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateRequest
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateResult
import com.mtislab.celvo.feature.store.domain.model.StoreCountriesData
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.domain.utils.map

class StoreRepositoryImpl(
    private val remoteService: StoreRemoteService
) : StoreRepository {

    private var cachedCountries: StoreCountriesData? = null
    private var cachedRegions: List<StoreItem>? = null
    private val packagesCache = mutableMapOf<String, EsimPackage>()


    override suspend fun getCountries(): Resource<StoreCountriesData, DataError.Remote> {
        cachedCountries?.let { return Resource.Success(it) }
        return remoteService.getCountries().map { dto ->
            val domain = dto.toDomain()
            cachedCountries = domain
            domain
        }
    }

    override suspend fun getRegions(): Resource<List<StoreItem>, DataError.Remote> {
        cachedRegions?.let { return Resource.Success(it) }
        return remoteService.getRegions().map { dto ->
            val domain = dto.toDomain()
            cachedRegions = domain
            domain
        }
    }

    override suspend fun getBanners(): Resource<List<MarketingBanner>, DataError.Remote> {
        return remoteService.getMarketingBanners().map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun getPackages(destination: String): Resource<List<EsimPackage>, DataError.Remote> {
        return remoteService.getPackages(destination).map { dtoList ->
            val domainList = dtoList
                .map { it.toDomain() }
                .sortedWith(
                    compareByDescending<EsimPackage> { it.isBestValue }
                        .thenBy { it.price }
                )
            domainList.forEach { pkg ->
                packagesCache[pkg.id] = pkg
                 println("StoreRepo: Cached ID: ${pkg.id}")
            }

            domainList
        }
    }

    override suspend fun getPackageById(id: String): EsimPackage? {
        val pkg = packagesCache[id]
        return pkg
    }

    override suspend fun initiatePayment(request: PaymentInitiateRequest): Resource<PaymentInitiateResult, DataError.Remote> {
        val requestDto = PaymentInitiateRequestDto(
            amount = request.amount,
            sku = request.sku,
            bundleName = request.bundleName,
            currency = request.currency,
            language = request.language,
            theme = request.theme
        )

        return remoteService.initiatePayment(requestDto).map { responseDto ->
            PaymentInitiateResult(
                redirectUrl = responseDto.redirectUrl
            )
        }
    }}