package com.mtislab.celvo.feature.myesim.data.repository

import com.mtislab.celvo.feature.myesim.data.mapper.toDomain
import com.mtislab.celvo.feature.myesim.data.remote.MyEsimRemoteService
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.auth.SessionController
import com.mtislab.core.domain.auth.SessionEvent
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class MyEsimRepositoryImpl(
    private val remoteService: MyEsimRemoteService,
    private val sessionController: SessionController
) : MyEsimRepository {

    private val mutex = Mutex()
    private var cachedEsims: List<UserEsim>? = null
    private var cacheTimestamp: Instant? = null

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val CACHE_TTL = 1.minutes
    }

    init {
        repositoryScope.launch {
            sessionController.sessionEvents.collect { event ->
                when (event) {
                    is SessionEvent.LoggedOut,
                    is SessionEvent.UserChanged -> {
                        clearCache()
                    }
                }
            }
        }
    }

    override suspend fun getActiveEsims(sync: Boolean): Resource<List<UserEsim>, DataError.Remote> {
        if (!sync) {
            mutex.withLock {
                val now = Clock.System.now()
                val isCacheValid = cachedEsims != null &&
                        cacheTimestamp != null &&
                        (now - cacheTimestamp!!) < CACHE_TTL

                if (isCacheValid) {
                    return Resource.Success(cachedEsims!!)
                }
            }
        }

        return when (val result = remoteService.getMyEsims()) {
            is Resource.Success -> {
                val domainList = result.data.map { it.toDomain() }

                mutex.withLock {
                    cachedEsims = domainList
                    cacheTimestamp = Clock.System.now()
                }

                Resource.Success(domainList)
            }
            is Resource.Failure -> Resource.Failure(result.error)
        }
    }

    override suspend fun getEsimBundles(iccid: String): Resource<EsimBundleInfo, DataError.Remote> {
        return when (val result = remoteService.getEsimBundles(iccid)) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Failure -> Resource.Failure(result.error)
        }
    }


    private suspend fun clearCache() {
        mutex.withLock {
            cachedEsims = null
            cacheTimestamp = null
        }
    }
}