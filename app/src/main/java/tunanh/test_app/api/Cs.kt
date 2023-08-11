package tunanh.test_app.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import tunanh.test_app.api.model.AccountRequest
import tunanh.test_app.api.model.TokenResponse

interface Cs {
    @POST
    suspend fun tokenize(
        @Body requestBody: AccountRequest, @Url url: String = ApiClient.CsURL,
    ): Response<TokenResponse>
}