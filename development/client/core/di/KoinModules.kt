package com.surfschool.core.di

import com.russhwolf.settings.Settings
import com.surfschool.core.network.createHttpClient
import com.surfschool.core.storage.SecureStorage
import com.surfschool.core.storage.SecureStorageImpl
import com.surfschool.core.utils.IdempotencyProvider
import com.surfschool.core.utils.IdempotencyProviderImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    // Multiplatform Settings
    single<Settings> { Settings() } // In reality might need platform-specific instantiation
    
    // Secure Storage
    single<SecureStorage> { SecureStorageImpl(get()) }
    
    // Network Client
    single { 
        createHttpClient(
            engine = com.surfschool.core.network.MockApiHandler.createMockEngine(),
            secureStorage = get()
        )
    }
    
    // Idempotency Provider (Used in Presentation layer)
    factory<IdempotencyProvider> { IdempotencyProviderImpl() }
}
