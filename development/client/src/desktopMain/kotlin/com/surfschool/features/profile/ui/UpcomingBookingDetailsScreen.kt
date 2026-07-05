package com.surfschool.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.surfschool.domain.models.BookingStatus
import com.surfschool.features.auth.ui.LoginScreen
import com.surfschool.features.profile.presentation.UpcomingBookingEffect
import com.surfschool.features.profile.presentation.UpcomingBookingIntent
import com.surfschool.features.profile.presentation.UpcomingBookingScreenModel
import com.surfschool.features.profile.presentation.UpcomingBookingState
import kotlinx.datetime.toLocalDateTime

class UpcomingBookingDetailsScreen(private val bookingId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val screenModel = getScreenModel<UpcomingBookingScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            screenModel.handleIntent(UpcomingBookingIntent.LoadBooking(bookingId))
        }

        LaunchedEffect(screenModel) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is UpcomingBookingEffect.ShowErrorSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is UpcomingBookingEffect.ShowToast -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is UpcomingBookingEffect.GoBack -> {
                        navigator.pop()
                    }
                    is UpcomingBookingEffect.NavigateToLogin -> {
                        navigator.replaceAll(LoginScreen())
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Детали записи") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("<-")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val currentState = state) {
                    is UpcomingBookingState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is UpcomingBookingState.Error -> {
                        Column(modifier = Modifier.align(Alignment.Center)) {
                            Text(currentState.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { screenModel.handleIntent(UpcomingBookingIntent.LoadBooking(bookingId)) }) {
                                Text("Повторить")
                            }
                        }
                    }
                    is UpcomingBookingState.Content -> {
                        val booking = currentState.booking
                        val slotDetails = currentState.slot
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (slotDetails != null) {
                                Text(slotDetails.program.title, style = MaterialTheme.typography.headlineMedium)
                                
                                val date = kotlinx.datetime.Instant.fromEpochMilliseconds(slotDetails.slot.datetimeStart)
                                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                val dateString = "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}.${date.year} ${date.hour.toString().padStart(2, '0')}:${date.minute.toString().padStart(2, '0')}"
                                Text("Дата и время: $dateString", style = MaterialTheme.typography.bodyLarge)
                                Text("Адрес: ${slotDetails.slot.address}", style = MaterialTheme.typography.bodyLarge)
                            }

                            Text("Запись: ${booking.id}", style = MaterialTheme.typography.bodySmall)
                            Text("Статус: ${booking.status.name}", style = MaterialTheme.typography.titleMedium)

                            if (booking.needsRentalEquipment) {
                                Text("Включена аренда инвентаря", color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))

                            if (booking.status == BookingStatus.PENDING_PAYMENT) {
                                Button(
                                    onClick = { navigator.push(com.surfschool.features.booking.ui.PaymentDetailsScreen(booking.id, booking.expiresAt ?: 0L)) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Оплатить")
                                }
                            }

                            if (booking.status == BookingStatus.ACTIVE || booking.status == BookingStatus.PENDING_PAYMENT) {
                                OutlinedButton(
                                    onClick = {
                                        bottomSheetNavigator.show(com.surfschool.features.booking.ui.CancellationBottomSheet(booking.id))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Отменить запись")
                                }
                            }

                            if (booking.status == BookingStatus.COMPLETED && booking.chefRating == null) {
                                Button(
                                    onClick = {
                                        bottomSheetNavigator.show(ChefRatingBottomSheet(booking.id))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Оценить шефа")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
