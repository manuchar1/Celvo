package com.mtislab.celvo.feature.myesim.data.remote

import com.mtislab.celvo.feature.myesim.data.dto.MyEsimsResponseDto
import com.mtislab.celvo.feature.myesim.data.dto.UpdateLabelRequestDto
import com.mtislab.celvo.feature.myesim.data.dto.UserEsimDto
import com.mtislab.core.data.networking.get
import com.mtislab.core.data.networking.post
import com.mtislab.core.data.networking.put
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class MyEsimRemoteService(
    private val httpClient: HttpClient
) {

    suspend fun getMyEsims(): Resource<MyEsimsResponseDto, DataError.Remote> {
        return httpClient.get(route = "/api/v1/esims/my-esims")
    }
}