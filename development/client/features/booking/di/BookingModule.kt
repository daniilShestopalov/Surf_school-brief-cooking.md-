package com.surfschool.features.booking.di

import com.surfschool.features.booking.data.BookingApi
import com.surfschool.features.booking.data.BookingRepository
import com.surfschool.features.booking.presentation.BookingScreenModel
import org.koin.dsl.module

val bookingModule = module {
    single { BookingApi(get()) }
    single { BookingRepository(get()) }
    factory { BookingScreenModel(get(), get()) }
}
