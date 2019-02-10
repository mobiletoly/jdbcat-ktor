package jdbcat.ktor.example.route

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import jdbcat.core.tx
import jdbcat.ktor.example.AppSpek
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.dao.EmployeeDao
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.koin.ktor.ext.inject
import org.spekframework.spek2.style.specification.describe
import javax.sql.DataSource

object AdminRouteTest : AppSpek({

    describe("Initial mock data bootstrap - POST /api/v1/admin/bootstrap") {
        it("should return HTTP 200 OK and data must be initialized") {
            withApp {
                val departmentDao by application.inject<DepartmentDao>()
                val employeeDao by application.inject<EmployeeDao>()
                val dataSource by application.inject<DataSource>()
                handleRequest(HttpMethod.Post, "/api/v1/admin/bootstrap") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }.apply {
                    response.status() `should equal` HttpStatusCode.NoContent
                    val numEmployees = dataSource.tx { employeeDao.countAll() }
                    val numDepartments = dataSource.tx { departmentDao.countAll() }
                    numEmployees `should equal` 5
                    numDepartments `should equal` 4
                }
            }
        }
    }
})
