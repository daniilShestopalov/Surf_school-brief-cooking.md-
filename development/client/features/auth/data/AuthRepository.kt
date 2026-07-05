package com.surfschool.features.auth.data

import com.surfschool.core.storage.SecureStorage
import com.surfschool.features.auth.data.dto.SendCodeRequest
import com.surfschool.features.auth.data.dto.UpdateProfileRequest
import com.surfschool.features.auth.data.dto.VerifyCodeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val secureStorage: SecureStorage
) {
    suspend fun sendCode(phone: String, consent: Boolean) {
        withContext(Dispatchers.IO) {
            api.sendCode(SendCodeRequest(phone, consent))
        }
    }

    // Returns true if new user, false otherwise
    suspend fun verifyCode(phone: String, code: String): Boolean {
        return withContext(Dispatchers.IO) {
            val response = api.verifyCode(VerifyCodeRequest(phone, code))
            // Assuming SecureStorage has a saveToken method based on the architecture blueprint
            secureStorage.saveToken(response.token)
            response.isNewUser
        }
    }

    suspend fun updateProfile(firstname: String) {
        withContext(Dispatchers.IO) {
            api.updateProfile(UpdateProfileRequest(firstname))
        }
    }
}
