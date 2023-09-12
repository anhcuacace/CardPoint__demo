package tunanh.test_app.pre

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.dbconnection.dblibrarybeta.ProfileManager
import com.dbconnection.dblibrarybeta.RESTResponse
import com.idtechproducts.device.*
import com.idtechproducts.device.IDT_Device.context
import com.idtechproducts.device.ReaderInfo.DEVICE_TYPE
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateToolMsg
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import tunanh.test_app.DataResponse
import tunanh.test_app.LoadingStatus
import tunanh.test_app.PreferenceStore.Companion.DEVICE
import tunanh.test_app.extention.ifNullOrEmpty
import tunanh.test_app.getPreferenceValue
import tunanh.test_app.pay.PayModel
import tunanh.test_app.setPreference


class ConnectIdTech private constructor() : OnReceiverListener, OnReceiverListenerPINRequest,
    FirmwareUpdateToolMsg, RESTResponse {
    companion object {
        private var instance: ConnectIdTech? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: ConnectIdTech()
                .also { instance = it }
        }

        private const val emvTimeout = 90

        private const val TimeOutConnect = 120
    }

    private val _timeWaiting = MutableStateFlow(-1)

    val timeWaiting: StateFlow<Int> = _timeWaiting

//    private val _autoConnect = MutableStateFlow<DataResponse<Boolean>>(DataResponse.DataIdle())
//    val autoConnect: StateFlow<DataResponse<Boolean>> = _autoConnect

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private var device: IDT_VP3300? = null
    private var fwTool: FirmwareUpdateTool? = null
    private var profileManager: ProfileManager = ProfileManager(this)
    private val _availableConnect = MutableStateFlow<DataResponse<Boolean>>(DataResponse.DataIdle())
    val availableConnect: StateFlow<DataResponse<Boolean>> = _availableConnect

    val disconnectState = MutableStateFlow(false)


    private var _cardDataState: MutableStateFlow<PayModel>? = null

//    private val listDevice: suspend (Context) -> List<String> = {
//        val list = arrayListOf<String>()
//        it.getPreferenceValue(PreferenceStore.DEVICE_1).let { d ->
//            if (d.isNotEmpty()) {
//                list.add(d)
//            }
//        }
//        it.getPreferenceValue(PreferenceStore.DEVICE_2).let { d ->
//            if (d.isNotEmpty()) {
//                list.add(d)
//            }
//        }
//        it.getPreferenceValue(PreferenceStore.DEVICE_3).let { d ->
//            if (d.isNotEmpty()) {
//                list.add(d)
//            }
//        }
//        list
//    }

    fun initState() {
        _availableConnect.value = DataResponse.DataIdle()
    }

    @Synchronized
    fun autoConnect(context: Context) {
        if (_availableConnect.value !is DataResponse.DataLoading) {
            Timber.e("auto Connect")
            _availableConnect.value = DataResponse.DataLoading()
            CoroutineScope(Dispatchers.IO).launch {
                val bDevice: String = context.getPreferenceValue(DEVICE)
                if (bDevice.isEmpty()) {
                    _availableConnect.value = DataResponse.DataIdle()
                    return@launch
                }
                if (device == null) {
                    withContext(Dispatchers.Main) {
                        initializeReader(context)
                    }
                }
                device!!.setIDT_Device(fwTool)


                delay(1000)
                if ((_availableConnect.value !is DataResponse.DataSuccess) || _availableConnect.value.loadingStatus == LoadingStatus.Error) {
                    if (device!!.device_connect()) {
                        if (device!!.device_disconnectBLE()) {
                            _availableConnect.value = DataResponse.DataError("disconnected")
                            return@launch
                        }
                    }
                }
                connectDevice(context, bDevice)

//                listDevice(context).forEach {
//                    if ((_autoConnect.value !is DataResponse.DataSuccess) || _autoConnect.value.loadingStatus == LoadingStatus.Error) {
//                        if (device!!.device_connect()) {
//                            if (device!!.device_disconnectBLE()) {
//                                _autoConnect.value = DataResponse.DataError("disconnected")
//                                return@launch
//                            }
//                        }
//                        val rc = device!!.device_enableBLESearch(
//                            device!!.device_getDeviceType(),
//                            it,
//                            15000
//                        )
//                        if (rc == 0) {
//                            delay(120000)
//                        } else {
//                            _autoConnect.value = DataResponse.DataError(null)
//                            return@launch
//                        }
//                    }
//                }

            }
        }
    }

    private fun connectDevice(context: Context, bDevice: String, connected: suspend () -> Unit = {}) {

        val rc = device!!.device_enableBLESearch(
            device!!.device_getDeviceType(),
            bDevice,
            80000
        )
        Timber.e(rc.toString())
        if (rc == 0) {

            _timeWaiting.value = TimeOutConnect
            CoroutineScope(Dispatchers.Main).launch {

                countDownTimer = object : CountDownTimer(TimeOutConnect * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        _timeWaiting.value = (millisUntilFinished / 1000).toInt()
                        Timber.e("countdown $millisUntilFinished")
                    }

                    override fun onFinish() {
                        if (_availableConnect.value is DataResponse.DataLoading) {
                            timeoutConnect()
                        }
                    }

                }
                _availableConnect.onEach {
                    if (it is DataResponse.DataSuccess || it is DataResponse.DataError<*, *>) {
                        if (it is DataResponse.DataSuccess) {

                            connected()
                        }
                        countDownTimer?.cancel()
                    }
                }.launchIn(this)
                countDownTimer?.start()
            }

        } else {
            _availableConnect.value = DataResponse.DataError(rc)
            when (rc) {
                1 -> Toast.makeText(context, "Invalid DEVICE_TYPE", Toast.LENGTH_SHORT)
                    .show()

                2 -> Toast.makeText(
                    context,
                    "Bluetooth LE is not supported on this device",
                    Toast.LENGTH_SHORT
                ).show()

                3 -> Toast.makeText(
                    context,
                    "Bluetooth LE is not available",
                    Toast.LENGTH_SHORT
                ).show()

                4 -> Toast.makeText(
                    context,
                    "Bluetooth LE is not enabled",
                    Toast.LENGTH_SHORT
                ).show()

                5 -> {
                    Toast.makeText(
                        context,
                        "Device not paired. Please pair first",
                        Toast.LENGTH_SHORT
                    ).show()
                    unregisterListen()
                    releaseSDK()
                    device = null
                    IDT_Device.context = null
                }
            }


        }

    }

//    fun connectUsb(context: Context) {
//        _availableConnect.value = false
//        Timber.e("connectUsb")
//        if (device == null) {
//            initializeReader(context)
//        }
//        if (device!!.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT_USB))
//            Toast.makeText(
//                context,
//                "VP3300 Bluetooth (USB) is selected",
//                Toast.LENGTH_SHORT
//            ).show()
//        else Toast.makeText(
//            context,
//            "Failed. Please disconnect first.",
//            Toast.LENGTH_SHORT
//        ).show()
//        device!!.setIDT_Device(fwTool)
//        device!!.registerListen()
//    }

    fun isConnected() = device?.device_isConnected() ?: false


    fun setSwipeListener(cardDataState: MutableStateFlow<PayModel>) {
        _cardDataState = cardDataState
    }


    //    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
//        super.onMtuChanged(gatt, mtu, status)
//        if (status== BluetoothAdapter.STATE_CONNECTED && !isConnected){
//            isConnected=true
//            _availableConnect.value = DataResponse.DataSuccess(true)
//        }
//    }
    private var countDownTimer: CountDownTimer? = null

    @SuppressLint("MissingPermission")
    fun connectBlueTooth(bDevice: String, context: Context) {
        if (availableConnect.value !is DataResponse.DataLoading) {
            _availableConnect.value = DataResponse.DataLoading()
            if (device == null) {
                initializeReader(context)
            }
            device!!.setIDT_Device(fwTool)

            connectDevice(context, bDevice) {
                context.setPreference(DEVICE, bDevice)
            }
        }

//        timeoutConnect(dialogState, context)

//        CoroutineScope(Dispatchers.Main).launch{
//            do {
//                delay(500)
//                if (isConnected()){
//                    activity.startActivity(Intent(activity,PayActivity::class.java))
//                    dialogState.close()
//                }
//            }
//            while (!dialogState.openState || isConnected())
//        }
    }

    fun disconnect() {
        if (device != null) {
            device!!.device_disconnectBLE()
        }
    }

    private fun timeoutConnect() {
        _availableConnect.value =
            DataResponse.DataError("Connection time has expired. If you have tried multiple times, you should check the connection again, try turning airplane mode on and off, closing the app and reopening it.")
        unregisterListen()
        releaseSDK()
        device = null
    }

    fun setTypeConnectBluetooth(context: Context): Boolean {
        if (device == null) {
            initializeReader(context)
        }
        return device!!.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT)
    }

    fun initializeReader(context: Context) {
        if (device != null) {
            releaseSDK()
        }
        device = IDT_VP3300(this, this, context)
        profileManager = ProfileManager(this)
        fwTool = FirmwareUpdateTool(this, context)
        profileManager.doGet()
        Toast.makeText(context, "initializeReader", Toast.LENGTH_LONG).show()
        device!!.log_setVerboseLoggingEnable(true)

        device!!.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT)
    }

    private var canListener = MutableStateFlow(true)
    fun listenerTransactionIdTech(context: Context, canListener: MutableStateFlow<Boolean>) {
        this.canListener = canListener
        _message.value = ""
        if (device == null) {
            initializeReader(context)
        }
        if (canListener.value) {
            canListener.value = false
            timesHandle = 0
            val ret = device!!.device_startTransaction(
                1.00,
                0.00,
                0,
                emvTimeout,
                null,
                false
            )
            when (ret) {
                ErrorCode.SUCCESS -> {
                    _message.value = "Please insert a card"

                }

                ErrorCode.RETURN_CODE_OK_NEXT_COMMAND -> {
                    _message.value = "Start EMV transaction\n"
                }

                else -> {
                    _message.value = "Cannot insert card\n"
                    _message.value += "Status: " + device!!.device_getResponseCodeString(ret) + ""
                    this.canListener.value = true
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (!this.canListener.value) {
                    this.canListener.value = true
                }
            }, emvTimeout * 1000L)

        }
    }

    fun listenerSwipeIDTech(context: Context, canListener: MutableStateFlow<Boolean>) {
        this.canListener = canListener
        _message.value = ""
        if (device == null) {
            initializeReader(context)
        }
        if (canListener.value) {
            canListener.value = false
            timesHandle = 0
            when (val ret = device!!.msr_startMSRSwipe()) {
                ErrorCode.SUCCESS -> {
                    _message.value = "Please swipe/tap a card"
                }

                ErrorCode.RETURN_CODE_OK_NEXT_COMMAND -> {
                    _message.value = "Start EMV transaction\n"
                }

                else -> {
                    _message.value = "Cannot swipe card\n"
                    _message.value += "Status: " + device!!.device_getResponseCodeString(ret) + ""
                    this.canListener.value = true
                }
            }
        }
    }

    fun unregisterListen() {
        device?.unregisterListen()
        countDownTimer?.cancel()
    }

    private fun releaseSDK() {
        if (device != null) {
            if (device!!.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_COM) device!!.unregisterListen()
            device!!.release()
//            				device = null
            context = null
        }
    }

    override fun swipeMSRData(p0: IDTMSRData?) {
        Timber.e("swipeMSRData")
        p0?.apply {
            handleMSRData()
            return
        }
        _cardDataState?.value = PayModel("", "", "")
    }

    private var timesHandle = 0
    private fun IDTMSRData.handleMSRData() {
        Timber.tag("handleMSRData").e(track1)
        Timber.tag("handleMSRData").e(track2)
        Timber.tag("handleMSRData").e(track3)
        timesHandle++
        if (timesHandle > 1) {
            return
        }
        val cardNumber =
            unencryptedTags?.let { Common.getHexStringFromBytes(it["5A"] ?: byteArrayOf()) }
                .ifNullOrEmpty {
                    var output = ""
                    if (track2?.indexOf('=') != -1) {
                        track2?.substring(0, track2.indexOf('='))?.let {

                            for (element in it) {
                                val c: Char = element
                                if (Character.isDigit(c)) {
                                    output += c
                                }
                            }

                        }
                    }
                    output
                }.ifNullOrEmpty {
                    track1?.substring(0, track1.indexOf('^'))?.let {
                        var output = ""
                        for (element in it) {
                            val c: Char = element
                            if (Character.isDigit(c)) {
                                output += c
                            }
                        }
                        output
                    } ?: ""
                }


        val expirationDate: String =
            unencryptedTags?.let {
                if (it.containsKey("5F24"))
                    Common.getHexStringFromBytes(it["5F24"] ?: byteArrayOf())?.let { exp ->
                        if (exp.length >= 4) {
                            val month = exp.substring(2, 4)
                            val year = exp.substring(0, 2)
                            "$month$year"
                        } else ""
                    }
                else ""
            }.ifNullOrEmpty {

                val expirationDatePart = track2?.substring(
                    track2.indexOf('=') + 1,
                    track2.indexOf('=') + 5
                ) ?: ""
                if (expirationDatePart.length >= 4) {
                    val month = expirationDatePart.substring(2, 4)
                    val year = expirationDatePart.substring(0, 2)
                    "$month$year"
                } else ""
            }
        Timber.tag("tunanh").e(Common.getHexStringFromBytes(cardData))
        val name = track1?.substring(track1.indexOf("^") + 1, track1.lastIndexOf("^"))?.trim()
            .ifNullOrEmpty {
                cardData?.let {
                    CardData(it)
                    extractCardholderName(it)
                } ?: "null"
            }
        _cardDataState?.value = PayModel(cardNumber, expirationDate, name)
    }

    private fun extractCardholderName(byteArray: ByteArray): String? {

//        val nameRegex = Regex("\\^([A-Z\\s]+)\\s*\\^|=([A-Z\\s]+)\\s*\\?")
//
//        Timber.tag("tun_anh").e(String(byteArray, Charsets.ISO_8859_1))
//        val nameMatches = nameRegex.find(String(byteArray, Charsets.ISO_8859_1))
//        return nameMatches?.groupValues?.lastOrNull { it.isNotBlank() }
        return hexToAscii(Common.getHexStringFromBytes(byteArray)).split("[^a-zA-Z ]".toRegex())
            .filter { it.isNotEmpty() }
            .find { it.trim().split(" ").size >= 2 }?.trim()
    }


//    private fun getCardholderName(hexString: String): String {
//        val asciiString = hexStringToAscii(hexString)
//        return extractCardholderName(asciiString)
//    }

    private fun hexToAscii(hex: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < hex.length) {
            val hexPair = hex.substring(i, i + 2)
            try {
                val decimal = hexPair.toInt(16)
                if (decimal in 32..126) {
                    sb.append(decimal.toChar())
                } else {
                    sb.append('�')
                }
            } catch (e: NumberFormatException) {
                sb.append('�')
            }
            i += 2
        }
        return sb.toString()
    }

    override fun lcdDisplay(p0: Int, p1: Array<out String>?, p2: Int) {
        Timber.e("lcdDisplay")
    }

    override fun lcdDisplay(p0: Int, p1: Array<out String>?, p2: Int, p3: ByteArray?, p4: Byte) {
        Timber.e("lcdDisplay")
        p3?.let { Timber.e(Common.getHexStringFromBytes(it)) }

        p1?.let {
            Timber.e(it.run {
                var s = ""
                it.forEach { string ->
                    s += string.plus("\n")
                }
                s
            })
        }
    }

    override fun ctlsEvent(p0: Byte, p1: Byte, p2: Byte) {
        Timber.e("ctlsEvent")
    }

    override fun emvTransactionData(p0: IDTEMVData?) {
        Timber.e("emvTransactionData")

        p0?.apply {
            if (msr_cardData != null) {
                msr_cardData.handleMSRData()
            } else {
                _cardDataState?.value = PayModel("", "", "")
            }

            if (unencryptedTags?.containsKey("57") == true) {
                val data = unencryptedTags["57"] ?: ByteArray(0)
                Timber.e(Common.getHexStringFromBytes(data))
            }
            if (result == IDTEMVData.GO_ONLINE && !IDT_VP3300.emv_getAutoCompleteTransaction()) {
                val resData = ResDataStruct()
                device?.emv_cancelTransaction(resData)
            }
            return
        }
        _cardDataState?.value = PayModel("", "", "")
    }

    //		private boolean btleDeviceRegistered = false;
    //		private String btleDeviceAddress = "00:1C:97:14:FD:34";
    private val tag8A = byteArrayOf(0x30, 0x30)
    private fun completeTransaction(resData: ResDataStruct?): Int {
//			byte[] authResponseCode = new byte[]{(byte)0x30, 0x30};
        val authResponseCode = ByteArray(2)
        System.arraycopy(tag8A, 0, authResponseCode, 0, 2)
        val issuerAuthData = byteArrayOf(
            0x11.toByte(),
            0x22,
            0x33,
            0x44,
            0x55,
            0x66,
            0x77,
            0x88.toByte(),
            0x30,
            0x30
        )
        val tlvScripts: ByteArray? = null
        val value: ByteArray? = null
        return device!!.emv_completeTransaction(
            false,
            authResponseCode,
            issuerAuthData,
            tlvScripts,
            value
        )
    }


    override fun deviceConnected() {
        _availableConnect.value = DataResponse.DataSuccess(true)
        Timber.e("connected")
    }

    override fun deviceDisconnected() {
        Timber.e("deviceDisconnected")
//        if (device != null && context != null && device?.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_BT) {
////            autoConnect(context)
//        } else {
        disconnectState.value = true
//        }
    }

    override fun timeout(p0: Int) {
//        if(p0==ErrorCode.RETURN_CODE_NEO_CDCVM|| p0==ErrorCode.RETURN_CODE_NEO_POWER_OFF){
        _message.value = ErrorCodeInfo.getErrorCodeDescription(p0)
//        }
        if (!this.canListener.value) {
            this.canListener.value = true
        }
        Timber.e("timeout")
    }

    override fun autoConfigCompleted(p0: StructConfigParameters?) {
        Timber.e("autoConfigCompleted")
        profileManager.doPost(p0)
    }

    override fun autoConfigProgress(p0: Int) {
        Timber.e("autoConfigProgress")
    }

    override fun msgRKICompleted(p0: String?) {
        Timber.e("msgRKICompleted")
    }

    override fun ICCNotifyInfo(p0: ByteArray?, p1: String?) {
        Timber.e("ICCNotifyInfo")
    }

    override fun msgBatteryLow() {
        Timber.e("msgBatteryLow")
    }

    override fun LoadXMLConfigFailureInfo(p0: Int, p1: String?) {
        Timber.e("LoadXMLConfigFailureInfo")
    }

    override fun msgToConnectDevice() {
        Timber.e("msgToConnectDevice")
    }

    override fun msgAudioVolumeAjustFailed() {
        Timber.e("msgAudioVolumeAjustFailed")
    }

    override fun dataInOutMonitor(p0: ByteArray?, p1: Boolean) {
        Timber.e("dataInOutMonitor: " + Common.getHexStringFromBytes(p0))
    }

    override fun pinRequest(
        p0: Int,
        p1: ByteArray?,
        p2: ByteArray?,
        p3: Int,
        p4: Int,
        p5: String?,
    ) {
        Timber.e("pinRequest")
    }

    override fun onReceiveMsgUpdateFirmwareProgress(p0: Int) {
        Timber.e("onReceiveMsgUpdateFirmwareProgress")
    }

    override fun onReceiveMsgUpdateFirmwareResult(p0: Int) {
        Timber.e("onReceiveMsgUpdateFirmwareResult")
    }

    override fun onReceiveMsgChallengeResult(p0: Int, p1: ByteArray?) {
        Timber.e("onReceiveMsgChallengeResult")
    }

    override fun getProfileResult(p0: String?) {
        Timber.e("getProfileResult")
    }

    override fun postProfileResult(p0: String?) {
        Timber.e("postProfileResult")
    }
}