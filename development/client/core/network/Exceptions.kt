package com.surfschool.core.network

open class AppException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class RateLimitException(val retryAfterSeconds: Int, message: String = "Too Many Requests") : AppException(message)
class UnauthorizedException(message: String = "Unauthorized") : AppException(message)
class NetworkException(message: String = "Network Error", cause: Throwable? = null) : AppException(message, cause)
class ServerException(val code: Int, message: String = "Server Error") : AppException(message)
