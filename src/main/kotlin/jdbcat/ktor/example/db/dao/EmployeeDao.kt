package jdbcat.ktor.example.db.dao

import jdbcat.core.EphemeralTable
import jdbcat.core.asSequence
import jdbcat.core.integer
import jdbcat.core.singleRow
import jdbcat.core.singleRowOrNull
import jdbcat.core.sqlAssignNamesToValues
import jdbcat.core.sqlDefinitions
import jdbcat.core.sqlNames
import jdbcat.core.sqlTemplate
import jdbcat.core.sqlValues
import jdbcat.core.txRequired
import jdbcat.dialects.pg.PSQLState
import jdbcat.dialects.pg.hasState
import jdbcat.ktor.example.EntityNotFoundException
import jdbcat.ktor.example.db.model.Employee
import jdbcat.ktor.example.db.model.Employees
import mu.KotlinLogging
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Data access object to provide basic manipulations with Employee objects.
 *
 * val employeeDao = EmployeeDao(dataSource)
 * val addedEmployee = dataSource.tx { connection ->
 *     employeeDao.add(employee = employeeToAdd)
 * }
 *
 * -- Do not put your business logic here, DAO should stay as light as possible.
 * -- For anything more complicated (or when logic requires few tables to interact) - use "service" layer.
 */
class EmployeeDao(private val dataSource: DataSource) {

    private val logger = KotlinLogging.logger { }

    suspend fun createTableIfNotExists() = dataSource.txRequired { connection ->
        val stmt = createTableIfNotExistsSqlTemplate.prepareStatement(connection)
        logger.debug { "createTableIfNotExists(): $stmt" }
        stmt.executeUpdate()
    }

    suspend fun dropTableIfExists() = dataSource.txRequired { connection ->
        val stmt = dropTableIfExistsSqlTemplate.prepareStatement(connection)
        logger.debug { "dropTableIfExists(): $stmt" }
        stmt.executeUpdate()
    }

    suspend fun add(employee: Employee) = dataSource.txRequired { connection ->
        val stmt = insertNewEmployeeSqlTemplate
            .prepareStatement(
                connection = connection,
                returningColumnsOnUpdate = listOf(Employees.id))
            .setColumns {
                employee.copyFieldsTo(it)
            }
        logger.debug { "add(): $stmt" }
        try {
            stmt.executeUpdate()
        } catch (ex: SQLException) {
            if (ex.hasState(PSQLState.FOREIGN_KEY_VIOLATION)) {
                // PostgreSQL specific state that most likely came from attempt to update Employees entity
                // with"departmentCode" pointing to non-existing column in Departments
                throw EntityNotFoundException("Department with code=${Employees.departmentCode} does not exist")
            }
            throw ex
        }
        val employeeId: Int = stmt.generatedKeys.singleRow { it[Employees.id] }
        employee.copy(id = employeeId)
    }

    suspend fun update(employee: Employee) = dataSource.txRequired { connection ->
        val stmt = updateEmployeeSqlTemplate
            .prepareStatement(
                connection = connection,
                returningColumnsOnUpdate = listOf(Employees.dateCreated))
            .setColumns {
                employee.copyFieldsTo(it)
            }
        logger.debug { "update(): $stmt" }
        if (stmt.executeUpdate() == 0) {
            throw EntityNotFoundException(errorMessage = "Entity Employee id=${employee.id} " +
                "was not found and cannot be updated")
        }
        val dateCreated = stmt.generatedKeys.singleRow { it[Employees.dateCreated] }
        employee.copy(dateCreated = dateCreated)
    }

    suspend fun queryById(id: Int) = dataSource.txRequired { connection ->
        val stmt = selectByIdSqlTemplate
            .prepareStatement(connection)
            .setColumns {
                it[Employees.id] = id
            }
        logger.debug { "queryById(): $stmt" }
        val rs = stmt.executeQuery()
        rs.singleRowOrNull { Employee.extractFrom(it) } ?: throw EntityNotFoundException("Entity Employee cannot be found by id=$id")
    }

    suspend fun queryAll() = dataSource.txRequired { connection ->
        val stmt = selectAll.prepareStatement(connection)
        logger.debug { "queryAll(): $stmt" }
        stmt.executeQuery().asSequence().map {
            Employee.extractFrom(it)
        }
    }

    suspend fun countAll() = dataSource.txRequired { connection ->
        val stmt = countAll.prepareStatement(connection)
        logger.debug { "countAll(): $stmt" }
        stmt.executeQuery().singleRow { it[CounterResult.counter] }
    }

    // SQL templates. For performance reasons it is always better to create constants with SQL templates
    // instead of doing it directly in DAO functions.
    companion object {

        private val createTableIfNotExistsSqlTemplate = sqlTemplate(Employees) {
            """
            | CREATE TABLE IF NOT EXISTS $tableName (${columns.sqlDefinitions});
            | CREATE INDEX IF NOT EXISTS ${age.sqlIndexName} ON $tableName ( $age );
            """
        }

        private val dropTableIfExistsSqlTemplate = sqlTemplate(Employees) {
            "DROP TABLE IF EXISTS $tableName"
        }

        private val insertNewEmployeeSqlTemplate = sqlTemplate(Employees) {
            "INSERT INTO $tableName (${(columns - id).sqlNames}) VALUES (${(columns - id).sqlValues})"
        }

        private val updateEmployeeSqlTemplate = sqlTemplate(Employees) {
            // .sqlAssignNamesToValues property represents a string with comma-separated column name
            // to value assignments such as: "code = ..., firstName = ..., ..."
            // since we don't want to assign any values to "id" and "dateCreated" fields (there
            // were assigned during creation of row) - we exclude them from a list.
            """
            | UPDATE $tableName
            |   SET ${(columns - id - dateCreated).sqlAssignNamesToValues}
            |   WHERE $id = ${id.v}
            """
        }

        private val selectByIdSqlTemplate = sqlTemplate(Employees) {
            "SELECT * FROM $tableName WHERE $id = ${id.v}"
        }

        private val selectAll = sqlTemplate(Employees) {
            "SELECT * FROM $tableName ORDER BY $dateCreated"
        }

        private val countAll = sqlTemplate(CounterResult, Employees) { cr, e ->
            "SELECT COUNT(*) AS ${cr.counter} FROM ${e.tableName}"
        }

        object CounterResult : EphemeralTable() {
            val counter = integer("counter").nonnull()
        }
    }
}
