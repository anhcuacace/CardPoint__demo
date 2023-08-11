package tunanh.test_app


sealed class DataResponse<T>(val loadingStatus: LoadingStatus) {
    class DataLoading<T> : DataResponse<T>(LoadingStatus.Loading)
    class DataIdle<T> : DataResponse<T>(LoadingStatus.Idle)
    data class DataError<T, V>(val errorData: V? = null) : DataResponse<T>(LoadingStatus.Error)
    data class DataSuccess<T>(val body: T) : DataResponse<T>(LoadingStatus.Success)
}
enum class LoadingStatus {
    Idle, Loading, Success, Error
}