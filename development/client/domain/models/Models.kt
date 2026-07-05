package com.surfschool.domain.models

typealias UUID = String
typealias Timestamp = Long // Represents Unix epoch in milliseconds

data class Client(
    val id: UUID,
    val phone: String,
    val email: String?,
    val name: String?,
    val allergyProfile: List<String>
)

data class ClassProgram(
    val id: UUID,
    val title: String,
    val description: String,
    val complexity: String,
    val basePrice: Int // Price in kopecks
)

data class Chef(
    val id: UUID,
    val name: String,
    val bio: String,
    val rating: Float
)

enum class SlotStatus { SCHEDULED, CANCELLED }

data class Slot(
    val id: UUID,
    val programId: UUID,
    val chefId: UUID,
    val datetimeStart: Timestamp,
    val duration: Int, // Duration in minutes
    val maxCapacity: Int,
    val availableSeats: Int,
    val status: SlotStatus,
    val address: String,
    val availableEquipmentStock: Int,
    val equipmentTariff: Int // Price in kopecks
)

enum class BookingStatus { PENDING_PAYMENT, ACTIVE, CANCELLED_BY_CLIENT, CANCELLED_BY_STUDIO, COMPLETED }

data class Booking(
    val id: UUID,
    val clientId: UUID,
    val slotId: UUID,
    val status: BookingStatus,
    val seatsCount: Int = 1,
    val needsRentalEquipment: Boolean,
    val expiresAt: Timestamp?,
    val chefRating: Int? // Rating from 1 to 5
)
