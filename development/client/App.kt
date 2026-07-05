package com.surfschool

import androidx.compose.runtime.Composable
import com.surfschool.core.di.coreModule
import com.surfschool.features.auth.di.authModule
import com.surfschool.features.booking.di.bookingModule
import com.surfschool.features.profile.di.profileModule
import com.surfschool.features.slots.di.slotsModule
import org.koin.compose.KoinApplication
import com.surfschool.core.navigation.AppNavigation
import com.surfschool.features.auth.ui.LoginScreen

@Composable
fun App() {
    KoinApplication(application = {
        modules(coreModule, authModule, slotsModule, bookingModule, profileModule)
    }) {
        AppNavigation(initialScreen = LoginScreen())
    }
}
