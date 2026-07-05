package com.surfschool.core.network

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.datetime.todayIn
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import com.surfschool.features.profile.data.dto.UpdateAllergiesRequest

object MockApiHandler {
    private val seenKeys = mutableSetOf<String>()
    private var mockAllergies = emptyList<String>()
    private var isBookingCancelled = false

    fun createMockEngine(): MockEngine {
        return MockEngine { request ->
            val url = request.url.encodedPath
            val method = request.method
            
            // Artificial delay to simulate network
            delay(500)

            when {
                // Auth: send code
                url.endsWith("/auth/send-code") && method == HttpMethod.Post -> {
                    respond(
                        content = ByteReadChannel("""{}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Auth: verify code
                url.endsWith("/auth/verify-code") && method == HttpMethod.Post -> {
                    respond(
                        content = ByteReadChannel("""
                            {
                                "token": "mock-access-token",
                                "is_new_user": false
                            }
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Slots list
                url.endsWith("/slots") && method == HttpMethod.Get -> {
                    val startDateStr = request.url.parameters["start_date"]
                    val startDate = try {
                        if (startDateStr != null) kotlinx.datetime.LocalDate.parse(startDateStr)
                        else kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
                    } catch (e: Exception) {
                        kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())
                    }
                    
                    val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
                    val startOfDayInstant = kotlinx.datetime.LocalDateTime(startDate, kotlinx.datetime.LocalTime(0, 0)).toInstant(timeZone)
                    val baseTime = startOfDayInstant.toEpochMilliseconds()
                    
                    val time1 = baseTime + 10 * 60 * 60 * 1000L
                    val time2 = baseTime + 12 * 60 * 60 * 1000L
                    val time3 = baseTime + 14 * 60 * 60 * 1000L
                    val time4 = baseTime + 16 * 60 * 60 * 1000L
                    val time5 = baseTime + 18 * 60 * 60 * 1000L

                    respond(
                        content = ByteReadChannel("""
                            [
                                {
                                    "id": "e0b8a211-1234-4321-abcd-slot10000001",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000001",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000001",
                                    "datetimeStart": $time1,
                                    "duration": 120,
                                    "maxCapacity": 10,
                                    "availableSeats": 5,
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": 5,
                                    "equipmentTariff": 150000
                                },
                                {
                                    "id": "slot-full-id",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000002",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000002",
                                    "datetimeStart": $time2,
                                    "duration": 150,
                                    "maxCapacity": 10,
                                    "availableSeats": 0,
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": 5,
                                    "equipmentTariff": 100000
                                },
                                {
                                    "id": "slot-no-equipment-id",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000003",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000003",
                                    "datetimeStart": $time3,
                                    "duration": 90,
                                    "maxCapacity": 10,
                                    "availableSeats": 2,
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": 0,
                                    "equipmentTariff": 50000
                                },
                                {
                                    "id": "slot-gone-id",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000004",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000001",
                                    "datetimeStart": $time4,
                                    "duration": 120,
                                    "maxCapacity": 10,
                                    "availableSeats": 5,
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": 5,
                                    "equipmentTariff": 150000
                                },
                                {
                                    "id": "slot-429-id",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000005",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000002",
                                    "datetimeStart": $time5,
                                    "duration": 120,
                                    "maxCapacity": 10,
                                    "availableSeats": 5,
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": 5,
                                    "equipmentTariff": 150000
                                }
                            ]
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Slot details
                url.contains("/slots/") && method == HttpMethod.Get -> {
                    val id = url.substringAfterLast("/")
                    val programTitle = when (id) {
                        "slot-full-id" -> "Стейк Рибай"
                        "slot-no-equipment-id" -> "Борщ"
                        "slot-gone-id" -> "Удаленный класс"
                        "slot-429-id" -> "Популярный класс"
                        else -> "Итальянская паста"
                    }
                    val price = when (id) {
                        "slot-full-id" -> 400000
                        "slot-no-equipment-id" -> 300000
                        else -> 500000
                    }
                    respond(
                        content = ByteReadChannel("""
                            {
                                "slot": {
                                    "id": "$id",
                                    "programId": "p0b8a211-0000-0000-0000-prog10000001",
                                    "chefId": "c0b8a211-0000-0000-0000-chef10000001",
                                    "datetimeStart": ${kotlinx.datetime.LocalDateTime(kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()), kotlinx.datetime.LocalTime(0, 0)).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds() + 10 * 60 * 60 * 1000L},
                                    "duration": 120,
                                    "maxCapacity": 10,
                                    "availableSeats": ${if (id == "slot-full-id") 0 else 5},
                                    "status": "SCHEDULED",
                                    "address": "ул. Кулинарная, д. 1",
                                    "availableEquipmentStock": ${if (id == "slot-no-equipment-id") 0 else 5},
                                    "equipmentTariff": 150000
                                },
                                "program": {
                                    "id": "p0b8a211-0000-0000-0000-prog10000001",
                                    "title": "$programTitle",
                                    "description": "Секреты приготовления",
                                    "complexity": "MEDIUM",
                                    "basePrice": $price
                                },
                                "chef": {
                                    "id": "c0b8a211-0000-0000-0000-chef10000001",
                                    "name": "Gordon Ramsey",
                                    "bio": "Известный шеф-повар",
                                    "rating": 4.9
                                }
                            }
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Bookings
                url.endsWith("/bookings") && method == HttpMethod.Post -> {
                    val idempotencyKey = request.headers["Idempotency-Key"]
                    val bodyString = request.body.toByteArray().decodeToString()
                    
                    // Определяем "Пасхалку" на основе тела запроса
                    val slotId = when {
                        bodyString.contains("slot-full-id") -> "slot-full-id"
                        bodyString.contains("slot-no-equipment-id") -> "slot-no-equipment-id"
                        bodyString.contains("slot-gone-id") -> "slot-gone-id"
                        bodyString.contains("slot-429-id") -> "slot-429-id"
                        else -> "regular-id"
                    }

                    // Сохраняем ключ (для мока нам достаточно просто игнорировать повторную бизнес-логику)
                    if (idempotencyKey != null) {
                        seenKeys.add(idempotencyKey)
                    }

                    when (slotId) {
                        "slot-full-id" -> respond(
                            content = ByteReadChannel("""{"code": "SLOT_FULL", "message": "Нет свободных мест на данный слот"}"""),
                            status = HttpStatusCode.Conflict,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                        "slot-no-equipment-id" -> respond(
                            content = ByteReadChannel("""{"code": "EQUIPMENT_OUT", "message": "Оборудование для данного слота закончилось"}"""),
                            status = HttpStatusCode.Conflict,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                        "slot-gone-id" -> respond(
                            content = ByteReadChannel("""{"code": "SLOT_GONE", "message": "Данный слот больше недоступен"}"""),
                            status = HttpStatusCode.Gone,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                        "slot-429-id" -> respond(
                            content = ByteReadChannel("""{"code": "TOO_MANY_REQUESTS", "message": "Слишком много запросов. Повторите позже."}"""),
                            status = HttpStatusCode.TooManyRequests,
                            headers = headersOf(
                                HttpHeaders.ContentType to listOf("application/json"),
                                HttpHeaders.RetryAfter to listOf("30") // <-- Требуемый заголовок
                            )
                        )
                        else -> {
                            val expiresAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + 60 * 60 * 1000
                            respond(
                                content = ByteReadChannel("""
                                    {
                                        "id": "b0b8a211-0000-0000-0000-book10000001",
                                        "status": "ACTIVE",
                                        "paymentUrl": null,
                                        "expires_at": $expiresAt
                                    }
                                """.trimIndent()),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                    }
                }
                
                // Profile
                url.endsWith("/profile") && method == HttpMethod.Get -> {
                    respond(
                        content = ByteReadChannel("""
                            {
                                "id": "u0b8a211-0000-0000-0000-user10000001",
                                "phone": "+79991234567",
                                "name": "Иван Иванов",
                                "email": "ivanov@example.com",
                                "allergy_profile": [${mockAllergies.joinToString(",") { "\"$it\"" }}]
                            }
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                
                // Profile update
                url.endsWith("/profile") && method == HttpMethod.Patch -> {
                    respond(
                        content = ByteReadChannel("""{}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Profile allergies update
                url.endsWith("/profile/allergies") && method == HttpMethod.Patch -> {
                    val bodyBytes = request.body.toByteArray()
                    val bodyString = bodyBytes.decodeToString()
                    try {
                        val req = Json { ignoreUnknownKeys = true }.decodeFromString<UpdateAllergiesRequest>(bodyString)
                        mockAllergies = req.allergyProfile
                    } catch (e: Exception) {
                        // fallback or ignore
                    }
                    respond(
                        content = ByteReadChannel("""{}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Active Bookings
                url.endsWith("/bookings") && method == HttpMethod.Get -> {
                    if (isBookingCancelled) {
                        respond(
                            content = ByteReadChannel("""[]"""),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    } else {
                        respond(
                            content = ByteReadChannel("""
                                [
                                    {
                                        "id": "b0b8a211-0000-0000-0000-book10000001",
                                        "client_id": "u0b8a211-0000-0000-0000-user10000001",
                                        "slot_id": "e0b8a211-1234-4321-abcd-slot10000001",
                                        "status": "ACTIVE",
                                        "seats_count": 1,
                                        "created_at": 1716000000000,
                                        "fixed_base_price": 500000,
                                        "equipment_tariff": 150000,
                                        "needs_rental_equipment": true
                                    }
                                ]
                            """.trimIndent()),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                }
                
                // Delete Booking
                url.contains("/bookings/") && method == HttpMethod.Delete -> {
                    isBookingCancelled = true
                    respond(
                        content = ByteReadChannel("""{}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                // Single Booking Details
                url.contains("/bookings/") && !url.endsWith("/bookings") && !url.endsWith("/rating") && method == HttpMethod.Get -> {
                    val bookingId = url.substringAfterLast("/")
                    respond(
                        content = ByteReadChannel("""
                            {
                                "id": "$bookingId",
                                "client_id": "u0b8a211-0000-0000-0000-user10000001",
                                "slot_id": "e0b8a211-1234-4321-abcd-slot10000001",
                                "status": "ACTIVE",
                                "seats_count": 1,
                                "created_at": 1716000000000,
                                "fixed_base_price": 500000,
                                "equipment_tariff": 150000,
                                "needs_rental_equipment": true
                            }
                        """.trimIndent()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }



                else -> respondBadRequest()
            }
        }
    }
}
