package com.surfschool.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.surfschool.features.booking.presentation.BookingIntent
import com.surfschool.features.booking.presentation.BookingScreenModel

class AllergiesSelectionBottomSheet : Screen {
    
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val screenModel = getScreenModel<BookingScreenModel>()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Выбор аллергий", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Список аллергенов (TODO)")
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    screenModel.onIntent(BookingIntent.SaveAllergies(emptyList())) // TODO: real allergies list
                    bottomSheetNavigator.hide()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
