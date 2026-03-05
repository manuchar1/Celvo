package com.mtislab.celvo.feature.myesim.data.remote

import com.mtislab.celvo.feature.myesim.data.dto.EsimBundlesResponseDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimItemDto
import com.mtislab.core.data.networking.get
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.HttpClient

class MyEsimRemoteService(
    private val httpClient: HttpClient
) {
    suspend fun getMyEsims(): Resource<List<EsimItemDto>, DataError.Remote> {
        return httpClient.get(route = "/api/v1/esims/my-esims")
    }


    suspend fun getEsimBundles(iccid: String): Resource<EsimBundlesResponseDto, DataError.Remote> {
        return httpClient.get(route = "/api/v1/esims/$iccid/bundles")
    }
}