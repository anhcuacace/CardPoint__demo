package tunanh.test_app.api.model

import com.google.gson.annotations.SerializedName

data class AccountRequest(
//    @SerializedName("account")
//    val accountId: String,
    @SerializedName("devicedata")
    val deviceData: String
)
