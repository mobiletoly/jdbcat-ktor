package jdbcat.ktor.example.route

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import jdbcat.ktor.example.route.model.HealthCheckResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun Route.healthCheckRoute() {

    route ("/healthcheck") {

        get {
            val healthCheckResponse = HealthCheckResponse(ready = true)
            call.respond(healthCheckResponse)
        }
    }
}
