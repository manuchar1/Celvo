package com.mtislab.celvo.api.controller

import com.mtislab.celvo.api.dto.AppBundleDto
import com.mtislab.celvo.api.dto.BundleDto
import com.mtislab.celvo.application.PackageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/provisioning")
@Tag(name = "Provisioning", description = "eSIM packages and orders")
class ProvisioningController(
    private val packageService: PackageService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/packages/{destination}")
    @Operation(
        summary = "Get packages for a destination",
        description = "Returns a list of eSIM bundles for a specific Country ISO (e.g., 'FR') or Region ID (e.g., 'europe'). Includes 'Best Value' logic and 5G indicators."
    )
    fun getPackages(@PathVariable destination: String): List<AppBundleDto> {
        logger.info("📱 API Request: Fetching packages for destination -> '$destination'")

        return packageService.getPackages(destination)
    }
}