package tool.xfy9326.milink.nfc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.Circulate
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.data.NfcActionIntentType
import tool.xfy9326.milink.nfc.ui.common.FunctionCard
import tool.xfy9326.milink.nfc.ui.common.IconTextButton
import tool.xfy9326.milink.nfc.ui.common.MacAddressTextField
import tool.xfy9326.milink.nfc.ui.common.SelectorTextField
import tool.xfy9326.milink.nfc.ui.dialog.NfcReadOnlyAlertDialog
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.CirculateViewModel


const val CirculateRoute = "circulate"

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        CirculateScreen(
            onRequestWriteNfc = {},
            onNavBack = {}
        )
    }
}

@Composable
fun CirculateScreen(
    viewModel: CirculateViewModel = viewModel(),
    onRequestWriteNfc: (NdefWriteData) -> Unit,
    onNavBack: () -> Unit
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.circulate_nfc)) },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.nav_back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .displayCutoutPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TestCirculateFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                circulate = uiState.value.testCirculate,
                onSendCirculate = { viewModel.sendCirculate(context, it) }
            )
            WriteNfcFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                nfcTagData = uiState.value.nfcTag,
                onRequestWriteNfc = { viewModel.requestWriteNfc(it, onRequestWriteNfc) }
            )
        }
    }
    EventHandler(
        snackBarHostState = snackBarHostState,
        viewModel = viewModel
    )
}

@Composable
private fun EventHandler(
    snackBarHostState: SnackbarHostState,
    viewModel: CirculateViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(snackBarHostState) {
        viewModel.instantMsg.collectLatest {
            snackBarHostState.showSnackbar(message = context.getString(it.resId))
        }
    }
}

@Composable
private fun TestCirculateFunctionCard(
    modifier: Modifier,
    circulate: Circulate,
    onSendCirculate: (Circulate) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var editCirculate by rememberSaveable { mutableStateOf(circulate) }

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.Devices,
        title = stringResource(id = R.string.test_circulate),
        description = stringResource(id = R.string.test_circulate_desc)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SelectorTextField(
                    modifier = Modifier.weight(0.4f),
                    label = stringResource(id = R.string.handoff_device_type),
                    selectKey = editCirculate.deviceType.name,
                    keyTextMap = Circulate.DeviceType.entries.associate { it.name to stringResource(id = it.resId) },
                    onKeySelected = {
                        editCirculate = editCirculate.copy(deviceType = Circulate.DeviceType.valueOf(it))
                    }
                )
                SelectorTextField(
                    modifier = Modifier.weight(0.6f),
                    label = stringResource(id = R.string.nfc_action_intent),
                    selectKey = editCirculate.actionIntentType.name,
                    keyTextMap = NfcActionIntentType.entries.associate { it.name to stringResource(id = it.resId) },
                    onKeySelected = {
                        editCirculate = editCirculate.copy(actionIntentType = NfcActionIntentType.valueOf(it))
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            MacAddressTextField(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.enter_wifi_mac_address),
                value = editCirculate.wifiMac,
                upperCase = true,
                onValueChange = {
                    editCirculate = editCirculate.copy(wifiMac = it)
                }
            )
            MacAddressTextField(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.enter_bluetooth_mac_address),
                value = editCirculate.bluetoothMac,
                upperCase = true,
                onValueChange = {
                    editCirculate = editCirculate.copy(bluetoothMac = it)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.send),
                icon = Icons.Default.Send,
                onClick = {
                    focusManager.clearFocus()
                    onSendCirculate(editCirculate.copy())
                }
            )
        }
    }
}

@Composable
private fun WriteNfcFunctionCard(
    modifier: Modifier,
    nfcTagData: CirculateViewModel.NFCTag,
    onRequestWriteNfc: (CirculateViewModel.NFCTag) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var readOnlyAlert by remember { mutableStateOf(false) }
    var editNfcTagData by rememberSaveable { mutableStateOf(nfcTagData) }

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.Nfc,
        title = stringResource(id = R.string.write_circulate_nfc),
        description = stringResource(id = R.string.write_circulate_nfc_desc)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SelectorTextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.handoff_device_type),
                selectKey = editNfcTagData.deviceType.name,
                keyTextMap = Circulate.DeviceType.entries.associate { it.name to stringResource(id = it.resId) },
                onKeySelected = {
                    editNfcTagData = editNfcTagData.copy(deviceType = Circulate.DeviceType.valueOf(it))
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            MacAddressTextField(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.enter_wifi_mac_address),
                value = editNfcTagData.wifiMac,
                upperCase = true,
                onValueChange = {
                    editNfcTagData = editNfcTagData.copy(wifiMac = it)
                }
            )
            MacAddressTextField(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.enter_bluetooth_mac_address),
                value = editNfcTagData.bluetoothMac,
                upperCase = true,
                onValueChange = {
                    editNfcTagData = editNfcTagData.copy(bluetoothMac = it)
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = editNfcTagData.readOnly,
                    onCheckedChange = {
                        if (it) {
                            readOnlyAlert = true
                        } else {
                            editNfcTagData = editNfcTagData.copy(readOnly = false)
                        }
                    }
                )
                Text(text = stringResource(id = R.string.set_nfc_read_only))
            }
            Spacer(modifier = Modifier.height(10.dp))
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.write_tag),
                icon = Icons.Default.Code,
                onClick = {
                    focusManager.clearFocus()
                    onRequestWriteNfc(editNfcTagData.copy())
                }
            )
        }
    }
    if (readOnlyAlert) {
        NfcReadOnlyAlertDialog(
            onConfirm = {
                editNfcTagData = editNfcTagData.copy(readOnly = true)
                readOnlyAlert = false
            },
            onDismissRequest = { readOnlyAlert = false }
        )
    }
}
