package tunanh.test_app.pre

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tunanh.test_app.R
import tunanh.test_app.bluetooth.BluetoothActivity
import tunanh.test_app.pay.PayActivity
import tunanh.test_app.ui.theme.Test_appTheme

class PreActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
                BackHandler {

                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val connect = ConnectIdTech.getInstance()

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = { Text(text = stringResource(id = R.string.select_type_connection)) },
            actions = {
                IconButton(onClick = { (context as PreActivity).finishAfterTransition() }) {
                    Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "exit")
                }
            })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (connect.setTypeConnectBluetooth(context)) {
                    if (context is PreActivity) {
                        context.apply {
                            startActivity(Intent(this, BluetoothActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Failed. Please disconnect first.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }) {
                Text(text = stringResource(R.string.bluetooth))
            }
            Button(onClick = {
                connect.connectUsb(context.applicationContext)
            }) {
                Text(text = stringResource(R.string.usb))
            }
        }
        connect.initializeReader(context.applicationContext)
        if (context is PreActivity) {
            context.lifecycleScope.launch {
                connect.availableConnect.collect {
                    if (it) {
                        Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, PayActivity::class.java))
                        context.finish()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Test_appTheme {
        Greeting(modifier = Modifier.fillMaxSize())
    }
}