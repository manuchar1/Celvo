package com.mtislab.celvo.feature.store.presentation.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PackagesScreenViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PackagesScreenState())

    // stateIn უზრუნველყოფს, რომ UI-ს მოტრიალებისას (Rotate) მონაცემები არ დაიკარგოს 5 წამი
    val state = _state
        .onStart {
            // თუ გვინდა რაიმე ლოგიკა ViewModel-ის შექმნისთანავე, აქ იწერება.
            // ამ შემთხვევაში LoadPackages იძახება UI-დან (LaunchedEffect), ამიტომ აქ ცარიელია.
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PackagesScreenState()
        )

    fun onAction(action: PackagesScreenAction) {
        when (action) {
            is PackagesScreenAction.LoadPackages -> loadPackages(action.isoCode)
            is PackagesScreenAction.SelectCategory -> selectCategory(action.category)

            // ამ ივენთებს მხოლოდ ვატარებთ, ლოგიკა UI-ში (Navigation) სრულდება,
            // ან თუ Analytics გვინდა, აქ ჩავსვამთ.
            is PackagesScreenAction.PackageClick -> Unit
            PackagesScreenAction.BackClick -> Unit
        }
    }

    // ... Imports

    private fun loadPackages(isoCode: String) {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getPackages(isoCode)) {
                is Resource.Success -> {
                    val packages = result.data ?: emptyList()

                    // 1. შევამოწმოთ პირობა: მინიმუმ 6 პაკეტი + მინიმუმ 1 ულიმიტო
                    val hasUnlimited = packages.any { it.isUnlimited }
                    val shouldShowSwitcher = packages.size >= 6 && hasUnlimited

                    // 2. განვსაზღვროთ საწყისი ფილტრაცია
                    val initialFilteredList = if (shouldShowSwitcher) {
                        // თუ სვიჩერი ჩანს -> ვაჩვენებთ მხოლოდ DATA-ს
                        filterPackages(packages, PackageCategory.DATA)
                    } else {
                        // თუ სვიჩერი არ ჩანს -> ვაჩვენებთ ყველას
                        packages
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            allPackages = packages,
                            filteredPackages = initialFilteredList,
                            selectedCategory = PackageCategory.DATA,
                            showCategorySwitcher = shouldShowSwitcher // <-- ვინახავთ State-ში
                        )
                    }
                }
                is Resource.Failure -> {
                    _state.update { it.copy(isLoading = false, error = "Error") }
                }
            }
        }
    }

    private fun selectCategory(category: PackageCategory) {
        // მხოლოდ მაშინ განვაახლოთ, თუ კატეგორია შეიცვალა
        if (_state.value.selectedCategory == category) return

        _state.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredPackages = filterPackages(currentState.allPackages, category)
            )
        }
    }

    // სუფთა ფუნქცია ფილტრაციისთვის (Pure Function)
    private fun filterPackages(
        allPackages: List<EsimPackage>,
        category: PackageCategory
    ): List<EsimPackage> {
        return when (category) {
            PackageCategory.DATA -> allPackages.filter { !it.isUnlimited }
            PackageCategory.UNLIMITED -> allPackages.filter { it.isUnlimited }
        }
    }
}