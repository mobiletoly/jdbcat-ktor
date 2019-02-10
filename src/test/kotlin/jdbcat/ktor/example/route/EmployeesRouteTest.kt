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
import jdbcat.ktor.example.db.dao.EmployeeDao
import jdbcat.ktor.example.db.model.Employee
import jdbcat.ktor.example.route.v1.model.AddEmployeeRequest
import jdbcat.ktor.example.route.v1.model.EmployeeResponse
import jdbcat.ktor.example.route.v1.model.UpdateEmployeeRequest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.koin.ktor.ext.inject
import org.spekframework.spek2.style.specification.describe
import java.util.Date
import javax.sql.DataSource

object EmployeesRouteTest : AppSpek({

    describe("Get all Employees - GET /api/v1/employees") {
        context("when two Employees exist in database") {
            it("should return HTTP 200 OK with two Employee entities") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val employeeDao by application.inject<EmployeeDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    lateinit var employee1: Employee
                    lateinit var employee2: Employee
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                        employee1 = employeeDao.add(newEmployee())
                        employee2 = employeeDao.add(newEmployee2())
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/employees") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val list = jacksonMapper.readValue<List<EmployeeResponse>>(response.content!!)
                        list.size `should equal` 2
                        val response1 = list.find { it.id == employee1.id }!!
                        response1 sameAs employee1
                        val response2 = list.find { it.id == employee2.id }!!
                        response2 sameAs employee2
                    }
                }
            }
        }
    }

    describe("Get Employee by id - GET /api/v1/employees/{id}") {
        context("when Employee exists in database") {
            it("should return HTTP 200 OK with Employee entity") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val employeeDao by application.inject<EmployeeDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    val employee = dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                        employeeDao.add(newEmployee())
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/employees/${employee.id}") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val entityResponse = jacksonMapper.readValue<EmployeeResponse>(response.content!!)
                        entityResponse sameAs employee
                    }
                }
            }
        }
        context("when Employee with this id does not exist in database") {
            it("should return HTTP 404 NotFound") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val employeeDao by application.inject<EmployeeDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                        employeeDao.add(newEmployee())
                    }
                    handleRequest(HttpMethod.Get, "/api/v1/employees/99999") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NotFound
                    }
                }
            }
        }
    }

    describe("Update Employee - PUT /api/v1/employees/{id}") {
        context("when Employee with {id} exists") {
            it("should return HTTP 200 OK with updated Employee object") {
                withApp {
                    val departmentDao by application.inject<DepartmentDao>()
                    val employeeDao by application.inject<EmployeeDao>()
                    val dataSource by application.inject<DataSource>()
                    val department = newSeattleDepartment()
                    val employeeToAdd = newEmployee()
                    val addedEmployee = dataSource.tx {
                        departmentDao.insertOrUpdate(department)
                        employeeDao.add(employeeToAdd)
                    }
                    val employeeRequest = updateEmployeeRequest()
                    handleRequest(HttpMethod.Put, "/api/v1/employees/${addedEmployee.id!!}") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(employeeRequest))
                    }.apply {
                        response.status() `should equal` HttpStatusCode.OK
                        val employeeResponse = jacksonMapper.readValue<EmployeeResponse>(response.content!!)
                        employeeResponse sameAs employeeRequest
                    }
                }
            }
        }
        context("when Employee with {id} does not exists") {
            it("should return HTTP 404 NotFound") {
                withApp {
                    val employeeRequest = updateEmployeeRequest()
                    handleRequest(HttpMethod.Put, "/api/v1/employees/99999") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(jacksonMapper.writeValueAsString(employeeRequest))
                    }.apply {
                        response.status() `should equal` HttpStatusCode.NotFound
                    }
                }
            }
        }
    }
})

infix fun EmployeeResponse.sameAs(employee: Employee) {
    this.firstName `should equal` employee.firstName
    this.lastName `should equal` employee.lastName
    this.age `should equal` employee.age
    this.departmentCode `should equal` employee.departmentCode
    this.comments `should equal` employee.comments
}

infix fun EmployeeResponse.sameAs(request: AddEmployeeRequest) {
    this.firstName `should equal` request.firstName
    this.lastName `should equal` request.lastName
    this.age `should equal` request.age
    this.comments `should equal` request.comments
}

infix fun EmployeeResponse.sameAs(request: UpdateEmployeeRequest) {
    this.firstName `should equal` request.firstName
    this.lastName `should equal` request.lastName
    this.age `should equal` request.age
    this.departmentCode `should equal` request.departmentCode
    this.comments `should equal` request.comments
}

fun updateEmployeeRequest() = UpdateEmployeeRequest(
    firstName = "-Toly-",
    lastName = "-Pochkin-",
    age = 39,
    departmentCode = "SEA",
    comments = "-CEO of company-"
)

fun newEmployee() = Employee(
    id = null,
    firstName = "Toly",
    lastName = "Pochkin",
    age = 40,
    departmentCode = "SEA",
    comments = "CEO of company",
    dateCreated = Date()
)

fun newEmployee2() = Employee(
    id = null,
    firstName = "Jemmy",
    lastName = "Hyland",
    age = 27,
    departmentCode = "SEA",
    comments = "CPO",
    dateCreated = Date()
)
