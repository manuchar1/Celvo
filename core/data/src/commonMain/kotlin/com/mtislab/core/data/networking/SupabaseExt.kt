package com.mtislab.core.data.networking

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend fun <T> safeSupabaseCall(
    action: suspend () -> T
): Resource<T, DataError.Remote> {
    return try {
        Resource.Success(action())
    } catch (e: RestException) {
        val error = when(e.statusCode) {
            400 -> DataError.Remote.BAD_REQUEST
            401 -> DataError.Remote.UNAUTHORIZED
            403 -> DataError.Remote.FORBIDDEN
            404 -> DataError.Remote.NOT_FOUND
            408 -> DataError.Remote.REQUEST_TIMEOUT
            409 -> DataError.Remote.CONFLICT
            429 -> DataError.Remote.TOO_MANY_REQUESTS
            in 500..599 -> DataError.Remote.SERVER_ERROR
            else -> DataError.Remote.UNKNOWN
        }
        Resource.Failure(error)
    } catch (e: HttpRequestTimeoutException) { // Ktor-ის Timeout
        Resource.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        // ინტერნეტის შემოწმება ზოგადი ექსეპშენით
        Resource.Failure(DataError.Remote.UNKNOWN)
    }
}