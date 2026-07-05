package com.surfschool.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.surfschool.features.profile.ui.ProfileScreen
import com.surfschool.features.slots.ui.SlotsCatalogScreen

class RootScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(SlotsTab) {
            Scaffold(
                bottomBar = {
                    BottomNavigation {
                        TabNavigationItem(SlotsTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    BottomNavigationItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { },
        label = { Text(tab.options.title) }
    )
}

object SlotsTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(index = 0u, title = "Расписание")

    @Composable
    override fun Content() {
        Navigator(SlotsCatalogScreen())
    }
}


object ProfileTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(index = 2u, title = "Профиль")

    @Composable
    override fun Content() {
        Navigator(ProfileScreen())
    }
}
