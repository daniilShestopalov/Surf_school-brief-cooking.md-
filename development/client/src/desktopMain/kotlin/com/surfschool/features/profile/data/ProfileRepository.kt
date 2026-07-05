package com.surfschool.features.profile.data

import com.surfschool.domain.models.Booking
import com.surfschool.domain.models.BookingStatus
import com.surfschool.domain.models.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val api: ProfileApi
) {
    suspend fun getProfile(): Client = withContext(Dispatchers.IO) {
        val response = api.getProfile()
        Client(
            id = response.id,
            phone = response.phone,
            email = response.email,
            name = response.name,
            allergyProfile = response.allergyProfile
        )
    }

    suspend fun getBookings(): List<Booking> = withContext(Dispatchers.IO) {
        api.getBookings().map { it.toDomain() }
    }

    suspend fun getBooking(bookingId: String): Booking = withContext(Dispatchers.IO) {
        api.getBooking(bookingId).toDomain()
    }

    suspend fun rateChef(bookingId: String, rating: Int) = withContext(Dispatchers.IO) {
        api.rateChef(bookingId, rating)
    }

    suspend fun cancelBooking(bookingId: String) = withContext(Dispatchers.IO) {
        api.cancelBooking(bookingId)
        com.surfschool.domain.events.BookingEvents.bookingCancelled.emit(bookingId)
    }

    private fun com.surfschool.features.profile.data.dto.BookingResponse.toDomain(): Booking {
        val parsedStatus = try {
            BookingStatus.valueOf(this.status)
        } catch (e: Exception) {
            BookingStatus.PENDING_PAYMENT // fallback
        }
        return Booking(
            id = this.id,
            clientId = this.clientId,
            slotId = this.slotId,
            status = parsedStatus,
            seatsCount = this.seatsCount,
            createdAt = this.createdAt,
            fixedBasePrice = this.fixedBasePrice,
            equipmentTariff = this.equipmentTariff,
            needsRentalEquipment = this.needsRentalEquipment,
            expiresAt = this.expiresAt,
            chefRating = this.chefRating
        )
    }
}
