package com.surfschool.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

interface SecureStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}

class SecureStorageImpl(
    private val settings: Settings
) : SecureStorage {
    companion object {
        private const val KEY_TOKEN = "bearer_token"
    }

    override fun saveToken(token: String) {
        settings[KEY_TOKEN] = token
    }

    override fun getToken(): String? {
        return settings.getStringOrNull(KEY_TOKEN)
    }

    override fun clearToken() {
        settings.remove(KEY_TOKEN)
    }
}
