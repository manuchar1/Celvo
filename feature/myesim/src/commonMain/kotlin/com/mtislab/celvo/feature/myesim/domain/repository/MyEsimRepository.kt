package com.mtislab.celvo.feature.myesim.domain.repository

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

/**
 * Repository interface for My eSim feature.
 * Defines the contract for accessing user's eSIM data.
 */
interface MyEsimRepository {

    /**
     * Fetches user's active eSIMs from the API.
     * @param sync Kept for backward compatibility. The new API might handle sync internally.
     * @return Resource containing list of active eSIMs or an error.
     */
    suspend fun getActiveEsims(sync: Boolean = false): Resource<List<UserEsim>, DataError.Remote>



}