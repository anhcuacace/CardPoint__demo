package tunanh.test_app.api.model

data class TokenResponse(
    val errorcode: Int,
    val message: String,
    val token: String,
)