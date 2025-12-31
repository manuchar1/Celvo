package com.mtislab.core.data.networking



import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import platform.Foundation.NSURLErrorCallIsActive
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDataNotAllowed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorInternationalRoamingOff
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorResourceUnavailable
import platform.Foundation.NSURLErrorTimedOut
import kotlin.coroutines.coroutineContext

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Resource<T, DataError.Remote>
): Resource<T, DataError.Remote> {
    return try {
        val response = execute()
        handleResponse(response)
    } catch(e: DarwinHttpRequestException) {
        handleDarwinException(e)
    } catch(_: UnresolvedAddressException) {
        Resource.Failure(DataError.Remote.NO_INTERNET)
    } catch(_: HttpRequestTimeoutException) {
        Resource.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(_: SerializationException) {
        Resource.Failure(DataError.Remote.SERIALIZATION)
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        Resource.Failure(DataError.Remote.UNKNOWN)
    }
}

private fun handleDarwinException(e: DarwinHttpRequestException): Resource<Nothing, DataError.Remote> {
    val nsError = e.origin

    return if(nsError.domain == NSURLErrorDomain) {
        when(nsError.code) {
            NSURLErrorNotConnectedToInternet,
            NSURLErrorNetworkConnectionLost,
            NSURLErrorCannotFindHost,
            NSURLErrorDNSLookupFailed,
            NSURLErrorResourceUnavailable,
            NSURLErrorInternationalRoamingOff,
            NSURLErrorCallIsActive,
            NSURLErrorDataNotAllowed -> {
                Resource.Failure(DataError.Remote.NO_INTERNET)
            }

            NSURLErrorTimedOut -> Resource.Failure(DataError.Remote.REQUEST_TIMEOUT)
            else -> Resource.Failure(DataError.Remote.UNKNOWN)
        }
    } else Resource.Failure(DataError.Remote.UNKNOWN)
}