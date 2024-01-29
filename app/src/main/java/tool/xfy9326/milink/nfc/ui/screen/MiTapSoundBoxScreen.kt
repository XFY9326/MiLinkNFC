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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.ui.common.FunctionCard
import tool.xfy9326.milink.nfc.ui.common.IconTextButton
import tool.xfy9326.milink.nfc.ui.common.MacAddressTextField
import tool.xfy9326.milink.nfc.ui.dialog.NfcReadOnlyAlertDialog
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MiTapSoundBoxViewModel
import tool.xfy9326.milink.nfc.utils.EMPTY

const val MiTapSoundBoxRoute = "mi_tap_sound_box"

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        MiTapSoundBox(
            onRequestWriteNfc = {},
            onNavBack = {}
        )
    }
}

@Composable
fun MiTapSoundBox(
    viewModel: MiTapSoundBoxViewModel = viewModel(),
    onRequestWriteNfc: (NdefWriteData) -> Unit,
    onNavBack: () -> Unit
) {
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
                title = { Text(text = stringResource(id = R.string.mi_tap_sound_box_nfc)) },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.nav_back)
                        )
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
    viewModel: MiTapSoundBoxViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(snackBarHostState) {
        viewModel.instantMsg.collectLatest {
            snackBarHostState.showSnackbar(message = context.getString(it.resId))
        }
    }
}

@Composable
private fun WriteNfcFunctionCard(
    modifier: Modifier,
    nfcTagData: MiTapSoundBoxViewModel.NFCTag,
    onRequestWriteNfc: (MiTapSoundBoxViewModel.NFCTag) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    var readOnlyAlert by remember { mutableStateOf(false) }
    var editNfcTagData by rememberSaveable { mutableStateOf(nfcTagData) }

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.Nfc,
        title = stringResource(id = R.string.write_mi_tap_sound_box_nfc),
        description = stringResource(id = R.string.write_mi_tap_sound_box_nfc_desc)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.iot_model_name))
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.placeholder_iot_model_name),
                        color = Color.Gray
                    )
                },
                trailingIcon = if (editNfcTagData.model.isEmpty()) null else {
                    {
                        IconButton(onClick = {
                            editNfcTagData = editNfcTagData.copy(model = EMPTY)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.clear)
                            )
                        }
                    }
                },
                value = editNfcTagData.model,
                onValueChange = {
                    it.asSequence().filterNot { c ->
                        c.isWhitespace()
                    }.take(64).joinToString("").let { v ->
                        editNfcTagData = editNfcTagData.copy(model = v)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(18.dp))
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
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconTextButton(
                    text = stringResource(id = R.string.open_iot_spec),
                    icon = Icons.AutoMirrored.Default.OpenInNew,
                    onClick = {
                        focusManager.clearFocus()
                        uriHandler.openUri(context.getString(R.string.mi_iot_spec_url))
                    }
                )
                IconTextButton(
                    text = stringResource(id = R.string.write_tag),
                    icon = Icons.Default.Code,
                    onClick = {
                        focusManager.clearFocus()
                        onRequestWriteNfc(editNfcTagData.copy())
                    }
                )
            }
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
