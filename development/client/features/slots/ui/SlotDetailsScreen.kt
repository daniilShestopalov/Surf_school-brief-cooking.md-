package com.surfschool.features.slots.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.surfschool.features.slots.domain.models.SlotDetails
import com.surfschool.features.slots.presentation.SlotDetailsEffect
import com.surfschool.features.slots.presentation.SlotDetailsIntent
import com.surfschool.features.slots.presentation.SlotDetailsStore
import kotlinx.coroutines.launch

data class SlotDetailsScreen(val slotId: String) : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val store = getScreenModel<SlotDetailsStore>()
        val state by store.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(slotId) {
            store.onIntent(SlotDetailsIntent.Load(slotId))
            store.effect.collect { effect ->
                when (effect) {
                    is SlotDetailsEffect.ShowError -> {
                        scope.launch { snackbarHostState.showSnackbar(effect.message) }
                    }
                    is SlotDetailsEffect.NavigateBack -> {
                        navigator.pop()
                    }
                    is SlotDetailsEffect.OpenBookingFlow -> {
                        navigator.push(com.surfschool.features.booking.ui.BookingConfirmationBottomSheet(effect.slot, effect.basePrice))
                    }
                    is SlotDetailsEffect.OpenUpcomingBookingDetails -> {
                        navigator.push(com.surfschool.features.profile.ui.UpcomingBookingDetailsScreen(effect.bookingId))
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Детали класса") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            },
            bottomBar = {
                state.details?.let { details ->
                    BottomActionBar(
                        details = details,
                        isUserBooked = state.isUserBooked,
                        onBookClicked = { store.onIntent(SlotDetailsIntent.BookClicked) },
                        onNavigateToBooking = { store.onIntent(SlotDetailsIntent.NavigateToBookingClicked) }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.isError && state.details == null) {
                    ErrorStateView(
                        message = "Ошибка загрузки деталей",
                        onRetry = { store.onIntent(SlotDetailsIntent.Load(slotId)) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    state.details?.let { details ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = details.program.title,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Сложность: ${details.program.complexity}")
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(details.program.description, style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Шеф-повар", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(details.chef.name, style = MaterialTheme.typography.titleMedium)
                            Text("Рейтинг: ${details.chef.rating}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(details.chef.bio, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Место проведения", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(details.slot.address, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Осталось ${details.slot.availableSeats} мест(а)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar(
    details: SlotDetails,
    isUserBooked: Boolean,
    onBookClicked: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (isUserBooked) {
                Button(onClick = onNavigateToBooking, modifier = Modifier.fillMaxWidth()) {
                    Text("Перейти к брони")
                }
            } else if (details.slot.status.name == "CANCELLED") {
                Button(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Класс отменен")
                }
            } else if (details.slot.availableSeats == 0) {
                Button(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Мест нет")
                }
            } else {
                Button(onClick = onBookClicked, modifier = Modifier.fillMaxWidth()) {
                    // Displaying price assuming basePrice is in kopecks
                    val priceRub = details.program.basePrice / 100
                    Text("Забронировать ($priceRub ₽)")
                }
            }
        }
    }
}
