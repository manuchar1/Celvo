package com.mtislab.core.data.networking


import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.statement.HttpResponse

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Resource<T, DataError.Remote>
): Resource<T, DataError.Remote> {
    TODO("Not yet implemented")
}