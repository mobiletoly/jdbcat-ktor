package jdbcat.ktor.example

import io.ktor.http.HttpStatusCode

/**
 * Inherit all application-specific exceptions from this class.
 */
open class AppGenericException(
    val httpStatusCode: HttpStatusCode,
    val errorMessage: String
) : RuntimeException("HTTP status code = $httpStatusCode, errorMessage = $errorMessage") {

    fun toResponse() = mapOf(
        "errorMessage" to errorMessage
    )
}

class EntityNotFoundException(errorMessage: String) : AppGenericException(
    httpStatusCode = HttpStatusCode.NotFound,
    errorMessage = errorMessage
)
