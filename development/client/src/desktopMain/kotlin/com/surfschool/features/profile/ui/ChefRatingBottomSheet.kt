package com.surfschool.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.surfschool.features.profile.presentation.UpcomingBookingEffect
import com.surfschool.features.profile.presentation.UpcomingBookingIntent
import com.surfschool.features.profile.presentation.UpcomingBookingScreenModel

class ChefRatingBottomSheet(private val bookingId: String) : Screen {

    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val screenModel = getScreenModel<UpcomingBookingScreenModel>()
        
        var selectedRating by remember { mutableStateOf(0) }

        LaunchedEffect(screenModel) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is UpcomingBookingEffect.ShowToast -> {
                        // Toast shown by screen model context, we just close the sheet
                        bottomSheetNavigator.hide()
                    }
                    is UpcomingBookingEffect.ShowErrorSnackbar -> {
                        // Handle error (in real app, we'd have a local snackbar or propagate up)
                    }
                    else -> {}
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Оцените работу шефа", style = MaterialTheme.typography.titleLarge)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val isSelected = i <= selectedRating
                    Button(
                        onClick = { selectedRating = i },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(i.toString())
                    }
                }
            }

            Button(
                onClick = { 
                    screenModel.handleIntent(UpcomingBookingIntent.RateChef(bookingId, selectedRating))
                },
                enabled = selectedRating > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Оценить")
            }
        }
    }
}
