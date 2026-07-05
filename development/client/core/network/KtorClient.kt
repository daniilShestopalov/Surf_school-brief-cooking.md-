package com.surfschool.core.network

import com.surfschool.core.storage.SecureStorage
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableSharedFlow

// Global event bus for navigation events triggered from Data layer
val globalNavigationEvents = MutableSharedFlow<GlobalNavigationEvent>(extraBufferCapacity = 1)

enum class GlobalNavigationEvent {
    NavigateToAuth
}

fun createHttpClient(
    engine: HttpClientEngine,
    secureStorage: SecureStorage
): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(Auth) {
            bearer {
                loadTokens {
                    secureStorage.getToken()?.let { BearerTokens(it, "") }
                }
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                when (response.status) {
                    HttpStatusCode.Unauthorized -> {
                        secureStorage.clearToken()
                        globalNavigationEvents.tryEmit(GlobalNavigationEvent.NavigateToAuth)
                        throw UnauthorizedException()
                    }
                    HttpStatusCode.TooManyRequests -> {
                        val retryAfter = response.headers["Retry-After"]?.toIntOrNull() ?: 60
                        throw RateLimitException(retryAfterSeconds = retryAfter)
                    }
                }
                
                if (response.status.value in 500..599) {
                    throw ServerException(response.status.value)
                }
            }
            
            handleResponseExceptionWithRequest { exception, _ ->
                // Map common I/O and Socket exceptions to our Domain NetworkException
                throw NetworkException(cause = exception)
            }
        }
        
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
