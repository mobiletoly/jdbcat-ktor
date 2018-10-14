package jdbcat.ktor.example.route.v1.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jdbcat.ktor.example.db.model.Department
import java.util.Date

// Request to add/update Department record
data class AddOrUpdateDepartmentRequest(
    val name: String,
    val countryCode: String,
    val city: String,
    // JSON request field name can be different from parameter name
    @JsonProperty("notes") val comments: String?
) {
    /**
     * Having "code" simplifies deserialization process. PUT requests
     * does not have "code" field in payload, because typical request is
     * PUT /api/v1/departments/{code}
     * with serialized AddOrUpdateDepartmentRequest body payload.
     * But we still need to have mandatory [code] field, so here we go, once ktor extracts
     * it as a parameter from URI request - it needs to be passed here:
     */
    fun toEntity(code: String) = Department(
        code = code,
        name = name,
        countryCode = countryCode,
        city = city,
        comments = comments,
        dateCreated = Date()
    )
}

// Response with Department record
data class DepartmentResponse(
    val code: String,
    val name: String,
    val countryCode: String,
    val city: String,
    // JSON response field name can be different from parameter name
    @JsonProperty("notes") val comments: String?,
    val dateCreated: Date,
    // We can provide a list of employees if necessary
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val employees: Collection<EmployeeResponse>? = null
) {
    companion object {
        fun fromEntity(entity: Department) =
            DepartmentResponse(
                code = entity.code,
                name = entity.name,
                countryCode = entity.countryCode,
                city = entity.city,
                comments = entity.comments,
                dateCreated = entity.dateCreated!!
            )
    }
}
