package tunanh.test_app.api.model

data class ProfileRequest(
    val account: String,
    val address: String,
    val city: String,
    val country: String,
    val currency: String,
    val expiry: String,
    val merchid: String,
    val name: String,
    val postal: String,
    val region: String,
)