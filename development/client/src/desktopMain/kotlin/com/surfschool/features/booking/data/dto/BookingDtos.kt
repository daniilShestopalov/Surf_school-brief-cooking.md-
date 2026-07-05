package com.surfschool.features.booking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingRequest(
    @SerialName("slot_id") val slotId: String,
    @SerialName("seats_count") val seatsCount: Int,
    @SerialName("needs_rental_equipment") val needsRentalEquipment: Boolean
)

@Serializable
data class CreateBookingResponse(
    @SerialName("id") val id: String,
    @SerialName("expires_at") val expiresAt: Long
)

@Serializable
data class ApiErrorResponse(
    @SerialName("code") val code: String
)
