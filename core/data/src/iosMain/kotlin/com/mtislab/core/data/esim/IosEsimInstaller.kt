package com.mtislab.core.data.esim

import com.mtislab.core.domain.esim.EsimInstallStatus
import com.mtislab.core.domain.esim.EsimInstallationData
import com.mtislab.core.domain.esim.EsimInstaller
import com.mtislab.core.domain.esim.InstallError
import com.mtislab.core.domain.logging.CelvoLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreTelephony.CTCellularPlanProvisioning
import platform.CoreTelephony.CTCellularPlanProvisioningAddPlanResult
import platform.CoreTelephony.CTCellularPlanProvisioningRequest

/**
 * iOS implementation of eSIM installer using CoreTelephony framework.
 *
 * Uses [CTCellularPlanProvisioning] to add eSIM plans. Unlike Android,
 * iOS handles the user consent dialog natively within the system call —
 * no resolution/Activity Result dance is needed.
 *
 * Requirements:
 * - Entitlement: `com.apple.developer.networking.cellular-provider` (carrier apps)
 *   OR use the standard `CTCellularPlanProvisioning` path (no entitlement needed
 *   for the basic addPlan flow on physical devices).
 * - Must be tested on a physical device — Simulator does not support eSIM.
 *
 * @param logger CelvoLogger for structured logging
 */
class IosEsimInstaller(
    private val logger: CelvoLogger
) : EsimInstaller {

    private val provisioning = CTCellularPlanProvisioning()

    override fun isEsimSupported(): Boolean {
        val supported = provisioning.supportsCellularPlan()
        logger.debug("[IosEsimInstaller] eSIM supported: $supported")
        return supported
    }

    override fun install(data: EsimInstallationData): Flow<EsimInstallStatus> = callbackFlow {
        logger.info("[IosEsimInstaller] Starting eSIM installation")
        logger.debug("[IosEsimInstaller] SMDP: ${data.smdpAddress}")

        // Validate device support
        if (!provisioning.supportsCellularPlan()) {
            logger.error("[IosEsimInstaller] Device does not support eSIM")
            trySend(EsimInstallStatus.Error(InstallError.DeviceNotSupported))
            close()
            return@callbackFlow
        }

        // Build provisioning request
        val request = CTCellularPlanProvisioningRequest().apply {
            address = data.smdpAddress
            matchingID = data.activationCode
        }

        trySend(EsimInstallStatus.Installing)

        // Initiate installation — iOS shows consent dialog natively
        provisioning.addPlanWith(request) { result ->
            logger.debug("[IosEsimInstaller] addPlanWith result: $result")

            when (result) {
                CTCellularPlanProvisioningAddPlanResult
                    .CTCellularPlanProvisioningAddPlanResultSuccess -> {
                    logger.info("[IosEsimInstaller] Installation successful")
                    trySend(EsimInstallStatus.Success)
                }

                CTCellularPlanProvisioningAddPlanResult
                    .CTCellularPlanProvisioningAddPlanResultFail -> {
                    logger.error("[IosEsimInstaller] Installation failed")
                    trySend(EsimInstallStatus.Error(InstallError.SystemError))
                }

                CTCellularPlanProvisioningAddPlanResult
                    .CTCellularPlanProvisioningAddPlanResultUnknown -> {
                    logger.error("[IosEsimInstaller] Unknown result")
                    trySend(EsimInstallStatus.Error(InstallError.SystemError))
                }

                else -> {
                    // Future enum values or user cancellation
                    logger.error("[IosEsimInstaller] Unexpected result: $result")
                    trySend(EsimInstallStatus.Error(InstallError.Cancelled))
                }
            }
            close()
        }

        awaitClose {
            logger.debug("[IosEsimInstaller] Flow closed")
        }
    }
}