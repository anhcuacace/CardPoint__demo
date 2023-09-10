package tunanh.test_app.pay

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import tunanh.test_app.api.ApiResponse
import tunanh.test_app.api.CallApiRepository
import tunanh.test_app.api.model.AccountRequest
import tunanh.test_app.api.model.AuthRequest
import tunanh.test_app.api.model.AuthResponse
import tunanh.test_app.api.model.CaptureRequest
import tunanh.test_app.api.model.CaptureResponse
import tunanh.test_app.api.model.TokenResponse
import tunanh.test_app.pre.ConnectIdTech

class PayViewModel : ViewModel() {

    private val _cardDataState = MutableStateFlow(PayModel("", "", ""))
    private val payModel = MutableStateFlow(PayModel("", "", ""))

    val cardDataState: StateFlow<PayModel> = payModel
    private val connect = ConnectIdTech.getInstance()
    private val _canListener = MutableStateFlow(true)
    val canListener: StateFlow<Boolean> = _canListener

    val canPay = MutableStateFlow(false)


    private val _messageState = MutableStateFlow("")
    val messageState: StateFlow<String> = _messageState

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response

    val disconnectState = connect.disconnectState

    val autoConnectState = connect.availableConnect

    val message = connect.message

    private val repository by lazy { CallApiRepository.getInstance() }


    private val tokenState = MutableStateFlow<ApiResponse<TokenResponse>>(ApiResponse.DataIdle())

    private val authState = MutableStateFlow<ApiResponse<AuthResponse>>(ApiResponse.DataIdle())

    private val captureState =
        MutableStateFlow<ApiResponse<CaptureResponse>>(ApiResponse.DataIdle())

    fun listener(context: Context) {
        _response.value = ""
        _cardDataState.value = PayModel("", "", "")
        connect.listenerIdTech(context, _canListener)
//        connect.setSwipeListener(_cardDataState)
    }

    private suspend fun <T> loadData(
        state: MutableStateFlow<ApiResponse<T>>,
        action: suspend () -> ApiResponse<T>,
    ) {
        if (state.value is ApiResponse.DataLoading) return
        state.value = ApiResponse.DataLoading()
        state.value = action.invoke()
    }

    private suspend fun <I, O> waitingSussesAndLoadData(
        waitingState: MutableStateFlow<ApiResponse<I>>,
        getData: suspend () -> ApiResponse<I>,
        outStatus: MutableStateFlow<ApiResponse<O>>,
        action: suspend (ApiResponse<I>) -> ApiResponse<O>,
    ) {
        var error = false
        viewModelScope.launch(Dispatchers.IO) {
            do {
                when (waitingState.value) {
                    is ApiResponse.DataSuccess -> {
                        loadData(outStatus) {
                            action.invoke(waitingState.value)
                        }
                        break
                    }

                    is ApiResponse.DataError -> {
                        loadData(waitingState) {
                            getData.invoke()
                        }
                        error = true
                    }

                    else -> {
                        delay(100)
                    }
                }
            } while (!error)
            if (waitingState.value is ApiResponse.DataError && error) {
                val errorData = (waitingState.value as ApiResponse.DataError<I>)
                canPay.value = true
                _messageState.value = "error ${errorData.code} ${errorData.msg.orEmpty()}"
                Timber.e("${errorData.code} ${errorData.msg.orEmpty()}")
            }
        }
    }

    private var amount = "0.0"

    fun pay(amount: String) {
        canPay.value = false
        this.amount = amount
        _cardDataState.value = payModel.value
        viewModelScope.launch {
//            val data = tokenState.value
//            if (data is ApiResponse.DataSuccess) {
//                putAuth(data.body.token, amount)
//            } else {
//
//            }
            _response.value = ""
            waitingSussesAndLoadData(
                tokenState,
                { getToken() },
                authState,
                {
                    putAuth(it, amount)
                })
        }
    }



    private suspend fun putAuth(response: ApiResponse<TokenResponse>, amount: String) =
        repository.putAuth(
            AuthRequest(
                (response as ApiResponse.DataSuccess).body.token,
                cardDataState.value.expiry,
                amount,
                cardDataState.value.name
            )
        )

    private suspend fun getToken() =
        repository.getToken(AccountRequest(cardDataState.value.cardNumber))

    private suspend fun capture(response: ApiResponse<AuthResponse>): ApiResponse<CaptureResponse> {
        val body = (response as ApiResponse.DataSuccess).body
        return repository.putCapture(CaptureRequest(amount, body.merchid, body.retref))
    }

    fun updateCardData(data: PayModel) {
        payModel.value = data
    }


    init {
        connect.setSwipeListener(_cardDataState)
        var error = false
        _cardDataState.onEach {
            error = false
            Timber.e(it.toString())
            _canListener.value = true
            updateCardData(it)
            if (it.cardNumber.isNotEmpty()) {
                loadData(tokenState) { getToken() }
            }
        }.launchIn(viewModelScope)

        authState.onEach {
            Timber.e(it.toString())
            if (it is ApiResponse.DataSuccess) {
                waitingSussesAndLoadData(
                    authState,
                    { putAuth(tokenState.value, amount) },
                    captureState,
                    { capture ->
                        capture(capture)
                    })
                _response.value += "\n\nauth: ${it.body}"
            } else if ((it is ApiResponse.DataError) && !error) {
                loadData(authState) {
                    putAuth(tokenState.value, amount)
                }
                error = true
                _response.value += "\n" +
                        "\nauth error"
            } else if (it is ApiResponse.DataLoading) {
                _messageState.value = "authing"
            }
        }.launchIn(viewModelScope)
        tokenState.onEach {
            Timber.e(it.toString())
            when (it) {
                is ApiResponse.DataLoading -> {
                    _messageState.value = "tokenizing"
                }

                is ApiResponse.DataError -> {
                    _messageState.value = "error token ${it.code} ${it.msg.orEmpty()}"
                    Timber.e("error token ${it.code} ${it.msg.orEmpty()}")
                    _response.value += "\n" +
                            "\ntoken error"
                }

                is ApiResponse.DataSuccess -> {
                    _response.value += "\n" +
                            "\ntoken: ${it.body}"
                }

                else -> {}
            }
        }.launchIn(viewModelScope)
        captureState.onEach {
            Timber.e(it.toString())
            if (it is ApiResponse.DataSuccess) {
                _messageState.value = "susses ${it.body.amount}"
                canPay.value = true
                _response.value += "\n" +
                        "\nsusses: ${it.body}"
            } else if (it is ApiResponse.DataLoading) {
                _messageState.value = "capturing"
            }
        }.launchIn(viewModelScope)

    }
}