package com.surfschool.features.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    @SerialName("id") val id: String,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("allergy_profile") val allergyProfile: List<String> = emptyList()
)

@Serializable
data class BookingResponse(
    @SerialName("id") val id: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("slot_id") val slotId: String,
    @SerialName("status") val status: String,
    @SerialName("seats_count") val seatsCount: Int = 1,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("fixed_base_price") val fixedBasePrice: Int,
    @SerialName("equipment_tariff") val equipmentTariff: Int = 0,
    @SerialName("needs_rental_equipment") val needsRentalEquipment: Boolean = false,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("chef_rating") val chefRating: Int? = null
)

@Serializable
data class RateChefRequest(
    @SerialName("rating") val rating: Int
)
