package jdbcat.ktor.example.route.model

import java.util.Date

data class HealthCheckResponse(
    val ready: Boolean,
    val responseTimestamp: Date = Date()
)
