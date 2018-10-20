package jdbcat.ktor.example.route.v1.model

import java.util.Date

data class HealthCheckResponse(
    val ready: Boolean,
    val responseTimestamp: Date = Date()
)
