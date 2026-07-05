package com.surfschool.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.surfschool.domain.models.Slot
import com.surfschool.features.booking.presentation.BookingEffect
import com.surfschool.features.booking.presentation.BookingIntent
import com.surfschool.features.booking.presentation.BookingScreenModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

class BookingConfirmationBottomSheet(private val slot: Slot, private val basePrice: Int) : Screen {
    
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<BookingScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        
        var showEquipmentDialog by remember { mutableStateOf(false) }
        var showSlotFullDialog by remember { mutableStateOf(false) }
        var showSlotGoneDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            screenModel.onIntent(BookingIntent.Init(slot, basePrice))
        }

        LaunchedEffect(screenModel.effect) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is BookingEffect.NavigateToPayment -> {
                        navigator.push(PaymentDetailsScreen(effect.bookingId, effect.expiresAt))
                    }
                    is BookingEffect.ShowErrorSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is BookingEffect.ShowSlotFullDialog -> {
                        showSlotFullDialog = true
                    }
                    is BookingEffect.AskToProceedWithoutEquipment -> {
                        showEquipmentDialog = true
                    }
                    is BookingEffect.ShowSlotGoneDialog -> {
                        showSlotGoneDialog = true
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Оформление бронирования") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Стоимость слота: ${state.slotBasePrice / 100} руб.")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Аренда инвентаря (${state.equipmentTariff / 100} руб.)")
                    Checkbox(
                        checked = state.isEquipmentChecked,
                        onCheckedChange = { 
                            screenModel.onIntent(BookingIntent.ToggleEquipment(it))
                        },
                        enabled = state.isEquipmentAvailable
                    )
                }
                
                if (!state.isEquipmentAvailable) {
                    Text("Инвентарь закончился", color = MaterialTheme.colorScheme.error)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Итого: ${state.totalPrice / 100} руб.", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { screenModel.onIntent(BookingIntent.ConfirmBooking(slot.id)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Подтвердить запись")
                    }
                }
            }
        }

        if (showEquipmentDialog) {
            AlertDialog(
                onDismissRequest = { showEquipmentDialog = false },
                title = { Text("Инвентарь закончился") },
                text = { Text("Желаете продолжить бронирование без инвентаря?") },
                confirmButton = {
                    TextButton(onClick = {
                        showEquipmentDialog = false
                        screenModel.onIntent(BookingIntent.ConfirmWithoutEquipment(slot.id))
                    }) {
                        Text("Да")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEquipmentDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showSlotFullDialog) {
            AlertDialog(
                onDismissRequest = { showSlotFullDialog = false },
                title = { Text("Свободных мест нет") },
                text = { Text("К сожалению, кто-то успел занять последние места.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSlotFullDialog = false
                        navigator.pop()
                    }) {
                        Text("Закрыть")
                    }
                }
            )
        }

        if (showSlotGoneDialog) {
            AlertDialog(
                onDismissRequest = { showSlotGoneDialog = false },
                title = { Text("Слот недоступен") },
                text = { Text("Слот устарел или больше недоступен.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSlotGoneDialog = false
                        navigator.pop()
                    }) {
                        Text("Понятно")
                    }
                }
            )
        }
    }
}
