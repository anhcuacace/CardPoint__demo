package tunanh.test_app.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import tunanh.test_app.api.model.AccountRequest
import tunanh.test_app.api.model.AuthRequest
import tunanh.test_app.api.model.CaptureRequest
import tunanh.test_app.api.model.ProfileRequest
import tunanh.test_app.api.model.RefundRequest
import tunanh.test_app.api.model.VoidByIdRequest
import tunanh.test_app.api.model.VoidByRetref

class CallApiRepository private constructor() {
    companion object {
        private var instance: CallApiRepository? = null
        fun getInstance(): CallApiRepository = instance ?: synchronized(this) {
            instance ?: CallApiRepository()
                .also { instance = it }
        }
    }

    @get:Synchronized
    private val cardPointService = ApiClient.getCardPointService()

    @get:Synchronized
    private val csCardPointService = ApiClient.getCsCardPointService()

    private suspend fun <T> callApi(call: suspend () -> Response<T>): ApiResponse<T> =
        withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) ApiResponse.DataSuccess(body)
                    else ApiResponse.DataError(response.code(), response.message())
                } else {
                    ApiResponse.DataError(response.code(), response.message())
                }
            } catch (e: Exception) {
                ApiResponse.DataError(msg = e.message)
            }
        }


    suspend fun getToken(acc: AccountRequest) =
        callApi { csCardPointService.tokenize(acc) }

    suspend fun putAuth(auth: AuthRequest) =
        callApi { cardPointService.putAuth(auth) }

    suspend fun putCapture(captureRequest: CaptureRequest) =
        callApi { cardPointService.putCapture(captureRequest) }

    suspend fun void(voidByRetref: VoidByRetref) =
        callApi { cardPointService.void(voidByRetref) }

    suspend fun void(voidByIdRequest: VoidByIdRequest) =
        callApi { cardPointService.void(voidByIdRequest) }

    suspend fun refund(refundRequest: RefundRequest) =
        callApi { cardPointService.refund(refundRequest) }

    suspend fun profile(profileRequest: ProfileRequest) =
        callApi { cardPointService.profile(profileRequest) }


}