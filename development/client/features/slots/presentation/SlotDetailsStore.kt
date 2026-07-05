package com.surfschool.features.slots.presentation

import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.core.mvi.BaseScreenModel
import com.surfschool.core.mvi.Effect
import com.surfschool.core.mvi.Intent
import com.surfschool.core.mvi.State
import com.surfschool.core.network.NetworkException
import com.surfschool.features.slots.domain.SlotsRepository
import com.surfschool.features.slots.domain.models.SlotDetails
import kotlinx.coroutines.launch

data class SlotDetailsState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val details: SlotDetails? = null,
    val isUserBooked: Boolean = false, // This would come from a real auth/booking state in a full app
    val userBookingId: String? = null
) : State

sealed interface SlotDetailsIntent : Intent {
    data class Load(val id: String) : SlotDetailsIntent
    object BookClicked : SlotDetailsIntent
    object NavigateToBookingClicked : SlotDetailsIntent
}

sealed interface SlotDetailsEffect : Effect {
    data class ShowError(val message: String) : SlotDetailsEffect
    object NavigateBack : SlotDetailsEffect
    data class OpenBookingFlow(
        val slot: com.surfschool.domain.models.Slot,
        val basePrice: Int
    ) : SlotDetailsEffect
    data class OpenUpcomingBookingDetails(val bookingId: String) : SlotDetailsEffect
}

class SlotDetailsStore(
    private val repository: SlotsRepository
) : BaseScreenModel<SlotDetailsState, SlotDetailsIntent, SlotDetailsEffect>(SlotDetailsState()) {

    override fun onIntent(intent: SlotDetailsIntent) {
        when (intent) {
            is SlotDetailsIntent.Load -> loadDetails(intent.id)
            is SlotDetailsIntent.BookClicked -> {
                state.value.details?.let {
                    emitEffect(SlotDetailsEffect.OpenBookingFlow(
                        slot = it.slot,
                        basePrice = it.program.basePrice
                    ))
                }
            }
            is SlotDetailsIntent.NavigateToBookingClicked -> {
                state.value.userBookingId?.let {
                    emitEffect(SlotDetailsEffect.OpenUpcomingBookingDetails(it))
                } ?: run {
                    // Fallback for mocked state
                    emitEffect(SlotDetailsEffect.OpenUpcomingBookingDetails("mock_booking_id"))
                }
            }
        }
    }

    private fun loadDetails(id: String) {
        screenModelScope.launch {
            updateState { copy(isLoading = true, isError = false) }
            try {
                val details = repository.getSlot(id)
                updateState { copy(isLoading = false, details = details) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, isError = true) }
                if (e is io.ktor.client.plugins.ClientRequestException && (e.response.status.value == 404 || e.response.status.value == 410)) {
                    emitEffect(SlotDetailsEffect.ShowError("Слот не найден"))
                    emitEffect(SlotDetailsEffect.NavigateBack)
                } else {
                    val msg = if (e is NetworkException) "Отсутствует подключение к сети" else "Ошибка загрузки"
                    emitEffect(SlotDetailsEffect.ShowError(msg))
                }
            }
        }
    }
}
