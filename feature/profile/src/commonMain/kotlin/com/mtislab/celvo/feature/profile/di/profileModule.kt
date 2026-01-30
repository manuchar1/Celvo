package com.mtislab.celvo.feature.profile.di


import com.mtislab.celvo.feature.profile.data.remote.ProfileService
import com.mtislab.celvo.feature.profile.data.repository.ProfileRepositoryImpl
import com.mtislab.celvo.feature.profile.domain.repository.ProfileRepository
import com.mtislab.celvo.feature.profile.presentation.ProfileViewModel
import com.mtislab.celvo.feature.profile.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileModule = module {

    singleOf(::ProfileService)
    singleOf(::ProfileRepositoryImpl) bind ProfileRepository::class
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)


}