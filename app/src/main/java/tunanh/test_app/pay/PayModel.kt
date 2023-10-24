package tunanh.test_app.pay

data class PayModel(
    val cardNumber: String,
    val expiry: String,
    val name: String,
    val create: Long = System.currentTimeMillis(),
    var cardData: String = ""
)