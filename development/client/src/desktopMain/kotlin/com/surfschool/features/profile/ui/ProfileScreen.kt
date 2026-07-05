package com.surfschool.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.surfschool.domain.models.Booking
import com.surfschool.features.profile.presentation.ProfileEffect
import com.surfschool.features.profile.presentation.ProfileIntent
import com.surfschool.features.profile.presentation.ProfileScreenModel
import com.surfschool.features.profile.presentation.ProfileState

import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import com.surfschool.features.auth.ui.LoginScreen

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ProfileScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LifecycleEffect(
            onStarted = {
                screenModel.handleIntent(ProfileIntent.RefreshData)
            }
        )

        LaunchedEffect(screenModel) {
            screenModel.effect.collect { effect ->
                when (effect) {
                    is ProfileEffect.NavigateToLogin -> {
                        navigator.replaceAll(LoginScreen())
                    }
                    is ProfileEffect.ShowErrorSnackbar -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Профиль") },
                    actions = {
                        IconButton(onClick = { screenModel.handleIntent(ProfileIntent.Logout) }) {
                            Text("Выйти")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val currentState = state) {
                    is ProfileState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ProfileState.Error -> {
                        Column(modifier = Modifier.align(Alignment.Center)) {
                            Text(currentState.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { screenModel.handleIntent(ProfileIntent.LoadData) }) {
                                Text("Повторить")
                            }
                        }
                    }
                    is ProfileState.Content -> {
                        ProfileContent(
                            state = currentState,
                            onBookingClick = { bookingId ->
                                navigator.push(UpcomingBookingDetailsScreen(bookingId))
                            },
                            onRefresh = { screenModel.handleIntent(ProfileIntent.RefreshData) },
                            onUpdateAllergies = { allergies ->
                                screenModel.handleIntent(ProfileIntent.UpdateAllergies(allergies))
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ProfileContent(
        state: ProfileState.Content,
        onBookingClick: (String) -> Unit,
        onRefresh: () -> Unit,
        onUpdateAllergies: (List<String>) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Мои данные", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Имя: ${state.client.name ?: "Не указано"}")
                Text("Телефон: ${state.client.phone}")
                Text("Email: ${state.client.email}")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.client.allergyProfile.isNotEmpty()) {
                        Text("Аллергии: ${state.client.allergyProfile.joinToString()}")
                    } else {
                        Text("Аллергии: Нет")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val bottomSheetNavigator = cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator.current
                    TextButton(onClick = {
                        bottomSheetNavigator.show(
                            ProfileAllergiesBottomSheet(
                                currentAllergies = state.client.allergyProfile,
                                onSave = onUpdateAllergies
                            )
                        )
                    }) {
                        Text("Изменить")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Активные записи", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                if (state.activeBookings.isEmpty()) {
                    Text("У вас пока нет записей")
                }
            }

            items(state.activeBookings) { booking ->
                BookingCard(booking, onClick = { onBookingClick(booking.id) })
            }

            if (state.pastBookings.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Прошедшие записи", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(state.pastBookings) { booking ->
                    BookingCard(booking, onClick = { onBookingClick(booking.id) })
                }
            }
        }
    }

    @Composable
    private fun BookingCard(booking: Booking, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Запись: ${booking.id.take(8)}")
                Text("Статус: ${booking.status.name}")
            }
        }
    }
}
