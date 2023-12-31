package tunanh.test_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import tunanh.test_app.R
import tunanh.test_app.ui.theme.Typography

class DialogState(initialOpen: Boolean = false) {
    var openState by mutableStateOf(initialOpen)

    fun open() {
        openState = true
    }

    fun close() {
        openState = false
    }
}

@Composable
fun rememberDialogState(initialOpen: Boolean = false) = remember {
    DialogState(initialOpen)
}

//@Composable
//fun TextBoxDialog(
//    dialogState: DialogState,
//    title: String,
//    text: String,
//    description: String? = null,
//    onReset: () -> Unit,
//    onDismiss: () -> Unit = {},
//    onConfirm: (String) -> Unit
//) {
//    var currentText by remember(text) { mutableStateOf(text) }
//
//    ConfirmResetDialog(dialogState, title, onConfirm = {
//        close()
//        onConfirm(currentText)
//    }, onDismiss = {
//        close()
//        currentText = text
//        onDismiss()
//    }, onReset = onReset) {
//        Column {
//            description?.let {
//                Text(it)
//                Spacer(Modifier.height(16.dp))
//            }
//            TextField(
//                value = currentText,
//                onValueChange = { currentText = it },
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//fun CheckBoxDialog(
//    dialogState: DialogState,
//    title: String,
//    selectedValues: Set<Int>,
//    valueStrings: Array<String>,
//    onReset: () -> Unit,
//    description: String? = null,
//    onDismiss: () -> Unit = {},
//    onConfirm: (List<Int>) -> Unit
//) {
//    var currentSelection = remember(selectedValues) {
//        selectedValues.toMutableStateList()
//    }
//
//    ConfirmResetDialog(dialogState, title, onConfirm = {
//        close()
//        onConfirm(currentSelection)
//    }, onDismiss = {
//        close()
//        currentSelection = selectedValues.toMutableStateList()
//        onDismiss()
//    }, onReset = onReset) {
//        LazyColumn {
//            description?.let {
//                item {
//                    Text(it)
//                    Spacer(Modifier.height(16.dp))
//                }
//            }
//            itemsIndexed(valueStrings) { index, item ->
//                val selected = currentSelection.contains(index)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .clickable {
//                            if (!selected) {
//                                currentSelection.add(index)
//                            } else {
//                                currentSelection.remove(index)
//                            }
//                        }
//                ) {
//                    Checkbox(
//                        checked = selected,
//                        onCheckedChange = {
//                            if (!selected) {
//                                currentSelection.add(index)
//                            } else {
//                                currentSelection.remove(index)
//                            }
//                        },
//                        modifier = Modifier.semantics {
//                            stateDescription =
//                                "$item is ${if (selected) "selected" else "not selected"}"
//                        }
//                    )
//                    Spacer(Modifier.width(8.dp))
//                    Text(item, modifier = Modifier.weight(1f))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ComboBoxDialog(
//    dialogState: DialogState,
//    title: String,
//    selectedItem: Int,
//    values: Array<String>,
//    onReset: () -> Unit,
//    description: String? = null,
//    onDismiss: () -> Unit = {},
//    onConfirm: (Int) -> Unit
//) {
//    var currentSelection by remember(selectedItem) {
//        mutableStateOf(selectedItem)
//    }
//
//    ConfirmResetDialog(dialogState, title, onConfirm = {
//        close()
//        onConfirm(currentSelection)
//    }, onDismiss = {
//        close()
//        currentSelection = selectedItem
//        onDismiss()
//    }, onReset = onReset) {
//        LazyColumn {
//            description?.let {
//                item {
//                    Text(it)
//                    Spacer(Modifier.height(16.dp))
//                }
//            }
//            itemsIndexed(values) { index, item ->
//                val selected = currentSelection == index
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .clickable {
//                            currentSelection = index
//                        }
//                ) {
//                    RadioButton(
//                        selected = selected,
//                        onClick = { currentSelection = index },
//                        modifier = Modifier.semantics {
//                            stateDescription =
//                                "$item is ${if (selected) "selected" else "not selected"}"
//                        }
//                    )
//                    Spacer(Modifier.width(8.dp))
//                    Text(item, modifier = Modifier.weight(1f))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SliderDialog(
//    dialogState: DialogState,
//    title: String,
//    valueFormat: String = "%f",
//    value: Float,
//    range: ClosedFloatingPointRange<Float>,
//    steps: Int = 0,
//    onReset: () -> Unit,
//    description: String? = null,
//    onDismiss: () -> Unit = {},
//    onValueChange: (Float) -> Unit
//) {
//    var sliderPosition by remember(value) {
//        mutableStateOf(value)
//    }
//
//    ConfirmResetDialog(dialogState, title, onConfirm = {
//        close()
//        onValueChange(sliderPosition)
//    }, onDismiss = {
//        close()
//        sliderPosition = value
//        onDismiss()
//    }, onReset = onReset) {
//        Column {
//            description?.let {
//                Text(it)
//                Spacer(Modifier.height(16.dp))
//            }
//
//            Text(valueFormat.format(sliderPosition))
//
//            Slider(
//                value = sliderPosition,
//                onValueChange = { sliderPosition = it },
//                valueRange = range,
//                steps = steps,
//                colors = SliderDefaults.colors(
//                    thumbColor = MaterialTheme.colorScheme.secondary,
//                    activeTrackColor = MaterialTheme.colorScheme.secondary
//                )
//            )
//        }
//    }
//}

@Composable
fun InfoDialog(
    dialogState: DialogState,
    title: String,
    onDismiss: DialogState.() -> Unit = { close() },
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) = with(dialogState) {
    if (openState) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            icon = icon,
            title = { Text(title) },
            text = { content() },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}


@Composable
fun ConfirmDialog(
    dialogState: DialogState,
    title: String,
    onDismiss: DialogState.() -> Unit = { close() },
    onConfirm: DialogState.() -> Unit,
    content: @Composable () -> Unit,
) = with(dialogState) {
    if (openState) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(title) },
            text = { content() },
            confirmButton = {
                TextButton(
                    onClick = { onConfirm() }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
fun LoadingDialog(
    dialogState: DialogState,
    title: String,
    desc: String,
    onDismiss: DialogState.() -> Unit = {}
) = with(dialogState) {
    if (openState) {
        Dialog(onDismissRequest = { onDismiss() }, DialogProperties(dismissOnBackPress = true)) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.0.dp
            ) {
                Column(
                    modifier = Modifier
                        .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(title, style = Typography.headlineMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(R.string.please_wait))
                    }
                    Text(
                        desc,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
