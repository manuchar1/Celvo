package com.mtislab.celvo.di

import com.mtislab.celvo.AuthRepositoryImpl
import com.mtislab.domain.AuthRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
}