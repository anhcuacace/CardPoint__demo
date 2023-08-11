package tunanh.test_app.api.model

data class VoidByOrderidResponse(
    val orderid: String,
    val respcode: String,
    val respproc: String,
    val respstat: String,
    val resptext: String,
)