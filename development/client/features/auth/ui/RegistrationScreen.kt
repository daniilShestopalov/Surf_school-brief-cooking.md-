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
import com.surfschool.features.auth.presentation.RegistrationEffect
import com.surfschool.features.auth.presentation.RegistrationIntent
import com.surfschool.features.auth.presentation.RegistrationScreenModel
import com.surfschool.core.navigation.RootScreen

class RegistrationScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<RegistrationScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(screenModel) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is RegistrationEffect.NavigateToSchedule -> navigator.replaceAll(RootScreen())
                    is RegistrationEffect.ShowErrorSnackbar -> snackbarHostState.showSnackbar(effect.message)
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
            Text(text = "Чтобы шеф знал...", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = state.name,
                onValueChange = { screenModel.onIntent(RegistrationIntent.NameChanged(it)) },
                label = { Text("Ваше имя") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { screenModel.onIntent(RegistrationIntent.ContinueClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isNameValid && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Продолжить")
                }
            }
            }
        }
    }
}
