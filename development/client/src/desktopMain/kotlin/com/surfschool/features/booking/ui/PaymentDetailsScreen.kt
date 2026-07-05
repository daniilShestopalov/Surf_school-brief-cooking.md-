package com.surfschool.features.booking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class PaymentDetailsScreen(private val bookingId: String, private val expiresAt: Long) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        
        var timeRemaining by remember { mutableStateOf(calculateRemainingTime()) }

        LaunchedEffect(Unit) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining = calculateRemainingTime()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Оплата бронирования", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                
                Text(
                    text = if (timeRemaining > 0) "Осталось времени: $timeString" else "Время вышло",
                    color = if (timeRemaining > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        // TODO: Implement clipboard copy
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = timeRemaining > 0
                ) {
                    Text("Скопировать реквизиты")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { navigator.popUntilRoot() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("На главную")
                }
            }
        }
    }

    private fun calculateRemainingTime(): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = (expiresAt - now) / 1000
        return if (diff > 0) diff else 0
    }
}
