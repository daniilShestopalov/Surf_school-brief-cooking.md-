package com.surfschool.features.auth.presentation

import cafe.adriel.voyager.core.model.screenModelScope
import com.surfschool.core.mvi.BaseScreenModel
import com.surfschool.core.mvi.Effect
import com.surfschool.core.mvi.Intent
import com.surfschool.core.mvi.State
import com.surfschool.core.network.NetworkException
import com.surfschool.core.network.RateLimitException
import com.surfschool.core.network.ServerException
import com.surfschool.features.auth.data.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LoginState(
    val phone: String = "",
    val consentChecked: Boolean = false,
    val showConsentError: Boolean = false,
    val isLoading: Boolean = false,
    val step: LoginStep = LoginStep.PhoneInput,
    val otpCode: String = "",
    val otpError: String? = null,
    val resendTimerSeconds: Int = 0
) : State

enum class LoginStep { PhoneInput, OtpInput }

sealed interface LoginIntent : Intent {
    data class PhoneChanged(val phone: String) : LoginIntent
    data class ConsentChanged(val checked: Boolean) : LoginIntent
    object SendCodeClicked : LoginIntent
    data class OtpChanged(val code: String) : LoginIntent
    object ResendCodeClicked : LoginIntent
}

sealed interface LoginEffect : Effect {
    data class NavigateToRegistration(val isNewUser: Boolean) : LoginEffect
    data class NavigateToSchedule(val isNewUser: Boolean) : LoginEffect
    data class ShowErrorSnackbar(val message: String) : LoginEffect
}

class LoginScreenModel(
    private val authRepository: AuthRepository
) : BaseScreenModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    private var timerJob: Job? = null

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged -> updateState { copy(phone = intent.phone) }
            is LoginIntent.ConsentChanged -> updateState { copy(consentChecked = intent.checked, showConsentError = false) }
            is LoginIntent.SendCodeClicked -> sendCode()
            is LoginIntent.OtpChanged -> handleOtpChanged(intent.code)
            is LoginIntent.ResendCodeClicked -> sendCode(isResend = true)
        }
    }

    private fun sendCode(isResend: Boolean = false) {
        val currentState = state.value
        if (!currentState.consentChecked) {
            updateState { copy(showConsentError = true) }
            return
        }
        if (currentState.phone.isBlank()) return

        updateState { copy(isLoading = true, showConsentError = false) }
        
        screenModelScope.launch {
            try {
                authRepository.sendCode(currentState.phone, currentState.consentChecked)
                startTimer(60)
                updateState { copy(isLoading = false, step = LoginStep.OtpInput, otpError = null) }
            } catch (e: Exception) {
                handleError(e)
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun handleOtpChanged(code: String) {
        updateState { copy(otpCode = code, otpError = null) }
        if (code.length == OTP_LENGTH) {
            verifyCode(code)
        }
    }

    private fun verifyCode(code: String) {
        val phone = state.value.phone
        updateState { copy(isLoading = true) }
        
        screenModelScope.launch {
            try {
                val isNewUser = authRepository.verifyCode(phone, code)
                updateState { copy(isLoading = false) }
                if (isNewUser) {
                    emitEffect(LoginEffect.NavigateToRegistration(true))
                } else {
                    emitEffect(LoginEffect.NavigateToSchedule(false))
                }
            } catch (e: Exception) {
                handleError(e, isVerify = true)
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun handleError(e: Exception, isVerify: Boolean = false) {
        when (e) {
            is RateLimitException -> {
                startTimer(e.retryAfterSeconds)
            }
            is NetworkException -> {
                emitEffect(LoginEffect.ShowErrorSnackbar("Отсутствует подключение к сети"))
            }
            is ServerException -> {
                emitEffect(LoginEffect.ShowErrorSnackbar("Сервис временно недоступен. Попробуйте позже"))
            }
            else -> {
                if (isVerify) {
                    updateState { copy(otpError = "Неверный код или код устарел") }
                } else {
                    emitEffect(LoginEffect.ShowErrorSnackbar("Произошла ошибка"))
                }
            }
        }
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        updateState { copy(resendTimerSeconds = seconds) }
        timerJob = screenModelScope.launch {
            for (i in seconds downTo 1) {
                delay(1000)
                updateState { copy(resendTimerSeconds = i - 1) }
            }
        }
    }

    companion object {
        private const val OTP_LENGTH = 4
    }
}
