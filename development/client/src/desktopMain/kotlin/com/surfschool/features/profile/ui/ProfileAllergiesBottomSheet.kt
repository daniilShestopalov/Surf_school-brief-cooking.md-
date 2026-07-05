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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator

val ALLERGY_OPTIONS = listOf("Орехи", "Молоко", "Мед", "Морепродукты", "Глютен", "Цитрусовые")

data class ProfileAllergiesBottomSheet(
    val currentAllergies: List<String>,
    val onSave: (List<String>) -> Unit
) : Screen {
    
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        var selectedAllergies by remember { mutableStateOf(currentAllergies.toSet()) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Выбор аллергий", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(ALLERGY_OPTIONS) { allergy ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedAllergies.contains(allergy),
                            onCheckedChange = { checked ->
                                val newSet = selectedAllergies.toMutableSet()
                                if (checked) newSet.add(allergy) else newSet.remove(allergy)
                                selectedAllergies = newSet
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(allergy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    onSave(selectedAllergies.toList())
                    bottomSheetNavigator.hide()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
