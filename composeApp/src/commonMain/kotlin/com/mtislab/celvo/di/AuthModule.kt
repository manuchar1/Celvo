package com.mtislab.celvo.di

import com.mtislab.core.data.repository.AuthRepositoryImpl
import com.mtislab.core.domain.repository.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
}