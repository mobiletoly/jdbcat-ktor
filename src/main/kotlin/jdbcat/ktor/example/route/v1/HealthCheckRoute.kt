package jdbcat.ktor.example.route.v1

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import jdbcat.ktor.example.route.v1.model.HealthCheckResponse
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
