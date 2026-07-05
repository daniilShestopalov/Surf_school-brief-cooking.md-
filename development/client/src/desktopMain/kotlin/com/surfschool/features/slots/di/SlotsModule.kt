package com.surfschool.features.slots.di

import com.surfschool.features.slots.data.SlotsRepositoryImpl
import com.surfschool.features.slots.domain.SlotsRepository
import com.surfschool.features.slots.presentation.SlotDetailsStore
import com.surfschool.features.slots.presentation.SlotsCatalogStore
import org.koin.dsl.module

val slotsModule = module {
    single<SlotsRepository> { SlotsRepositoryImpl(get()) }
    
    factory { SlotsCatalogStore(get()) }
    factory { SlotDetailsStore(get()) }
}
