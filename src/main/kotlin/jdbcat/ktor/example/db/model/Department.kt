package jdbcat.ktor.example.db.model

import jdbcat.core.ColumnValueBuilder
import jdbcat.core.ColumnValueExtractor
import jdbcat.core.Table
import jdbcat.dialects.pg.pgText
import jdbcat.ext.javaDate
import java.util.Date

// Table definition (representation of "departments" database table)
object Departments : Table(tableName = "departments") {
    val code = varchar("code", size = 3, specifier = "PRIMARY KEY").nonnull()
    val name = varchar("name", size = 100, specifier = "UNIQUE").nonnull()
    val countryCode = varchar("country_code", size = 3).nonnull()
    val city = varchar("city", size = 20).nonnull()
    val comments = pgText("comments")
    val dateCreated = javaDate("date_created").nonnull()
}

// Table row definition
data class Department(
    val code: String,
    val name: String,
    val countryCode: String,
    val city: String,
    val comments: String?,
    val dateCreated: Date?
) {
    fun copyValuesTo(builder: ColumnValueBuilder) {
        builder[Departments.code] = code
        builder[Departments.name] = name
        builder[Departments.countryCode] = countryCode
        builder[Departments.city] = city
        builder[Departments.comments] = comments
        if (dateCreated != null) {
            builder[Departments.dateCreated] = dateCreated
        }
    }

    companion object {
        fun extractFrom(extractor: ColumnValueExtractor) = Department(
            code = extractor[Departments.code],
            name = extractor[Departments.name],
            countryCode = extractor[Departments.countryCode],
            city = extractor[Departments.city],
            comments = extractor[Departments.comments],
            dateCreated = extractor[Departments.dateCreated]
        )
    }
}
