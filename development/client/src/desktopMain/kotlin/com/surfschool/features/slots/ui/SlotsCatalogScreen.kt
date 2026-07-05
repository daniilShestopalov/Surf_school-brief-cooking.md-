package com.surfschool.features.slots.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.surfschool.features.slots.presentation.SlotsCatalogEffect
import com.surfschool.features.slots.presentation.SlotsCatalogIntent
import com.surfschool.features.slots.presentation.SlotsCatalogStore
import com.surfschool.features.slots.presentation.models.SlotItem
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant

class SlotsCatalogScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val store = getScreenModel<SlotsCatalogStore>()
        val state by store.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            store.effect.collect { effect ->
                when (effect) {
                    is SlotsCatalogEffect.ShowError -> {
                        scope.launch { snackbarHostState.showSnackbar(effect.message) }
                    }
                    is SlotsCatalogEffect.NavigateToDetails -> {
                        navigator.push(SlotDetailsScreen(effect.id))
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Filter bar
                Column {
                    Text(
                        text = "Расписание",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    DateFilterRow(
                        availableDates = state.availableDates,
                        selectedDate = state.selectedDate,
                        onDateSelected = { store.onIntent(SlotsCatalogIntent.SelectDate(it)) }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.isError && state.slots.isEmpty()) {
                    ErrorStateView(
                        message = "Не удалось загрузить расписание",
                        onRetry = { store.onIntent(SlotsCatalogIntent.Refresh) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.isEmpty) {
                    EmptyStateView(
                        message = "Нет доступных классов на выбранные даты",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.slots) { slot ->
                            SlotCard(
                                slot = slot,
                                onClick = { store.onIntent(SlotsCatalogIntent.SlotClicked(slot.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateFilterRow(
    availableDates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val monthNames = remember { listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек") }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableDates) { date ->
            val isSelected = date == selectedDate
            val labelText = "${date.dayOfMonth} ${monthNames[date.monthNumber - 1]}"
            FilterChip(
                selected = isSelected,
                onClick = { onDateSelected(date) },
                label = { Text(labelText) }
            )
        }
    }
}

@Composable
fun SlotCard(slot: SlotItem, onClick: () -> Unit) {
    val isDisabled = slot.isCancelled || slot.isFull
    val alpha = if (isDisabled) 0.5f else 1f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = slot.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val formattedTime = remember(slot.datetimeStart) {
                val localDateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(slot.datetimeStart)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val hour = localDateTime.hour.toString().padStart(2, '0')
                val minute = localDateTime.minute.toString().padStart(2, '0')
                "$hour:$minute"
            }
            Text(text = "Начало: $formattedTime | Длительность: ${slot.duration} мин")
            Spacer(modifier = Modifier.height(8.dp))
            if (slot.isCancelled) {
                Text("Отменен", color = MaterialTheme.colorScheme.error)
            } else if (slot.isFull) {
                Text("Мест нет", color = Color.Gray)
            } else {
                Text("Осталось мест: ${slot.availableSeats}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Обновить")
        }
    }
}

@Composable
fun EmptyStateView(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
