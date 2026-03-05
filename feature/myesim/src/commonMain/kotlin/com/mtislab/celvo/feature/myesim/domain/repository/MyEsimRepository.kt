package com.mtislab.celvo.feature.myesim.domain.repository

import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource


interface MyEsimRepository {

    /**
     * Fetches user's active eSIMs.
     * @param sync Kept for backward compatibility. The new API might handle sync internally.
     * @return Resource containing list of active eSIMs or an error.
     */
    suspend fun getActiveEsims(sync: Boolean = false): Resource<List<UserEsim>, DataError.Remote>


    suspend fun getEsimBundles(iccid: String): Resource<EsimBundleInfo, DataError.Remote>



}