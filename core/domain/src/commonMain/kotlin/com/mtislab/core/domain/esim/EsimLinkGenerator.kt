package com.mtislab.core.domain.esim

/**
 * Android: https://esimsetup.android.com/esim_qrcode_provisioning
 * iOS: https://esimsetup.apple.com/esim_qrcode_provisioning
 */
expect object EsimPlatformUrlProvider {
    val baseUrl: String
}

class EsimLinkGenerator {

    /**
     * Genarates Universal Link-ს eSIM-ის for installation.
     * Format: LPA:1$smdpAddress$activationCode
     */
    fun generateInstallLink(smdpAddress: String, activationCode: String): String {
        val lpaString = "LPA:1$$smdpAddress$$activationCode"
        return "${EsimPlatformUrlProvider.baseUrl}?carddata=$lpaString"
    }
}