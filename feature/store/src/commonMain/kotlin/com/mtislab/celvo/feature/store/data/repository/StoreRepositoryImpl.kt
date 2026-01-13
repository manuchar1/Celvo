package com.mtislab.celvo.feature.store.data.repository

import com.mtislab.celvo.feature.store.data.mapper.toDomain
import com.mtislab.celvo.feature.store.data.remote.StoreRemoteService
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
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
            dtoList
                .map { it.toDomain() }
                .sortedWith(
                    compareByDescending<EsimPackage> { it.isBestValue }
                        .thenBy { it.price }
                )
        }
    }


}


