package tunanh.test_app.api

sealed class ApiResponse<T> constructor(val loadingStatus: LoadingStatus) {
    class DataLoading<T> : ApiResponse<T>(LoadingStatus.Loading)
    class DataIdle<T> : ApiResponse<T>(LoadingStatus.Idle)
    class DataError<T>(val code: Int? = null, val msg: String? = "") :
        ApiResponse<T>(LoadingStatus.Error)

    data class DataSuccess<T>(val body: T) : ApiResponse<T>(LoadingStatus.Success)
}

enum class LoadingStatus {
    Idle, Loading, Success, Error
}