package com.surfschool.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendCodeRequest(
    @SerialName("phone") val phone: String,
    @SerialName("personal_data_consent") val personalDataConsent: Boolean
)

@Serializable
data class VerifyCodeRequest(
    @SerialName("phone") val phone: String,
    @SerialName("code") val code: String
)

@Serializable
data class AuthResponse(
    @SerialName("token") val token: String,
    @SerialName("is_new_user") val isNewUser: Boolean
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("firstname") val firstname: String
)
