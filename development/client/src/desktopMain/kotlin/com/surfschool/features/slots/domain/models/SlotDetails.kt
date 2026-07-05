package com.surfschool.features.slots.domain.models

import com.surfschool.domain.models.Chef
import com.surfschool.domain.models.ClassProgram
import com.surfschool.domain.models.Slot
import kotlinx.serialization.Serializable

@Serializable
data class SlotDetails(
    val slot: Slot,
    val program: ClassProgram,
    val chef: Chef
)
