package tunanh.test_app.pay

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tunanh.test_app.DataResponse
import tunanh.test_app.DevicesViewModel
import tunanh.test_app.R
import tunanh.test_app.Utils
import tunanh.test_app.bluetooth.BluetoothDisabledCard
import tunanh.test_app.bluetooth.BroadcastListener
import tunanh.test_app.pre.ConnectIdTech
import tunanh.test_app.pre.PreActivity
import tunanh.test_app.ui.LoadingDialog
import tunanh.test_app.ui.rememberDialogState
import tunanh.test_app.ui.theme.Test_appTheme

class PayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConnectIdTech.getInstance().disconnectState.value = false
        setContent {
            Test_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting2(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting2(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dialogState = rememberDialogState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val deviceViewModel = DevicesViewModel()
    val bluetoothAdapter = if (Utils.isAndroidS()) {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    } else {
        BluetoothAdapter.getDefaultAdapter();
    }
    deviceViewModel.isBluetoothEnabled = bluetoothAdapter != null && bluetoothAdapter.isEnabled

    LoadingDialog(
        dialogState,
        stringResource(R.string.connecting),
        stringResource(R.string.connect_help)
    )
    deviceViewModel.BroadcastListener()

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            title = { Text(text = "Pay") },
            navigationIcon = {
                IconButton(onClick = {
                    if (context is PayActivity) {
                        ConnectIdTech.getInstance().disconnect()
                        context.finish()
                    }
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBackIos, contentDescription = null)
                }
            }, scrollBehavior = scrollBehavior
        )
    }) { padding ->
        with(viewModel<PayViewModel>()) {
            val cardData by cardDataState.collectAsState()
            val listenerEnabled by canListener.collectAsState()
            val payEnabled by canPay.collectAsState()
            val message by messageState.collectAsState()
            val response by response.collectAsState()
            var amount by remember { mutableStateOf("") }

            BackHandler {}
            rememberCoroutineScope().launch {
                disconnectState.onEach {
                    if (it) {
                        if (context is PayActivity) {
                            Toast.makeText(
                                context.applicationContext,
                                "lost connection need reconnect",
                                Toast.LENGTH_LONG
                            ).show()
                            context.startActivity(Intent(context,PreActivity::class.java))
                            context.finish()
                        }
                    }
                }
                autoConnectState.onEach {
                    when (it) {
                        is DataResponse.DataSuccess -> {
                            dialogState.close()
                        }

                        is DataResponse.DataLoading -> {
                            dialogState.open()
                        }

                        is DataResponse.DataError<*, *> -> {
                            dialogState.close()
                            Toast.makeText(
                                context.applicationContext,
                                "can't seem to connect automatically, you should go back to previous screen to update mac",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {

                        }
                    }
                }
            }
            if (deviceViewModel.isBluetoothEnabled) {
                Column(
                    modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            listener(context.applicationContext)
                        }, enabled = listenerEnabled) {
                            Text(text = "listener swipe and transaction")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        if (!listenerEnabled) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Row {
                        Text(text = "card number:", style = TextStyle(fontSize = 24.sp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cardData.cardNumber,
                            style = TextStyle(fontSize = 24.sp)
                        )
                    }
                    Row {
                        Text(text = "expiry:", style = TextStyle(fontSize = 16.sp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cardData.expiry,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    }
                    Row {
                        Text(text = "name:", style = TextStyle(fontSize = 12.sp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cardData.name,
                            style = TextStyle(fontSize = 12.sp)
                        )
                    }

                    if (cardData.cardNumber.isNotEmpty() && cardData.expiry.isNotEmpty()) {
                        Row {
                            Text(text = "amount:")
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                value = amount,
                                placeholder = {
                                    Text(text = "0.00")
                                },
                                onValueChange = { newText ->
                                    amount = newText
                                    canPay.value = newText.isNotEmpty() && newText.toDouble() > 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        Button(onClick = {
                            pay(amount)
                        }, enabled = payEnabled) {
                            Text(text = "pay")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = message)
                        Text(text = response)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp, 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BluetoothDisabledCard()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    Test_appTheme {
        Greeting2(Modifier.fillMaxSize())
    }
}