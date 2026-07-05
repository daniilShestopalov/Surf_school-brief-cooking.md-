package com.surfschool.features.booking.data

import com.surfschool.domain.models.Booking
import com.surfschool.domain.models.BookingStatus
import com.surfschool.features.booking.data.dto.CreateBookingRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class ConflictException(message: String) : Exception(message)
class GoneException(message: String) : Exception(message)

class BookingRepository(private val api: BookingApi) {
    suspend fun createBooking(
        slotId: String,
        seatsCount: Int,
        needsRentalEquipment: Boolean,
        idempotencyKey: String,
        clientId: String
    ): Booking {
        try {
            val response = api.createBooking(
                idempotencyKey,
                CreateBookingRequest(slotId, seatsCount, needsRentalEquipment)
            )
            return Booking(
                id = response.id,
                clientId = clientId,
                slotId = slotId,
                status = BookingStatus.PENDING_PAYMENT,
                seatsCount = seatsCount,
                needsRentalEquipment = needsRentalEquipment,
                expiresAt = response.expiresAt,
                chefRating = null
            )
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Conflict -> {
                    val bodyStr = io.ktor.client.statement.bodyAsText(e.response)
                    val errorCode = try {
                        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            .decodeFromString<com.surfschool.features.booking.data.dto.ApiErrorResponse>(bodyStr).code
                    } catch (ex: Exception) {
                        e.response.status.description
                    }
                    throw ConflictException(errorCode)
                }
                HttpStatusCode.Gone -> throw GoneException(e.response.status.description)
                else -> throw e
            }
        }
    }
}
