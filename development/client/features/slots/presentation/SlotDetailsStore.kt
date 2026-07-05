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
    val isUserBooked: Boolean = false // This would come from a real auth/booking state in a full app
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
        val slotId: String, 
        val basePrice: Int, 
        val equipmentTariff: Int, 
        val availableEquipmentStock: Int
    ) : SlotDetailsEffect
    object OpenUpcomingBookingDetails : SlotDetailsEffect
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
                        slotId = it.slot.id,
                        basePrice = it.program.basePrice,
                        equipmentTariff = it.slot.equipmentTariff,
                        availableEquipmentStock = it.slot.availableEquipmentStock
                    ))
                }
            }
            is SlotDetailsIntent.NavigateToBookingClicked -> {
                emitEffect(SlotDetailsEffect.OpenUpcomingBookingDetails)
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
                if (e is io.ktor.client.plugins.ClientRequestException && e.response.status.value == 404) {
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
