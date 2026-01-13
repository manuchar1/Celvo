package com.mtislab.celvo.feature.store.presentation.packages

import com.mtislab.celvo.feature.store.domain.model.EsimPackage

data class PackagesScreenState(
    val isLoading: Boolean = false,
    val allPackages: List<EsimPackage> = emptyList(),
    val filteredPackages: List<EsimPackage> = emptyList(),
    val selectedCategory: PackageCategory = PackageCategory.DATA,
    val error: String? = null,
    val showCategorySwitcher: Boolean = false
)

enum class PackageCategory {
    DATA,
    UNLIMITED
}