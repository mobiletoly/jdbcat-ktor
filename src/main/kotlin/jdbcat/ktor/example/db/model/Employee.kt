package jdbcat.ktor.example.db.model

import jdbcat.core.ColumnValueBuilder
import jdbcat.core.ColumnValueExtractor
import jdbcat.core.Table
import jdbcat.core.integer
import jdbcat.core.varchar
import jdbcat.dialects.pg.pgSerial
import jdbcat.dialects.pg.pgText
import jdbcat.ext.javaDate
import java.util.Date

// Table definition
object Employees : Table(tableName = "employees") {
    val id = pgSerial("id", specifier = "PRIMARY KEY")
    val firstName = varchar("first_name", size = 50).nonnull()
    val lastName = varchar("last_name", size = 50).nonnull()
    val age = integer("age").nonnull()
    val departmentCode = varchar(
        "department_code",
        size = 3,
        specifier = "REFERENCES ${Departments.tableName} (${Departments.code.name})"
    ).nonnull()
    val comments = pgText("comments")
    val dateCreated = javaDate("date_created").nonnull()
}

// Table row definition
data class Employee(
    val id: Int? = null,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val departmentCode: String,
    val comments: String?,
    val dateCreated: Date?
) {
    fun copyFieldsTo(builder: ColumnValueBuilder) {
        if (id != null) {
            builder[Employees.id] = id
        }
        builder[Employees.firstName] = firstName
        builder[Employees.lastName] = lastName
        builder[Employees.age] = age
        builder[Employees.departmentCode] = departmentCode
        builder[Employees.comments] = comments
        if (dateCreated != null) {
            builder[Employees.dateCreated] = dateCreated
        }
    }

    companion object {
        fun extractFrom(value: ColumnValueExtractor) = Employee(
            id = value[Employees.id],
            firstName = value[Employees.firstName],
            lastName = value[Employees.lastName],
            age = value[Employees.age],
            departmentCode = value[Employees.departmentCode],
            comments = value[Employees.comments],
            dateCreated = value[Employees.dateCreated]
        )
    }
}
