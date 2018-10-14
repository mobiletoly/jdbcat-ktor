package jdbcat.ktor.example.route.v1

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route
import jdbcat.core.tx
import jdbcat.ktor.example.db.dao.EmployeeDao
import jdbcat.ktor.example.route.v1.model.EmployeeResponse
import jdbcat.ktor.example.route.v1.model.UpdateEmployeeRequest
import org.koin.ktor.ext.inject
import javax.sql.DataSource

fun Route.employeeRoute() {

    val dataSource by inject<DataSource>()
    val employeeDao by inject<EmployeeDao>()

    route ("/employees") {

        // Get all employees
        get("/") {
            dataSource.tx {
                val employees = employeeDao.queryAll()
                    .map { e -> EmployeeResponse.fromEntity(e) }
                    .toList()
                call.respond(employees)
            }
        }

        get("/{id}") { _ ->
            val id = call.parameters["id"]!!.toInt()
            dataSource.tx {
                val employeeResponse = employeeDao
                    .queryById(id = id)
                    .let { EmployeeResponse.fromEntity(it) }
                call.respond(employeeResponse)
            }
        }

        put("/{id}") { _ ->
            val id = call.parameters["id"]!!.toInt()
            val employeeRequest = call.receive<UpdateEmployeeRequest>()
            val employeeToUpdate = employeeRequest.toEntity(id = id)
            dataSource.tx {
                val employeeResponse = employeeDao
                    .update(employee = employeeToUpdate)
                    .let { EmployeeResponse.fromEntity(it) }
                call.respond(employeeResponse)
            }
        }
    }
}
