package com.surfschool.features.slots.domain

import com.surfschool.domain.models.Slot
import com.surfschool.features.slots.domain.models.SlotDetails

interface SlotsRepository {
    suspend fun listSlots(startDate: String? = null, endDate: String? = null): List<Slot>
    suspend fun getSlot(id: String): SlotDetails
}
