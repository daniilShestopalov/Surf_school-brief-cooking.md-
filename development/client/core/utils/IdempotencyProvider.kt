package com.surfschool.core.utils

interface IdempotencyProvider {
    fun generateKey(): String
}

class IdempotencyProviderImpl : IdempotencyProvider {
    override fun generateKey(): String {
        // Simulated UUIDv4 for Multiplatform implementation. 
        // In reality, this relies on expect/actual or a multiplatform library like com.benasher44.uuid
        val chars = ('a'..'f') + ('0'..'9')
        fun randomHex(length: Int) = (1..length).map { chars.random() }.joinToString("")
        return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-8${randomHex(3)}-${randomHex(12)}"
    }
}
