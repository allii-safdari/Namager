package com.example.core.error

/**
 * Typed failures thrown by the data layer. Presentation maps these to localized
 * messages, so repositories never contain UI copy.
 */
sealed class AppException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Network(cause: Throwable? = null) :
        AppException("Network request failed", cause)

    class Timeout(cause: Throwable? = null) :
        AppException("Network request timed out", cause)

    class Server(val statusCode: Int) :
        AppException("Server returned HTTP $statusCode")

    class Parse(cause: Throwable? = null) :
        AppException("The server response could not be parsed", cause)

    class Storage(cause: Throwable? = null) :
        AppException("Local storage operation failed", cause)

    class NotFound(message: String = "Requested content was not found") :
        AppException(message)
}
