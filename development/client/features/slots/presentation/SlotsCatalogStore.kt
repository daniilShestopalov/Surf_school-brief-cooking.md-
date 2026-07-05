package com.surfschool.features.slots.presentation

import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.core.mvi.BaseScreenModel
import com.surfschool.core.mvi.Effect
import com.surfschool.core.mvi.Intent
import com.surfschool.core.mvi.State
import com.surfschool.core.network.NetworkException
import com.surfschool.features.slots.domain.SlotsRepository
import com.surfschool.features.slots.presentation.models.SlotItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SlotsCatalogState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val slots: List<SlotItem> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val availableDates: List<LocalDate> = generateDates(LocalDate.now(), 7)
) : State {
    val isEmpty: Boolean get() = !isLoading && !isError && slots.isEmpty()
}

sealed interface SlotsCatalogIntent : Intent {
    object Init : SlotsCatalogIntent
    object Refresh : SlotsCatalogIntent
    data class SelectDate(val date: LocalDate) : SlotsCatalogIntent
    data class SlotClicked(val id: String) : SlotsCatalogIntent
}

sealed interface SlotsCatalogEffect : Effect {
    data class ShowError(val message: String) : SlotsCatalogEffect
    data class NavigateToDetails(val id: String) : SlotsCatalogEffect
}

class SlotsCatalogStore(
    private val repository: SlotsRepository
) : BaseScreenModel<SlotsCatalogState, SlotsCatalogIntent, SlotsCatalogEffect>(SlotsCatalogState()) {

    override fun onIntent(intent: SlotsCatalogIntent) {
        when (intent) {
            is SlotsCatalogIntent.Init -> loadSlots()
            is SlotsCatalogIntent.Refresh -> loadSlots(isRefresh = true)
            is SlotsCatalogIntent.SelectDate -> {
                updateState { copy(selectedDate = intent.date) }
                loadSlots()
            }
            is SlotsCatalogIntent.SlotClicked -> {
                emitEffect(SlotsCatalogEffect.NavigateToDetails(intent.id))
            }
        }
    }

    private fun loadSlots(isRefresh: Boolean = false) {
        screenModelScope.launch {
            if (isRefresh) {
                updateState { copy(isRefreshing = true, isError = false) }
            } else {
                updateState { copy(isLoading = true, isError = false) }
            }
            
            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val startDate = state.value.selectedDate.format(formatter)
                val endDate = state.value.selectedDate.plusDays(7).format(formatter)
                
                val domainSlots = repository.listSlots(startDate = startDate, endDate = endDate)
                
                // Mapping domain Slot to presentation SlotItem
                val items = domainSlots.map { slot ->
                    SlotItem(
                        id = slot.id,
                        title = "Мастер-класс (ID: ${slot.programId.take(4)})", // Placeholder for program title
                        datetimeStart = slot.datetimeStart,
                        duration = slot.duration,
                        availableSeats = slot.availableSeats,
                        isCancelled = slot.status.name == "CANCELLED",
                        isFull = slot.availableSeats == 0
                    )
                }
                
                updateState { 
                    copy(
                        isLoading = false, 
                        isRefreshing = false, 
                        slots = items
                    ) 
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, isRefreshing = false, isError = true) }
                val msg = if (e is NetworkException) "Отсутствует подключение к сети" else "Не удалось загрузить расписание"
                emitEffect(SlotsCatalogEffect.ShowError(msg))
            }
        }
    }
}

fun generateDates(start: LocalDate, days: Int): List<LocalDate> {
    return (0 until days).map { start.plusDays(it.toLong()) }
}
