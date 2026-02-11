package com.mtislab.celvo.feature.myesim.data.repository

import com.mtislab.celvo.feature.myesim.data.dto.EsimItemDto
import com.mtislab.celvo.feature.myesim.data.dto.UpdateLabelRequestDto
import com.mtislab.celvo.feature.myesim.data.remote.MyEsimRemoteService
import com.mtislab.celvo.feature.myesim.domain.model.EsimCountry
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.InstallationInfo
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

class MyEsimRepositoryImpl(
    private val remoteService: MyEsimRemoteService
) : MyEsimRepository {


    private var cachedEsims: List<UserEsim>? = null
    private val esimCache = mutableMapOf<String, UserEsim>()

    override suspend fun getActiveEsims(sync: Boolean): Resource<List<UserEsim>, DataError.Remote> {

        return when (val result = remoteService.getMyEsims()) {
            is Resource.Success -> {
                val domainList = result.data.esims.map { it.toDomain() }
                updateLocalCache(domainList)
                Resource.Success(domainList)
            }
            is Resource.Failure -> {
                Resource.Failure(result.error)
            }
        }
    }






    private fun updateLocalCache(list: List<UserEsim>) {
        cachedEsims = list
        esimCache.clear()
        list.forEach { esim -> esimCache[esim.id] = esim }
    }

    /**
     * MAPPER: EsimItemDto -> UserEsim
     * ახალი API სტრუქტურის მიხედვით.
     */
    private fun EsimItemDto.toDomain(): UserEsim {
        return UserEsim(
            id = this.iccid ?: "", // ID-დ ვიყენებთ ICCID-ს
            iccid = this.iccid ?: "",
            // profileStatus განსაზღვრავს რეალურ სტატუსს (ACTIVE, INSTALLED, etc.)
            status = EsimStatus.fromString(this.profileStatus ?: ""),
            statusDisplayName = this.statusLabel ?: "",
            // ფერის იგნორირება მოხდა, როგორც მოითხოვე

            userLabel = this.displayName, // displayName არის ახალი userLabel

            country = EsimCountry(
                code = this.primaryCountryCode ?: "",
                name = "", // სახელი არ მოდის, UI-მ კოდით უნდა დახატოს
                flagUrl = this.flagUrl ?: "",
                isRegion = false
            ),

            // ⚠️ ეს ველები არ მოდის ახალ API-ში, ამიტომ null
            dataUsage = null,
            validity = null,

            installation = InstallationInfo(
                smdpAddress = this.smdpAddress ?: "",
                activationCode = this.activationCode ?: "",
                manualCode = this.manualInstallCode ?: "",
                qrCodeUrl = "", // არ მოდის
                isReady = !this.activationCode.isNullOrEmpty()
            ),

            // UI-სთვის კრიტიკული ველი ("INSTALL" ღილაკის გამოსაჩენად)
            primaryAction = this.primaryAction,

            purchaseDateFormatted = "" // არ მოდის
        )
    }
}