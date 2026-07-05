package com.surfschool.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.surfschool.features.profile.presentation.UpcomingBookingIntent
import com.surfschool.features.profile.presentation.UpcomingBookingScreenModel

class CancellationBottomSheet(private val bookingId: String) : Screen {
    
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val screenModel = getScreenModel<UpcomingBookingScreenModel>()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Отмена бронирования", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Вы уверены, что хотите отменить бронирование?")
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    screenModel.handleIntent(UpcomingBookingIntent.CancelBooking(bookingId))
                    bottomSheetNavigator.hide()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Отменить бронь")
            }
        }
    }
}
