package tunanh.test_app.pre

import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.dbconnection.dblibrarybeta.ProfileManager
import com.dbconnection.dblibrarybeta.RESTResponse
import com.idtechproducts.device.*
import com.idtechproducts.device.IDT_Device.context
import com.idtechproducts.device.ReaderInfo.DEVICE_TYPE
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateToolMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import tunanh.test_app.*
import tunanh.test_app.pay.PayModel
import tunanh.test_app.ui.DialogState


class ConnectIdTech private constructor() : OnReceiverListener, OnReceiverListenerPINRequest,
    FirmwareUpdateToolMsg, RESTResponse {
    companion object {
        private var instance: ConnectIdTech? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: ConnectIdTech()
                .also { instance = it }
        }

        private const val emvTimeout = 90
    }

    private val _autoConnect = MutableStateFlow<DataResponse<Boolean>>(DataResponse.DataIdle())
    val autoConnect: StateFlow<DataResponse<Boolean>> = _autoConnect

    private var device: IDT_VP3300? = null
    private var fwTool: FirmwareUpdateTool? = null
    private val profileManager: ProfileManager = ProfileManager(this)
    private val _availableConnect = MutableStateFlow(false)
    val availableConnect: StateFlow<Boolean> = _availableConnect

    val disconnectState = MutableStateFlow(false)

    private val _connectBlueToothState = MutableStateFlow(-1)
    val connectBlueToothState: StateFlow<Int> = _connectBlueToothState

    private var _cardDataState: MutableStateFlow<PayModel>? = null

    private val listDevice: suspend (Context) -> List<String> = {
        val list = arrayListOf<String>()
        it.getPreferenceValue(PreferenceStore.DEVICE_1)?.let { d ->
            if (d.isNotEmpty()) {
                list.add(d)
            }
        }
        it.getPreferenceValue(PreferenceStore.DEVICE_2)?.let { d ->
            if (d.isNotEmpty()) {
                list.add(d)
            }
        }
        it.getPreferenceValue(PreferenceStore.DEVICE_3)?.let { d ->
            if (d.isNotEmpty()) {
                list.add(d)
            }
        }
        list
    }

    fun initState() {
        _autoConnect.value = DataResponse.DataIdle()
    }

    fun autoConnect(context: Context) {
        if (_autoConnect.value !is DataResponse.DataLoading) {
            _autoConnect.value = DataResponse.DataLoading()
            CoroutineScope(Dispatchers.IO).launch {
                if (device == null) {
                    initializeReader(context)
                }
                device!!.setIDT_Device(fwTool)

                listDevice(context).forEach {
                    if ((_autoConnect.value !is DataResponse.DataSuccess) || _autoConnect.value.loadingStatus == LoadingStatus.Error) {
                        val rc = device!!.device_enableBLESearch(
                            device!!.device_getDeviceType(),
                            it,
                            8000
                        )
                        if (rc == 0) {
                            delay(10000)
                        } else {
                            _autoConnect.value = DataResponse.DataError(null)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    fun connectUsb(context: Context) {
        _availableConnect.value = false
        Timber.e("connectUsb")
        if (device == null) {
            initializeReader(context)
        }
        if (device!!.device_setDeviceType(DEVICE_TYPE.DEVICE_VP3300_BT_USB))
            Toast.makeText(
            context,
            "VP3300 Bluetooth (USB) is selected",
            Toast.LENGTH_SHORT
        ).show()
        else Toast.makeText(
            context,
            "Failed. Please disconnect first.",
            Toast.LENGTH_SHORT
        ).show()
        device!!.setIDT_Device(fwTool)
        device!!.registerListen()
    }

    fun isConnected() = device?.device_isConnected() ?: false


    fun setSwipeListener(cardDataState: MutableStateFlow<PayModel>) {
        _cardDataState = cardDataState
    }

    fun connectBlueTooth(address: String, context: Context, dialogState: DialogState) {
        _availableConnect.value = false
        timeoutConnect(dialogState, context)
        if (device == null) {
            initializeReader(context)
        }
        device!!.setIDT_Device(fwTool)
        val rc = device!!.device_enableBLESearch(
            device!!.device_getDeviceType(),
            address,
            8000
        )
        Timber.e(rc.toString())
        _connectBlueToothState.value = rc
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

    private fun timeoutConnect(dialogState: DialogState, context: Context) {
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (dialogState.openState && device?.device_isConnected() == false) {
                dialogState.close()
                Toast.makeText(context, "An error occurred, please try again", Toast.LENGTH_LONG).show()
            }
        }, 12000)
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
        profileManager.doGet()
        Toast.makeText(context, "get started", Toast.LENGTH_LONG).show()
        device!!.log_setVerboseLoggingEnable(true)
        fwTool = FirmwareUpdateTool(this, context)
    }

    private var canListener = MutableStateFlow(true)
    fun listenerIdTech(context: Context, _canListener: MutableStateFlow<Boolean>) {
        this.canListener = _canListener
        if (device == null) {
            initializeReader(context)
        }
        if (canListener.value) {
            device!!.device_startTransaction(
                1.00,
                0.00,
                0,
                emvTimeout,
                null,
                false
            )
            canListener.value = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                canListener.value = true
            }, emvTimeout * 1000L)

        }
    }

    private fun releaseSDK() {
        if (device != null) {
            if (device!!.device_getDeviceType() != DEVICE_TYPE.DEVICE_VP3300_COM) device!!.unregisterListen()
            device!!.release()
            //				device = null;
        }
    }

    override fun swipeMSRData(p0: IDTMSRData?) {
        Timber.e("swipeMSRData")
        p0?.apply {
            val cardNumber = track2?.substring(0, track2.indexOf('='))?.let {
                var output = ""
                for (element in it) {
                    val c: Char = element
                    if (Character.isDigit(c)) {
                        output += c
                    }
                }
                output
            } ?: ""
            val expirationDatePart = track2?.substring(
                track2.indexOf('=') + 1,
                track2.indexOf('=') + 5
            ) ?: ""
            val expirationDate: String = if (expirationDatePart.length >= 4) {
                val month = expirationDatePart.substring(2, 4)
                val year = expirationDatePart.substring(0, 2)
                "$month$year"
            } else ""
            Timber.e(track1)
            val name =
                track1?.substring(track1.indexOf("^") + 1, track1.lastIndexOf("^"))?.trim() ?: ""
            _cardDataState?.value = PayModel(cardNumber, expirationDate, name)
        }
    }

    override fun lcdDisplay(p0: Int, p1: Array<out String>?, p2: Int) {
        Timber.e("lcdDisplay")
    }

    override fun lcdDisplay(p0: Int, p1: Array<out String>?, p2: Int, p3: ByteArray?, p4: Byte) {
        Timber.e("lcdDisplay")
    }

    override fun ctlsEvent(p0: Byte, p1: Byte, p2: Byte) {
        Timber.e("ctlsEvent")
    }

    override fun emvTransactionData(p0: IDTEMVData?) {
        Timber.e("emvTransactionData")
        p0?.apply {
            val data = unencryptedTags["57"] ?: ByteArray(0)
            Timber.e(Common.getHexStringFromBytes(data))
        }
    }

    override fun deviceConnected() {

        if (_autoConnect.value is DataResponse.DataLoading) {
            _autoConnect.value = DataResponse.DataSuccess(true)
        } else {
            _availableConnect.value = true
        }
        Timber.e("connected")
    }

    override fun deviceDisconnected() {
        Timber.e("deviceDisconnected")
        if (device != null && context != null && device?.device_getDeviceType() == DEVICE_TYPE.DEVICE_VP3300_BT) {
            autoConnect(context)
        } else {
            disconnectState.value = true
        }
    }

    override fun timeout(p0: Int) {
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
        Timber.e("dataInOutMonitor $p0")
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

    }

    override fun postProfileResult(p0: String?) {

    }
}