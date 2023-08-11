package tunanh.test_app.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import tunanh.test_app.api.model.AuthRequest
import tunanh.test_app.api.model.AuthResponse
import tunanh.test_app.api.model.CaptureRequest
import tunanh.test_app.api.model.CaptureResponse
import tunanh.test_app.api.model.ProfileRequest
import tunanh.test_app.api.model.ProfileResponse
import tunanh.test_app.api.model.RefundRequest
import tunanh.test_app.api.model.RefundResponse
import tunanh.test_app.api.model.VoidByIdRequest
import tunanh.test_app.api.model.VoidByOrderidResponse
import tunanh.test_app.api.model.VoidByRetref
import tunanh.test_app.api.model.VoidResponse

interface CardPoint {
    @PUT("auth")
    suspend fun putAuth(@Body auth: AuthRequest): Response<AuthResponse>


    @PUT("capture")
    suspend fun putCapture(@Body captureRequest: CaptureRequest): Response<CaptureResponse>

    @PUT("void")
    suspend fun void(@Body voidByRetref: VoidByRetref): Response<VoidResponse>

    @PUT("voidByOrderId")
    suspend fun void(@Body voidByIdRequest: VoidByIdRequest): Response<VoidByOrderidResponse>

    @PUT("refund")
    suspend fun refund(@Body refundRequest: RefundRequest): Response<RefundResponse>

    @PUT("profile")
    suspend fun profile(@Body profileRequest: ProfileRequest): Response<ProfileResponse>
}