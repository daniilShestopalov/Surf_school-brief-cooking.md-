package com.surfschool.features.booking.data

import com.surfschool.features.booking.data.dto.CreateBookingRequest
import com.surfschool.features.booking.data.dto.CreateBookingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class BookingApi(private val client: HttpClient) {
    suspend fun createBooking(
        idempotencyKey: String,
        request: CreateBookingRequest
    ): CreateBookingResponse {
        return client.post("/bookings") {
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }.body()
    }
}
