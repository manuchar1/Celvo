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

    val state = _state
        .onStart {
            // აქ ცარიელია, რადგან launchedEffect იძახებს
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
            is PackagesScreenAction.PackageClick -> Unit
            PackagesScreenAction.BackClick -> Unit
        }
    }

    private fun loadPackages(isoCode: String) {
        // [FIX - GUARD CLAUSE]
        // 1. თუ უკვე იტვირთება -> return
        // 2. თუ სიაში მონაცემები უკვე არის -> return (ეს აჩერებს ხელახალ ჩატვირთვას უკან დაბრუნებისას)
        if (_state.value.isLoading || _state.value.allPackages.isNotEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getPackages(isoCode)) {
                is Resource.Success -> {
                    val packages = result.data ?: emptyList()

                    val hasUnlimited = packages.any { it.isUnlimited }
                    val shouldShowSwitcher = packages.size >= 6 && hasUnlimited

                    val initialFilteredList = if (shouldShowSwitcher) {
                        filterPackages(packages, PackageCategory.DATA)
                    } else {
                        packages
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            allPackages = packages,
                            filteredPackages = initialFilteredList,
                            selectedCategory = PackageCategory.DATA,
                            showCategorySwitcher = shouldShowSwitcher
                        )
                    }
                }
                is Resource.Failure -> {
                    // სურვილის შემთხვევაში აქ შეგიძლია ერორის მესიჯი DataError-იდან ამოიღო
                    _state.update { it.copy(isLoading = false, error = "შეცდომა მონაცემების მიღებისას") }
                }
            }
        }
    }

    private fun selectCategory(category: PackageCategory) {
        if (_state.value.selectedCategory == category) return

        _state.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredPackages = filterPackages(currentState.allPackages, category)
            )
        }
    }

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