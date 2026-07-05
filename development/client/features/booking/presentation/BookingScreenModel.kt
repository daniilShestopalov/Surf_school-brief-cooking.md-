package com.surfschool.features.booking.presentation

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.core.utils.IdempotencyProvider
import com.surfschool.domain.models.Slot
import com.surfschool.features.booking.data.BookingRepository
import com.surfschool.features.booking.data.ConflictException
import com.surfschool.features.booking.data.GoneException
import com.surfschool.core.network.NetworkException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class BookingState(
    val isLoading: Boolean = false,
    val slotBasePrice: Int = 0,
    val equipmentTariff: Int = 0,
    val isEquipmentAvailable: Boolean = false,
    val isEquipmentChecked: Boolean = false,
    val totalPrice: Int = 0,
    val idempotencyKey: String? = null,
    val allergies: List<String> = emptyList()
)

sealed interface BookingIntent {
    data class Init(val slot: Slot, val basePrice: Int) : BookingIntent
    data class ToggleEquipment(val isChecked: Boolean) : BookingIntent
    data class ConfirmBooking(val slotId: String) : BookingIntent
    data class ConfirmWithoutEquipment(val slotId: String) : BookingIntent
    data class SaveAllergies(val allergies: List<String>) : BookingIntent
}

sealed interface BookingEffect {
    data class NavigateToPayment(val bookingId: String, val expiresAt: Long) : BookingEffect
    data class ShowErrorSnackbar(val message: String) : BookingEffect
    object ShowSlotFullDialog : BookingEffect
    object AskToProceedWithoutEquipment : BookingEffect
    object ShowSlotGoneDialog : BookingEffect
}

class BookingScreenModel(
    private val repository: BookingRepository,
    private val idempotencyProvider: IdempotencyProvider
) : StateScreenModel<BookingState>(BookingState()) {

    private val _effect = MutableSharedFlow<BookingEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: BookingIntent) {
        when (intent) {
            is BookingIntent.Init -> initSlot(intent.slot, intent.basePrice)
            is BookingIntent.ToggleEquipment -> toggleEquipment(intent.isChecked)
            is BookingIntent.ConfirmBooking -> confirmBooking(intent.slotId, false)
            is BookingIntent.ConfirmWithoutEquipment -> confirmBooking(intent.slotId, true)
            is BookingIntent.SaveAllergies -> mutableState.value = state.value.copy(allergies = intent.allergies)
        }
    }

    private fun initSlot(slot: Slot, basePrice: Int) {
        val key = state.value.idempotencyKey ?: idempotencyProvider.generateKey()
        mutableState.value = state.value.copy(
            slotBasePrice = basePrice,
            equipmentTariff = slot.equipmentTariff,
            isEquipmentAvailable = slot.availableEquipmentStock > 0,
            totalPrice = basePrice,
            idempotencyKey = key
        )
    }

    private fun toggleEquipment(isChecked: Boolean) {
        if (!state.value.isEquipmentAvailable) return
        val newTotal = state.value.slotBasePrice + if (isChecked) state.value.equipmentTariff else 0
        mutableState.value = state.value.copy(
            isEquipmentChecked = isChecked,
            totalPrice = newTotal
        )
    }

    private fun confirmBooking(slotId: String, forceWithoutEquipment: Boolean) {
        if (state.value.isLoading) return
        val key = state.value.idempotencyKey ?: return
        
        val needsEq = if (forceWithoutEquipment) false else state.value.isEquipmentChecked

        mutableState.value = state.value.copy(isLoading = true)
        screenModelScope.launch {
            try {
                val booking = repository.createBooking(
                    slotId = slotId,
                    seatsCount = 1, // Fixed value from spec
                    needsRentalEquipment = needsEq,
                    idempotencyKey = key,
                    clientId = "current_user" // Mock user id
                )
                _effect.emit(BookingEffect.NavigateToPayment(booking.id, booking.expiresAt ?: 0))
            } catch (e: ConflictException) {
                if (e.message == "EQUIPMENT_OUT") {
                    _effect.emit(BookingEffect.AskToProceedWithoutEquipment)
                } else {
                    _effect.emit(BookingEffect.ShowSlotFullDialog)
                }
            } catch (e: GoneException) {
                _effect.emit(BookingEffect.ShowSlotGoneDialog)
            } catch (e: NetworkException) {
                _effect.emit(BookingEffect.ShowErrorSnackbar("Отсутствует подключение к сети"))
            } catch (e: Exception) {
                _effect.emit(BookingEffect.ShowErrorSnackbar("Произошла ошибка при бронировании"))
            } finally {
                mutableState.value = state.value.copy(isLoading = false)
            }
        }
    }
}
