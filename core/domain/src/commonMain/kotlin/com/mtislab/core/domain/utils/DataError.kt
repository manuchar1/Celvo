package com.mtislab.core.domain.utils

sealed interface DataError : Error {
    enum class Remote : DataError {
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        SERIALIZATION,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN
    }
}

/**
 * True when the failure was caused by a lack of internet connectivity rather
 * than a server/API fault. Drives the choice between the no-internet
 * placeholder (auto-recovers on reconnect) and the generic error placeholder.
 */
val DataError.isConnectivityError: Boolean
    get() = this == DataError.Remote.NO_INTERNET
