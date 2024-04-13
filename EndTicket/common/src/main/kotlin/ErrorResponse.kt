data class ErrorResponse(
    val code: Int,
    val message: String? = null,
    val detail: String? = null
)