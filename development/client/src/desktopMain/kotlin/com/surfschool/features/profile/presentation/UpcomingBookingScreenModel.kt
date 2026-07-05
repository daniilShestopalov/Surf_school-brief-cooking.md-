package com.surfschool.features.profile.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.domain.models.Booking
import com.surfschool.features.profile.data.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpcomingBookingState {
    object Loading : UpcomingBookingState()
    data class Content(
        val booking: Booking
        // In a real app we might also fetch Slot and attach it here
    ) : UpcomingBookingState()
    data class Error(val message: String) : UpcomingBookingState()
}

sealed class UpcomingBookingIntent {
    data class LoadBooking(val bookingId: String) : UpcomingBookingIntent()
    data class CancelBooking(val bookingId: String) : UpcomingBookingIntent()
    data class RateChef(val bookingId: String, val rating: Int) : UpcomingBookingIntent()
}

sealed class UpcomingBookingEffect {
    data class ShowToast(val message: String) : UpcomingBookingEffect()
    data class ShowErrorSnackbar(val message: String) : UpcomingBookingEffect()
    object GoBack : UpcomingBookingEffect()
    object NavigateToLogin : UpcomingBookingEffect()
}

class UpcomingBookingScreenModel(
    private val repository: ProfileRepository
) : ScreenModel {

    private val _state = MutableStateFlow<UpcomingBookingState>(UpcomingBookingState.Loading)
    val state: StateFlow<UpcomingBookingState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UpcomingBookingEffect>()
    val effect: SharedFlow<UpcomingBookingEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: UpcomingBookingIntent) {
        when (intent) {
            is UpcomingBookingIntent.LoadBooking -> loadBooking(intent.bookingId)
            is UpcomingBookingIntent.CancelBooking -> cancelBooking(intent.bookingId)
            is UpcomingBookingIntent.RateChef -> rateChef(intent.bookingId, intent.rating)
        }
    }

    private fun loadBooking(bookingId: String) {
        _state.value = UpcomingBookingState.Loading
        screenModelScope.launch {
            try {
                val booking = repository.getBooking(bookingId)
                // If we had a Slot repository, we could also load slot here
                _state.value = UpcomingBookingState.Content(booking)
            } catch (e: com.surfschool.core.network.UnauthorizedException) {
                _effect.emit(UpcomingBookingEffect.NavigateToLogin)
            } catch (e: Exception) {
                _state.value = UpcomingBookingState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    private fun cancelBooking(bookingId: String) {
        screenModelScope.launch {
            try {
                repository.cancelBooking(bookingId)
                _effect.emit(UpcomingBookingEffect.ShowToast("Запись отменена"))
                // Refresh booking or just go back
                _effect.emit(UpcomingBookingEffect.GoBack)
            } catch (e: com.surfschool.core.network.UnauthorizedException) {
                _effect.emit(UpcomingBookingEffect.NavigateToLogin)
            } catch (e: Exception) {
                _effect.emit(UpcomingBookingEffect.ShowErrorSnackbar(e.message ?: "Не удалось отменить"))
            }
        }
    }

    private fun rateChef(bookingId: String, rating: Int) {
        screenModelScope.launch {
            try {
                repository.rateChef(bookingId, rating)
                _effect.emit(UpcomingBookingEffect.ShowToast("Спасибо за оценку!"))
            } catch (e: com.surfschool.core.network.UnauthorizedException) {
                _effect.emit(UpcomingBookingEffect.NavigateToLogin)
            } catch (e: Exception) {
                _effect.emit(UpcomingBookingEffect.ShowErrorSnackbar(e.message ?: "Не удалось отправить оценку"))
            }
        }
    }
}
