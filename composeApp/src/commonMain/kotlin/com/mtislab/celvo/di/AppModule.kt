package com.mtislab.celvo.di

import com.mtislab.celvo.ui.MainScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainScreenViewModel)
}