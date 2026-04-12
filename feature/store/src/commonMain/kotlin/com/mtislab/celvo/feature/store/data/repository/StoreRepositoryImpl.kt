package com.mtislab.celvo.feature.store.data.repository

import com.mtislab.celvo.feature.store.data.dto.PaymentInitiateRequestDto
import com.mtislab.celvo.feature.store.data.dto.PromoValidationRequestDto
import com.mtislab.celvo.feature.store.data.dto.WalletPaymentRequestDto
import com.mtislab.celvo.feature.store.data.mapper.toDomain
import com.mtislab.celvo.feature.store.data.remote.StoreRemoteService
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
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.model.EsimHomePackage
import com.mtislab.core.domain.payment.PaymentVerificationResult
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

    /**
     * Banners are intentionally NOT cached — the backend filters by [placement]
     * and may return different sets depending on server-side targeting rules.
     */
    override suspend fun getBanners(placement: String): Resource<List<MarketingBanner>, DataError.Remote> {
        return remoteService.getMarketingBanners(placement).map { dtoList ->
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
            }
            domainList
        }
    }

    override suspend fun getPackageById(id: String): EsimPackage? {
        return packagesCache[id]
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
                redirectUrl = responseDto.redirectUrl,
                orderId = responseDto.orderId
            )
        }
    }


    /**
     * Intentionally NOT cached — the response contains live usage data
     * and real-time profile status from the SMDP+ server.
     */
    override suspend fun getEsimHome(): Resource<ActiveEsimHome?, DataError.Remote> {
        return remoteService.getEsimHome().map { dto -> dto.toDomain() }
    }

    override suspend fun getEsimPackages(iccid: String): Resource<List<EsimHomePackage>, DataError.Remote> {
        return remoteService.getEsimPackages(iccid).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }


    override suspend fun validatePromo(
        request: PromoValidationRequest
    ): Resource<PromoValidationResult, DataError.Remote> {
        val requestDto = PromoValidationRequestDto(
            code = request.code,
            cartValue = request.cartValue,
            bundleId = request.bundleId,
            countryIso = request.countryIso,
            regionId = request.regionId
        )
        return remoteService.validatePromo(requestDto).map { it.toDomain() }
    }


    override suspend fun processWalletPayment(
        request: WalletPaymentRequest
    ): Resource<WalletPaymentResult, DataError.Remote> {
        val requestDto = WalletPaymentRequestDto(
            sku = request.sku,
            bundleName = request.bundleName,
            amount = request.amount,
            currency = request.currency,
            paymentMethod = request.paymentMethod.name,
            walletToken = request.walletToken,
            promoCodeId = request.promoCodeId
        )
        return remoteService.processWalletPayment(requestDto).map { it.toDomain() }
    }

    override suspend fun verifyPayment(
        orderId: String
    ): Resource<PaymentVerificationResult, DataError.Remote> {
        return remoteService.verifyPayment(orderId).map { it.toDomain() }
    }

}