package tunanh.test_app

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tunanh.test_app.bluetooth.BluetoothController

class DevicesViewModel : ViewModel() {
    var foundDevices = mutableStateListOf<BluetoothDevice>()
    var pairedDevices = mutableStateListOf<BluetoothDevice>()

    var isScanning by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)

    var isBluetoothEnabled by mutableStateOf(false)

    fun refresh(controller: BluetoothController) {
        viewModelScope.launch {
            isRefreshing = true
            pairedDevices.clear()
            pairedDevices.addAll(controller.pairedDevices)
            if (!isScanning) {
                controller.scanDevices()
            }
            delay(500)
            isRefreshing = false
        }
    }
}
