package jdbcat.ktor.example.route

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import jdbcat.core.tx
import jdbcat.ktor.example.AppSpek
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.model.Department
import jdbcat.ktor.example.db.model.Employee
import jdbcat.ktor.example.route.v1.model.AddEmployeeRequest
import jdbcat.ktor.example.route.v1.model.AddOrUpdateDepartmentRequest
import jdbcat.ktor.example.route.v1.model.DepartmentResponse
import jdbcat.ktor.example.route.v1.model.EmployeeResponse
import org.amshove.kluent.`should equal`
import org.koin.ktor.ext.inject
import org.spekframework.spek2.style.specification.describe
import java.util.Date
import javax.sql.DataSource

object DepartmentsRouteTest : AppSpek({

    describe("Get all Departments - GET /api/v1/departments") {
        context("when two departments exist") {
            it("return list of two departments") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val seattleDepartment = newSeattleDepartment()
                    val chicagoDepartment = newChicagoDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(seattleDepartment)
                        departmentDao.insertOrUpdate(chicagoDepartment)
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/departments") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val list = jacksonMapper.readValue<List<DepartmentResponse>>(response.content!!)
                        list.size `should equal` 2
                        val seattleResponse = list.find { it.code == seattleDepartment.code }!!
                        seattleResponse sameAs seattleDepartment
                        val chicagoResponse = list.find { it.code == chicagoDepartment.code }!!
                        chicagoResponse sameAs chicagoDepartment
                    }
                }
            }
        }
    }

    describe("Get Department by code - GET /api/v1/departments/{code}") {
        context("when department exist") {
            it("return valid existing department") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val seattleDepartment = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(seattleDepartment)
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val seattleResponse = jacksonMapper.readValue<DepartmentResponse>(response.content!!)
                        seattleResponse sameAs seattleDepartment
                    }
                }
            }
        }
        context("when department does not exist") {
            it("fails with HTTP 404 NotFound") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val seattleDepartment = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(seattleDepartment)
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/departments/CHI") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NotFound
                    }
                }
            }
        }
    }

    describe("Add department - PUT /api/v1/departments/{code}") {
        context("when Department with {code} code does not exists") {
            it("should be created") {
                withApp {
                    val request = newUpdatedDepartmentRequest()
                    val call = handleRequest(HttpMethod.Put, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(request))
                    }
                    with(call) {
                        val response =
                            jacksonMapper.readValue<DepartmentResponse>(response.content!!)
                        response.code `should equal` "SEA"
                        response.name `should equal` request.name
                        response.countryCode `should equal` request.countryCode
                        response.city `should equal` request.city
                        response.comments `should equal` request.comments
                    }
                }
            }
        }

        context("when epartment with {code} code already exists") {
            it("should be updated") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                    }
                    val request = newUpdatedDepartmentRequest()
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

    describe("Delete Department - DELETE /api/v1/departments/{code}") {
        context("when Department with {code} code already exists") {
            it("should be deleted") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                    }
                    handleRequest(HttpMethod.Delete, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NoContent
                    }
                }
            }
        }
        context("when Department with {code} code does not exists") {
            it("should return HTTP 401 NotFound response") {
                withApp {
                    handleRequest(HttpMethod.Delete, "/api/v1/departments/SEA") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NotFound
                    }
                }
            }
        }
    }

    describe("Add Employee - POST /api/v1/departments/{code}/employees") {
        context("when Department with {code} exists") {
            it("should return HTTP 201 Created with added Employee object") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                    }
                    val employeeRequest = addEmployeeRequest()
                    handleRequest(HttpMethod.Post, "/api/v1/departments/SEA/employees") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(employeeRequest))
                    }.apply {
                        response.status() `should equal` HttpStatusCode.Created
                        val employeeResponse = jacksonMapper.readValue<EmployeeResponse>(response.content!!)
                        employeeResponse sameAs employeeRequest
                    }
                }
            }
        }
        context("when Department with {code} does not exist") {
            it("should return HTTP 401 NotFound response") {
                withApp {
                    handleRequest(HttpMethod.Post, "/api/v1/departments/ZZZ/employees") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(addEmployeeRequest()))
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NotFound
                    }
                }
            }
        }
    }
})

fun newSeattleDepartment() = Department(
    code = "SEA",
    name = "Seattle office",
    countryCode = "USA",
    city = "Seattle",
    comments = "Headquarter",
    dateCreated = Date()
)

fun newChicagoDepartment() = Department(
    code = "CHI",
    name = "Chicago office",
    countryCode = "USA",
    city = "Chicago",
    comments = "Financial department",
    dateCreated = Date()
)

fun newUpdatedDepartmentRequest() = AddOrUpdateDepartmentRequest(
    name = "-Seattle office-",
    countryCode = "USA",
    city = "Bellevue",
    comments = "Even better office"
)

fun newEmployee(departmentCode: String) = Employee(
    firstName = "Toly",
    lastName = "Pochkin",
    age = 40,
    departmentCode = departmentCode,
    comments = "CEO of company",
    dateCreated = Date()
)

fun addEmployeeRequest() = AddEmployeeRequest(
    firstName = "Toly",
    lastName = "Pochkin",
    age = 40,
    comments = "CEO of company"
)

private infix fun DepartmentResponse.sameAs(department: Department) {
    this.code `should equal` department.code
    this.name `should equal` department.name
    this.countryCode `should equal` department.countryCode
    this.city `should equal` department.city
    this.comments `should equal` department.comments
}
