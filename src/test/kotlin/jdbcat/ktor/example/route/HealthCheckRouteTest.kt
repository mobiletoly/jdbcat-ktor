package jdbcat.ktor.example.route

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import jdbcat.ktor.example.AppSpek
import jdbcat.ktor.example.route.v1.model.HealthCheckResponse
import org.amshove.kluent.`should be`
import org.spekframework.spek2.style.specification.describe

object HealthCheckRouteTest : AppSpek({

    describe("HTTP GET /api/v1/healthcheck") {
        context("when application is healthy") {
            it("returns healthy healthcheck response") {
                withApp {
                    with(handleRequest(HttpMethod.Get, "/api/v1/healthcheck")) {
                        val objectMapper = jacksonObjectMapper()
                        val healthCheckResponse = objectMapper.readValue<HealthCheckResponse>(response.content!!)
                        healthCheckResponse.ready `should be` true
                    }
                }
            }
        }
    }
})
