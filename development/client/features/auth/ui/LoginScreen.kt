package com.surfschool.features.auth.ui

import androidx.compose.foundation.layout.*
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
import com.surfschool.features.auth.presentation.LoginEffect
import com.surfschool.features.auth.presentation.LoginIntent
import com.surfschool.features.auth.presentation.LoginScreenModel
import com.surfschool.features.auth.presentation.LoginStep
import com.surfschool.core.navigation.RootScreen

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(screenModel) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is LoginEffect.NavigateToRegistration -> navigator.push(RegistrationScreen())
                    is LoginEffect.NavigateToSchedule -> navigator.replaceAll(RootScreen())
                    is LoginEffect.ShowErrorSnackbar -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Шеф-стол", style = MaterialTheme.typography.headlineLarge)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (state.step == LoginStep.PhoneInput) {
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { screenModel.onIntent(LoginIntent.PhoneChanged(it)) },
                    label = { Text("Номер телефона") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.consentChecked,
                        onCheckedChange = { screenModel.onIntent(LoginIntent.ConsentChanged(it)) },
                        colors = if (state.showConsentError) CheckboxDefaults.colors(uncheckedColor = Color.Red) else CheckboxDefaults.colors(),
                        enabled = !state.isLoading
                    )
                    Text("Согласие на обработку ПД", color = if (state.showConsentError) Color.Red else Color.Unspecified)
                }
                
                Button(
                    onClick = { screenModel.onIntent(LoginIntent.SendCodeClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && state.phone.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Получить код")
                    }
                }
            } else {
                OutlinedTextField(
                    value = state.otpCode,
                    onValueChange = { screenModel.onIntent(LoginIntent.OtpChanged(it)) },
                    label = { Text("Код из СМС") },
                    isError = state.otpError != null,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
                if (state.otpError != null) {
                    Text(text = state.otpError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { screenModel.onIntent(LoginIntent.ResendCodeClicked) },
                    enabled = state.resendTimerSeconds == 0 && !state.isLoading
                ) {
                    Text(if (state.resendTimerSeconds > 0) "Запросить повторно через ${state.resendTimerSeconds}с" else "Запросить повторно")
                }
            }
            }
        }
    }
}
