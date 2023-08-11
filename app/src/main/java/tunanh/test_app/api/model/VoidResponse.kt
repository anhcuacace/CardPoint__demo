package tunanh.test_app.api.model

data class VoidResponse(
    val amount: String,
    val authcode: String,
    val currency: String,
    val merchid: String,
    val respcode: String,
    val respproc: String,
    val respstat: String,
    val resptext: String,
    val retref: String,
)