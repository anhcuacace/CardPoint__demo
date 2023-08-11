package tunanh.test_app.api.model

data class ProfileResponse(
    val acctid: String,
    val accttype: String,
    val address: String,
    val auoptout: String,
    val city: String,
    val cofpermission: String,
    val country: String,
    val defaultacct: String,
    val expiry: String,
    val name: String,
    val postal: String,
    val profileid: String,
    val region: String,
    val respcode: String,
    val respproc: String,
    val respstat: String,
    val resptext: String,
    val token: String,
)