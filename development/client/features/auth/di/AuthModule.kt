package com.surfschool.features.auth.di

import com.surfschool.features.auth.data.AuthApi
import com.surfschool.features.auth.data.AuthRepository
import com.surfschool.features.auth.presentation.LoginScreenModel
import com.surfschool.features.auth.presentation.RegistrationScreenModel
import org.koin.dsl.module

val authModule = module {
    single { AuthApi(get()) }
    single { AuthRepository(get(), get()) }
    
    factory { LoginScreenModel(get()) }
    factory { RegistrationScreenModel(get()) }
}
