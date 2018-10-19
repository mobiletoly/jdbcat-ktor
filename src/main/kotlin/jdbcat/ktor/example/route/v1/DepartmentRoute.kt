package jdbcat.ktor.example.route.v1

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import jdbcat.core.tx
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.dao.EmployeeDao
import jdbcat.ktor.example.route.v1.model.AddEmployeeRequest
import jdbcat.ktor.example.route.v1.model.AddOrUpdateDepartmentRequest
import jdbcat.ktor.example.route.v1.model.DepartmentResponse
import jdbcat.ktor.example.route.v1.model.EmployeeResponse
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import java.net.URI
import javax.sql.DataSource

private val logger = KotlinLogging.logger { }

fun Route.departmentRoute() {

    val dataSource by inject<DataSource>()
    val departmentDao by inject<DepartmentDao>()
    val employeeDao by inject<EmployeeDao>()

    route("/departments") {

        // Get all departments
        get("/") { _ ->
            dataSource.tx { _ ->
                val departmentsResponse = departmentDao
                    .queryAll()
                    .map { DepartmentResponse.fromEntity(it) }
                    .toList()
                call.respond(departmentsResponse)
            }
        }

        get("/{code}") { _ ->
            val code = call.parameters["code"]!!
            dataSource.tx {
                val departmentResponse = departmentDao
                    .queryByCode(code = code)
                    .let { DepartmentResponse.fromEntity(it) }
                call.respond(departmentResponse)
            }
        }

        // Create new Department or update existing one
        // Since we want for caller to provide a department code (so basically caller is responsible
        // of creating "primary key") - we use PUT instead of POST to create a new resource.
        // Please read this discussions: https://stackoverflow.com/questions/630453/put-vs-post-in-rest
        put("/{code}") { _ ->
            val departmentRequest = call.receive<AddOrUpdateDepartmentRequest>()
            val code = call.parameters["code"]!!
            val departmentToAddOrUpdate = departmentRequest.toEntity(code = code)
            dataSource.tx {
                val departmentResponse = departmentDao
                    .insertOrUpdate(department = departmentToAddOrUpdate)
                    .let { DepartmentResponse.fromEntity(it) }
                call.respond(departmentResponse)
            }
        }

        delete("/{code}") {
            val code = call.parameters["code"]!!
            dataSource.tx {
                departmentDao.deleteByCode(code = code)
            }
            call.respond(HttpStatusCode.NoContent)
        }

        // Add new employee to an existing department
        post("/{code}/employees") {
            val employeeRequest = call.receive<AddEmployeeRequest>()
            val code = call.parameters["code"]!!
            val employeeToAdd = employeeRequest.toEntity(departmentCode = code)
            dataSource.tx {
                val updatedEmployee = employeeDao
                    .add(employee = employeeToAdd)
                    .let { EmployeeResponse.fromEntity(it) }
                // POST operations SHOULD support the Location response header to specify the location of any
                // created resource that was not explicitly named, via the Location header:
                // https://github.com/Microsoft/api-guidelines/blob/vNext/Guidelines.md#741-post
                val reqLocal = call.request.local
                val location = URI(
                    reqLocal.scheme, null, reqLocal.host, reqLocal.port,
                    "/employees/${updatedEmployee.id}", null, null
                )
                call.response.header(HttpHeaders.Location, location.toString())
                call.respond(HttpStatusCode.Created, updatedEmployee)
            }
        }
    }
}
