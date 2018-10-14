package jdbcat.ktor.example.service

import jdbcat.core.asSequence
import jdbcat.core.sqlTemplate
import jdbcat.core.tx
import jdbcat.ktor.example.db.model.Department
import jdbcat.ktor.example.db.model.Departments
import jdbcat.ktor.example.db.model.Employee
import jdbcat.ktor.example.db.model.Employees
import mu.KotlinLogging
import javax.sql.DataSource

/**
 * Business logic to obtain employee reports.
 */
class EmployeeReportService(private val dataSource: DataSource) {
    private val logger = KotlinLogging.logger { }

    /** Query all employees within a lowerAge..upperAge age range. Results are grouped by department. */
    suspend fun allEmployeesWithinAgeRangeGroupedByDepartment(
        countryCode: String,
        lowerAge: Int,
        upperAge: Int
    ) = dataSource.tx { connection ->
        val stmt = selectSortedWithinAgeRange.prepareStatement(connection) {
            it[Departments.countryCode] = countryCode
            it[Employees.age, "lowerAge"] = lowerAge
            it[Employees.age, "upperAge"] = upperAge
        }
        logger.debug { "queryAllWithinAgeRangeGroupedByDepartment(): $stmt" }
        stmt.executeQuery()
            .asSequence()
            .map {
                val department = Department.extractFrom(it)
                val employee = Employee.extractFrom(it)
                department to employee
            }
            .groupBy({ it.first }, { it.second })
            .toMap()
    }

    companion object {
        // Prefer to keep SQL Template variable in companion objects or somewhere where it will
        // be calculated only 1 time. sqlTemplate() call performs some string manipulations and
        // it is better to avoid this recalculations every time when your code executes SQL query.
        private val selectSortedWithinAgeRange = sqlTemplate(Departments, Employees) { d, e ->
            """
            | SELECT t_dep.*, t_emp.*
            |   FROM
            |       ${d.tableName} AS t_dep
            |       LEFT OUTER JOIN ${e.tableName} AS t_emp
            |           ON t_dep.${d.code} = t_emp.${e.departmentCode}
            |   WHERE
            |       t_dep.${d.countryCode} = ${d.countryCode.v}
            |           AND
            |       t_emp.${e.age} >= ${e.age["lowerAge"]}
            |           AND
            |       t_emp.${e.age} <= ${e.age["upperAge"]}
            |   ORDER BY
            |       t_dep.${d.code}, t_emp.${e.lastName}, t_emp.${e.firstName}
            """
        }
    }
}
