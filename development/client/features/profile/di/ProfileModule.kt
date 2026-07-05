package com.surfschool.features.profile.di

import com.surfschool.features.profile.data.ProfileApi
import com.surfschool.features.profile.data.ProfileRepository
import com.surfschool.features.profile.presentation.ProfileScreenModel
import com.surfschool.features.profile.presentation.UpcomingBookingScreenModel
import org.koin.dsl.module

val profileModule = module {
    single { ProfileApi(get()) }
    single { ProfileRepository(get()) }
    
    factory { ProfileScreenModel(get()) }
    factory { UpcomingBookingScreenModel(get()) }
}
