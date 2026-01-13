package com.mtislab.core.domain.utils

sealed interface Resource<out D, out E: Error> {
    data class Success<out D>(val data: D): Resource<D, Nothing>
    data class Failure<out E: Error>(val error: E): Resource<Nothing, E>
}

inline fun <T, E: Error, R> Resource<T, E>.map(map: (T) -> R): Resource<R, E> {
    return when(this) {
        is Resource.Failure -> Resource.Failure(error)
        is Resource.Success -> Resource.Success(map(this.data))
    }
}

inline fun <T, E: Error> Resource<T, E>.onSuccess(action: (T) -> Unit): Resource<T, E> {
    return when(this) {
        is Resource.Failure -> this
        is Resource.Success -> {
            action(this.data)
            this
        }
    }
}

inline fun <T, E: Error> Resource<T, E>.onFailure(action: (E) -> Unit): Resource<T, E> {
    return when(this) {
        is Resource.Failure -> {
            action(error)
            this
        }
        is Resource.Success -> this
    }
}

fun <T, E: Error> Resource<T, E>.asEmptyResult(): EmptyResult<E> {
    return map {  }
}

typealias EmptyResult<E> = Resource<Unit, E>