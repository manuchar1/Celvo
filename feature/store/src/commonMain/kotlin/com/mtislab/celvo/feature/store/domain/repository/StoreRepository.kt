package com.mtislab.celvo.feature.store.domain.repository

import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreCountriesData
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface StoreRepository {
    suspend fun getCountries(): Resource<StoreCountriesData, DataError.Remote>
    suspend fun getRegions(): Resource<List<StoreItem>, DataError.Remote>

    suspend fun getBanners(): Resource<List<MarketingBanner>, DataError.Remote>
    suspend fun getPackages(destination: String): Resource<List<EsimPackage>, DataError.Remote>
}