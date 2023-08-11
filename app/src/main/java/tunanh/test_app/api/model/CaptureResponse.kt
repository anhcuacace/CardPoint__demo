package tunanh.test_app.api.model

data class CaptureResponse(
    val account: String,
    val amount: String,
    val authcode: String,
    val batchid: String,
    val merchid: String,
    val respcode: String,
    val respproc: String,
    val respstat: String,
    val resptext: String,
    val retref: String,
    val setlstat: String,
    val token: String,
)