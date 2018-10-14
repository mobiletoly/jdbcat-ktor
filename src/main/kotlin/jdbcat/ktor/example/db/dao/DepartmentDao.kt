package jdbcat.ktor.example.db.dao

import jdbcat.core.asSequence
import jdbcat.core.singleRow
import jdbcat.core.singleRowOrNull
import jdbcat.core.sqlDefinitions
import jdbcat.core.sqlNames
import jdbcat.core.sqlTemplate
import jdbcat.core.sqlValues
import jdbcat.core.txRequired
import jdbcat.dialects.pgAssignNamesToExcludedNames
import jdbcat.ktor.example.EntityNotFoundException
import jdbcat.ktor.example.db.model.Department
import jdbcat.ktor.example.db.model.Departments
import mu.KotlinLogging
import javax.sql.DataSource

/**
 * Data access object to provide basic manipulations with Department objects.
 *
 * Example:
 * val departmentDao = DepartmentDao(dataSource)
 * val updatedDepartment = dataSource.txAsync { connection ->
 *     departmentDao.insertOrUpdate(department = departmentToUpdate)
 * }
 *
 * -- Do not put your business logic in this class, DAO should stay as light as possible.
 * -- For anything more complicated (or when logic requires few tables to interact) - use "service" layer.
 */
class DepartmentDao(private val dataSource: DataSource) {

    private val logger = KotlinLogging.logger { }

    // txRequired enforces usage of transaction that needs to be started by caller
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

    // upsert functionality
    // if Department with this [Department.code] already exists - it will be updated
    // if it does not exist yet - it will be created.
    suspend fun insertOrUpdate(department: Department) = dataSource.txRequired { connection ->
        val stmt = upsertDepartmentSqlTemplate.prepareStatement(
            connection = connection,
            returningColumnsOnUpdate = listOf(Departments.dateCreated)
        ) {
            department.copyValuesTo(it)
        }
        logger.debug { "insertOrUpdate(): $stmt" }
        stmt.executeUpdate()
        val dateCreated = stmt.generatedKeys.singleRow { it[Departments.dateCreated] }
        department.copy(dateCreated = dateCreated)
    }

    suspend fun queryByCode(code: String) = dataSource.txRequired { connection ->
        val stmt = selectByCodeSqlTemplate.prepareStatement(connection) {
            it[Departments.code] = code
        }
        logger.debug { "getByCode(): $stmt" }
        stmt.executeQuery()
            .singleRowOrNull { Department.extractFrom(it) }
            ?: throw EntityNotFoundException(errorMessage = "Department code=$code cannot be found")
    }

    suspend fun queryAll() = dataSource.txRequired { connection ->
        val stmt = selectAllOrderedByCodeSqlTemplate.prepareStatement(connection)
        logger.debug { "queryAll(): $stmt" }
        stmt.executeQuery().asSequence().map {
            Department.extractFrom(it)
        }
    }

    // SQL templates. For performance reasons it is always better to create constants with SQL templates
    // instead of doing it directly in DAO functions.
    companion object {

        // Create table request with unique constraint on countryCode and city
        // (I know there could be a possibility of country having multiple cities with the same name,
        // this is just a demo case)
        private val createTableIfNotExistsSqlTemplate = sqlTemplate(Departments) {
            """
            | CREATE TABLE IF NOT EXISTS $tableName (
            |   ${columns.sqlDefinitions},
            |   UNIQUE ( $countryCode, $city )
            | )
            """
        }

        private val dropTableIfExistsSqlTemplate = sqlTemplate(Departments) {
            "DROP TABLE IF EXISTS $tableName"
        }

        // Upsert (Update or Insert) functionality
        // https://www.postgresql.org/docs/9.5/static/sql-insert.html
        private val upsertDepartmentSqlTemplate = sqlTemplate(Departments) {
            """
            | INSERT INTO $tableName (${columns.sqlNames}) VALUES (${columns.sqlValues})
            | ON CONFLICT ($code)
            | DO UPDATE SET ${(columns - dateCreated).pgAssignNamesToExcludedNames}
            """
            // We don't want to update dateCreated field,
            // it needs to be updated only once - when record is added
        }

        private val selectByCodeSqlTemplate = sqlTemplate(Departments) {
            "SELECT * FROM $tableName WHERE $code = ${code.v}"
        }

        private val selectAllOrderedByCodeSqlTemplate = sqlTemplate(Departments) {
            "SELECT * FROM $tableName ORDER BY $code"
        }
    }
}
