package com.surfschool.features.profile.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.domain.models.Booking
import com.surfschool.domain.models.BookingStatus
import com.surfschool.domain.models.Client
import com.surfschool.features.profile.data.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Content(
        val client: Client,
        val activeBookings: List<Booking>,
        val pastBookings: List<Booking>
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileIntent {
    object LoadData : ProfileIntent()
    object RefreshData : ProfileIntent()
    object Logout : ProfileIntent()
}

sealed class ProfileEffect {
    data class ShowErrorSnackbar(val message: String) : ProfileEffect()
    object NavigateToLogin : ProfileEffect()
}

class ProfileScreenModel(
    private val repository: ProfileRepository
) : ScreenModel {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        handleIntent(ProfileIntent.LoadData)
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadData -> loadData(showLoading = true)
            is ProfileIntent.RefreshData -> loadData(showLoading = false)
            is ProfileIntent.Logout -> logout()
        }
    }

    private fun loadData(showLoading: Boolean) {
        if (showLoading) {
            _state.value = ProfileState.Loading
        }
        screenModelScope.launch {
            try {
                val profileDeferred = async { repository.getProfile() }
                val bookingsDeferred = async { repository.getBookings() }
                
                val profile = profileDeferred.await()
                val bookings = bookingsDeferred.await()
                
                val active = bookings.filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.PENDING_PAYMENT }
                val past = bookings.filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED_BY_CLIENT || it.status == BookingStatus.CANCELLED_BY_STUDIO }
                
                // Active bookings sorted by datetime_start if we had slot details here, for now keeping as is.
                _state.value = ProfileState.Content(
                    client = profile,
                    activeBookings = active,
                    pastBookings = past
                )
            } catch (e: com.surfschool.core.network.UnauthorizedException) {
                _effect.emit(ProfileEffect.NavigateToLogin)
            } catch (e: Exception) {
                if (_state.value is ProfileState.Content) {
                    _effect.emit(ProfileEffect.ShowErrorSnackbar("Не удалось обновить данные: ${e.message}"))
                } else {
                    _state.value = ProfileState.Error(e.message ?: "Неизвестная ошибка")
                }
            }
        }
    }

    private fun logout() {
        // Here we should clear the secure storage token.
        // Assuming secure storage is injected or handled at a higher level, or we emit NavigateToLogin
        screenModelScope.launch {
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
