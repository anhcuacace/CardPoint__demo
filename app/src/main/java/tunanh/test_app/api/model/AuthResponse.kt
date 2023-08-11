package tunanh.test_app.api.model

data class AuthResponse(
    val account: String,
    val amount: String,
    val authcode: String,
    val avsresp: String,
    val bintype: String,
    val commcard: String,
    val cvvresp: String,
    val entrymode: String,
    val expiry: String,
    val merchid: String,
    val respcode: String,
    val respproc: String,
    val respstat: String,
    val resptext: String,
    val retref: String,
    val token: String,
)