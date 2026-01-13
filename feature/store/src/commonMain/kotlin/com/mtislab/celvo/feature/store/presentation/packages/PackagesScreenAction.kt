package com.mtislab.celvo.feature.store.presentation.packages

import com.mtislab.celvo.feature.store.domain.model.EsimPackage

sealed interface PackagesScreenAction {
    data class LoadPackages(val isoCode: String) : PackagesScreenAction
    data class SelectCategory(val category: PackageCategory) : PackagesScreenAction
    data class PackageClick(val pkg: EsimPackage) : PackagesScreenAction
    data object BackClick : PackagesScreenAction
}