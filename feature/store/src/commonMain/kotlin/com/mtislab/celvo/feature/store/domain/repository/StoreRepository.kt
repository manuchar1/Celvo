package com.mtislab.celvo.feature.store.domain.repository

import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateRequest
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateResult
import com.mtislab.celvo.feature.store.domain.model.PromoValidationRequest
import com.mtislab.celvo.feature.store.domain.model.PromoValidationResult
import com.mtislab.celvo.feature.store.domain.model.StoreCountriesData
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentRequest
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentResult
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.model.EsimHomePackage
import com.mtislab.core.domain.payment.PaymentVerificationRepository
import com.mtislab.core.domain.payment.PaymentVerificationResult
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface StoreRepository : PaymentVerificationRepository {
    suspend fun getCountries(): Resource<StoreCountriesData, DataError.Remote>
    suspend fun getRegions(): Resource<List<StoreItem>, DataError.Remote>

    /**
     * Fetches marketing banners for the given screen [placement].
     *
     * Valid placements: `"HOME"`, `"STORE"`, `"POST_PURCHASE"`, `"UNIVERSAL"`.
     */
    suspend fun getBanners(placement: String): Resource<List<MarketingBanner>, DataError.Remote>

    suspend fun getPackages(destination: String): Resource<List<EsimPackage>, DataError.Remote>

    suspend fun getPackageById(id: String): EsimPackage?

    suspend fun initiatePayment(request: PaymentInitiateRequest): Resource<PaymentInitiateResult, DataError.Remote>


    suspend fun getEsimHome(): Resource<ActiveEsimHome?, DataError.Remote>

    suspend fun getEsimPackages(iccid: String): Resource<List<EsimHomePackage>, DataError.Remote>

    suspend fun validatePromo(
        request: PromoValidationRequest
    ): Resource<PromoValidationResult, DataError.Remote>



    suspend fun processWalletPayment(
        request: WalletPaymentRequest
    ): Resource<WalletPaymentResult, DataError.Remote>
}