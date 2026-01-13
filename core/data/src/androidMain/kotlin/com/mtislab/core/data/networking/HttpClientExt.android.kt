package com.mtislab.core.data.networking



import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Resource<T, DataError.Remote>
): Resource<T, DataError.Remote> {
    return try {
        val response = execute()
        handleResponse(response)
    } catch(e: UnknownHostException) {
        Resource.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: UnresolvedAddressException) {
        Resource.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: ConnectException) {
        Resource.Failure(DataError.Remote.NO_INTERNET)
    } catch(e: SocketTimeoutException) {
        Resource.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: HttpRequestTimeoutException) {
        Resource.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch(e: SerializationException) {
        Resource.Failure(DataError.Remote.SERIALIZATION)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Resource.Failure(DataError.Remote.UNKNOWN)
    }
}