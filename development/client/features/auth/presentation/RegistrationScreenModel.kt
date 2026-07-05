package com.surfschool.features.auth.presentation

import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.core.mvi.BaseScreenModel
import com.surfschool.core.mvi.Effect
import com.surfschool.core.mvi.Intent
import com.surfschool.core.mvi.State
import com.surfschool.core.network.NetworkException
import com.surfschool.core.network.ServerException
import com.surfschool.features.auth.data.AuthRepository
import kotlinx.coroutines.launch

data class RegistrationState(
    val name: String = "",
    val isLoading: Boolean = false,
    val isNameValid: Boolean = false
) : State

sealed interface RegistrationIntent : Intent {
    data class NameChanged(val name: String) : RegistrationIntent
    object ContinueClicked : RegistrationIntent
}

sealed interface RegistrationEffect : Effect {
    object NavigateToSchedule : RegistrationEffect
    data class ShowErrorSnackbar(val message: String) : RegistrationEffect
}

class RegistrationScreenModel(
    private val authRepository: AuthRepository
) : BaseScreenModel<RegistrationState, RegistrationIntent, RegistrationEffect>(RegistrationState()) {

    override fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.NameChanged -> {
                val isValid = intent.name.length >= 2
                updateState { copy(name = intent.name, isNameValid = isValid) }
            }
            is RegistrationIntent.ContinueClicked -> saveProfile()
        }
    }

    private fun saveProfile() {
        val currentState = state.value
        if (!currentState.isNameValid) return
        
        updateState { copy(isLoading = true) }
        
        screenModelScope.launch {
            try {
                authRepository.updateProfile(currentState.name)
                updateState { copy(isLoading = false) }
                emitEffect(RegistrationEffect.NavigateToSchedule)
            } catch (e: Exception) {
                updateState { copy(isLoading = false) }
                when (e) {
                    is NetworkException -> emitEffect(RegistrationEffect.ShowErrorSnackbar("Отсутствует подключение к сети"))
                    is ServerException -> emitEffect(RegistrationEffect.ShowErrorSnackbar("Сервис временно недоступен. Попробуйте позже"))
                    else -> emitEffect(RegistrationEffect.ShowErrorSnackbar("Произошла ошибка"))
                }
            }
        }
    }
}
