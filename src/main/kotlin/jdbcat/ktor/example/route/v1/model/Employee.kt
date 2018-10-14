package jdbcat.ktor.example.route.v1.model

import jdbcat.ktor.example.db.model.Employee
import java.util.Date

// Request to add Employee record
data class AddEmployeeRequest(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val comments: String?
) {
    fun toEntity(departmentCode: String) = Employee(
        id = null,
        firstName = firstName,
        lastName = lastName,
        age = age,
        departmentCode = departmentCode,
        comments = comments,
        dateCreated = Date()
    )
}

// Request to modify Employee record
data class UpdateEmployeeRequest(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val departmentCode: String,
    val comments: String?
) {
    fun toEntity(id: Int) = Employee(
        id = id,
        firstName = firstName,
        lastName = lastName,
        age = age,
        departmentCode = departmentCode,
        comments = comments,
        dateCreated = Date()
    )
}

// Response with Employee record
data class EmployeeResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val departmentCode: String,
    val comments: String?,
    val dateCreated: Date
) {

    companion object {
        fun fromEntity(entity: Employee) =
            EmployeeResponse(
                id = entity.id!!,
                firstName = entity.firstName,
                lastName = entity.lastName,
                age = entity.age,
                departmentCode = entity.departmentCode,
                comments = entity.comments,
                dateCreated = entity.dateCreated!!
            )
    }
}
