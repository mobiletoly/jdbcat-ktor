package jdbcat.ktor.example.route

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import jdbcat.ktor.example.AppSpek
import jdbcat.ktor.example.route.v1.model.DepartmentResponse
import org.amshove.kluent.`should equal`
import org.spekframework.spek2.style.specification.describe

object ReportRouteTest : AppSpek({

    describe("Retrieve report - GET /api/v1/reports/departments/employees") {
        context("when has valid query arguments") {
            it("should return report") {
                withApp {
                    handleRequest(HttpMethod.Post, "/admin/bootstrap").apply {
                        response.status() `should equal` HttpStatusCode.NoContent
                    }
                    handleRequest(
                        method = HttpMethod.Get,
                        uri = "/api/v1/reports/departments/employees?country-code=USA&lower-age=25&upper-age=35"
                    ) {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val departments = jacksonMapper.readValue<List<DepartmentResponse>>(response.content!!)
                        departments.size `should equal` 2
                    }
                }
            }
        }
        context("when no query arguments") {
            it("should fail with HTTP 400 BadRequest") {
                withApp {
                    handleRequest(
                        method = HttpMethod.Get,
                        uri = "/api/v1/reports/departments/employees"
                    ) {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.BadRequest
                    }
                }
            }
        }
    }
})
