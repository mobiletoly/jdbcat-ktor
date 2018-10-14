package jdbcat.ktor.example.route.v1

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import jdbcat.ktor.example.route.v1.model.DepartmentResponse
import jdbcat.ktor.example.route.v1.model.EmployeeResponse
import jdbcat.ktor.example.service.EmployeeReportService
import org.koin.ktor.ext.inject

fun Route.reportRoute() {

    val employeeReportService by inject<EmployeeReportService>()

    route("/reports") {

        // Get all employees grouped by departments
        get("/departments/employees") { _ ->
            val countryCode = call.parameters["country-code"]!!
            val lowerAge = call.parameters["lower-age"]!!.toInt()
            val upperAge = call.parameters["upper-age"]!!.toInt()
            val departmentsToEmployees = employeeReportService
                .allEmployeesWithinAgeRangeGroupedByDepartment(
                    countryCode = countryCode,
                    lowerAge = lowerAge,
                    upperAge = upperAge)
                // We don't want to return database models and instead we are going to return
                // set of DepartmentResponse objects with list of employees attached to every one of them
                .map { (department, employeeList) ->
                    val departmentResponse = DepartmentResponse.fromEntity(department)
                    val employeeListResponse = employeeList
                        .map { EmployeeResponse.fromEntity(it) }
                    departmentResponse.copy(employees = employeeListResponse)
                }
            call.respond(departmentsToEmployees)
        }
    }
}
