package com.surfschool.features.slots.presentation.models

import com.surfschool.domain.models.Timestamp

data class SlotItem(
    val id: String,
    val title: String, // mapped from programId or backend response
    val datetimeStart: Timestamp,
    val duration: Int,
    val availableSeats: Int,
    val isCancelled: Boolean,
    val isFull: Boolean
)
