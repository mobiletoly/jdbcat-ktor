package jdbcat.ktor.example

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import jdbcat.core.tx
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.model.Department
import jdbcat.ktor.example.route.v1.model.AddOrUpdateDepartmentRequest
import jdbcat.ktor.example.route.v1.model.DepartmentResponse
import org.amshove.kluent.`should equal`
import org.koin.ktor.ext.inject
import org.spekframework.spek2.style.specification.describe
import java.util.Date
import javax.sql.DataSource

object `Manage Departments entities` : AppSpek({

    describe("PUT /departments/{code}") {
        context("when Department with this 'SEA' code does not exists") {
            it("should be created") {
                withApp {
                    val request = AddOrUpdateDepartmentRequest(
                        name = "Seattle office",
                        countryCode = "USA",
                        city = "Seattle",
                        comments = "Awesome office"
                    )
                    val call = handleRequest(HttpMethod.Put, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(request))
                    }
                    with(call) {
                        val response = jacksonMapper.readValue<DepartmentResponse>(response.content!!)
                        response.code `should equal` "SEA"
                        response.name `should equal` request.name
                        response.countryCode `should equal` request.countryCode
                        response.city `should equal` request.city
                        response.comments `should equal` request.comments
                    }
                }
            }
        }

        context("when Department with 'SEA' code already exists") {
            it("should be updated") {
                withApp {
                    val dataSource by application.inject<DataSource>()
                    val departmentDao by application.inject<DepartmentDao>()
                    val dateCreated = Date()
                    val department = Department(
                        code = "SEA",
                        name = "Seattle office",
                        countryCode = "USA",
                        city = "Seattle",
                        comments = "Awesome office",
                        dateCreated = dateCreated)
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                    }
                    val request = AddOrUpdateDepartmentRequest(
                        name = "-Seattle office-",
                        countryCode = "usa",
                        city = "Bellevue",
                        comments = "Even better office"
                    )
                    val call = handleRequest(HttpMethod.Put, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(request))
                    }
                    with(call) {
                        val response = jacksonMapper.readValue<DepartmentResponse>(response.content!!)
                        response.code `should equal` "SEA"
                        response.name `should equal` request.name
                        response.countryCode `should equal` request.countryCode
                        response.city `should equal` request.city
                        response.comments `should equal` request.comments
                        response.dateCreated `should equal` department.dateCreated
                    }
                }
            }
        }
    }
})
