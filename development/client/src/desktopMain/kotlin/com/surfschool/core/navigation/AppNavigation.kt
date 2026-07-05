package com.surfschool.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.surfschool.core.network.GlobalNavigationEvent
import com.surfschool.core.network.globalNavigationEvents
import com.surfschool.features.auth.ui.LoginScreen

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun AppNavigation(initialScreen: Screen) {
    // BottomSheetNavigator provided by Voyager allows nested BottomSheet navigation natively in compose
    BottomSheetNavigator {
        Navigator(screen = initialScreen) { navigator ->
            LaunchedEffect(Unit) {
                globalNavigationEvents.collect { event ->
                    when (event) {
                        GlobalNavigationEvent.NavigateToAuth -> {
                            navigator.replaceAll(LoginScreen())
                        }
                    }
                }
            }
            SlideTransition(navigator)
        }
    }
}
