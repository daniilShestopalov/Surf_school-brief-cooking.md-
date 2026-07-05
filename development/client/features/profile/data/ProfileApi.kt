package com.surfschool.features.profile.data

import com.surfschool.core.network.Exceptions
import com.surfschool.features.profile.data.dto.BookingResponse
import com.surfschool.features.profile.data.dto.ProfileResponse
import com.surfschool.features.profile.data.dto.RateChefRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ProfileApi(private val httpClient: HttpClient) {

    suspend fun getProfile(): ProfileResponse {
        return httpClient.get("/profile").body()
    }

    suspend fun getBookings(): List<BookingResponse> {
        return httpClient.get("/bookings").body()
    }

    suspend fun getBooking(bookingId: String): BookingResponse {
        return httpClient.get("/bookings/$bookingId").body()
    }

    suspend fun rateChef(bookingId: String, rating: Int) {
        httpClient.post("/bookings/$bookingId/rating") {
            setBody(RateChefRequest(rating))
        }
    }

    suspend fun cancelBooking(bookingId: String) {
        httpClient.delete("/bookings/$bookingId")
    }
}
