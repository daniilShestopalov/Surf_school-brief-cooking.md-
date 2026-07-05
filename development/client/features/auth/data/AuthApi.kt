package com.surfschool.features.auth.data

import com.surfschool.features.auth.data.dto.AuthResponse
import com.surfschool.features.auth.data.dto.SendCodeRequest
import com.surfschool.features.auth.data.dto.UpdateProfileRequest
import com.surfschool.features.auth.data.dto.VerifyCodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {
    suspend fun sendCode(request: SendCodeRequest) {
        client.post("/auth/send-code") {
            setBody(request)
        }
    }

    suspend fun verifyCode(request: VerifyCodeRequest): AuthResponse {
        return client.post("/auth/verify-code") {
            setBody(request)
        }.body()
    }

    suspend fun updateProfile(request: UpdateProfileRequest) {
        client.patch("/profile") {
            setBody(request)
        }
    }
}
