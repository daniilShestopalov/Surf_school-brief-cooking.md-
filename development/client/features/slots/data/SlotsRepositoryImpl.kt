package com.surfschool.features.slots.data

import com.surfschool.core.network.NetworkException
import com.surfschool.core.network.ServerException
import com.surfschool.domain.models.Slot
import com.surfschool.features.slots.domain.SlotsRepository
import com.surfschool.features.slots.domain.models.SlotDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay

class SlotsRepositoryImpl(
    private val httpClient: HttpClient
) : SlotsRepository {

    override suspend fun listSlots(startDate: String?, endDate: String?): List<Slot> {
        return try {
            httpClient.get("/slots") {
                parameter("start_date", startDate)
                parameter("end_date", endDate)
            }.body()
        } catch (e: Exception) {
            // Re-throw handled core exceptions, or wrap in NetworkException
            if (e is ServerException || e is NetworkException) throw e
            throw NetworkException(cause = e)
        }
    }

    override suspend fun getSlot(id: String): SlotDetails {
        return try {
            httpClient.get("/slots/$id").body()
        } catch (e: Exception) {
            if (e is ServerException || e is NetworkException) throw e
            throw NetworkException(cause = e)
        }
    }
}
