package com.mtislab.core.domain.esim

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InstallEsimUseCase(
    private val installer: EsimInstaller
) {

    operator fun invoke(
        smdpAddress: String,
        activationCode: String,
        manualCode: String
    ): Flow<EsimInstallStatus> {

        if (!installer.isEsimSupported()) {
            return flow { emit(EsimInstallStatus.Error(InstallError.DeviceNotSupported)) }
        }

        val data = EsimInstallationData(
            smdpAddress = smdpAddress,
            activationCode = activationCode,
            manualCode = manualCode
        )
        return installer.install(data)
    }
}