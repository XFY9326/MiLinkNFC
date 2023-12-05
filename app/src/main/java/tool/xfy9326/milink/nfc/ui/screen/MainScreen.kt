package tool.xfy9326.milink.nfc.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.HuaweiRedirectData
import tool.xfy9326.milink.nfc.data.XiaomiDeviceType
import tool.xfy9326.milink.nfc.data.XiaomiMirrorData
import tool.xfy9326.milink.nfc.data.XiaomiNFCTagData
import tool.xfy9326.milink.nfc.service.NfcNotificationListenerService
import tool.xfy9326.milink.nfc.ui.common.FunctionCard
import tool.xfy9326.milink.nfc.ui.common.IconTextButton
import tool.xfy9326.milink.nfc.ui.common.MacAddressTextField
import tool.xfy9326.milink.nfc.ui.common.MiConnectActionSettings
import tool.xfy9326.milink.nfc.ui.common.MirrorDataController
import tool.xfy9326.milink.nfc.ui.common.SelectorTextField
import tool.xfy9326.milink.nfc.ui.dialog.AboutDialog
import tool.xfy9326.milink.nfc.ui.dialog.MiLinkVersionDialog
import tool.xfy9326.milink.nfc.ui.dialog.NdefWriterDialog
import tool.xfy9326.milink.nfc.ui.dialog.NotSupportedOSDialog
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.openAppSettings
import tool.xfy9326.milink.nfc.utils.openNotificationServiceSettings

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        MainScreen {}
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val contentScrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .displayCutoutPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TestScreenMirrorFunctionCard(
                mirrorData = uiState.value.defaultScreenMirrorData,
                onOpenMiLinkVersionDialog = viewModel::openMiLinkVersionDialog,
                onSendScreenMirror = { viewModel.sendScreenMirror(context, it) }
            )
            WriteNfcFunctionCard(
                nfcTagData = uiState.value.defaultNFCTagData,
                onRequestWriteNfc = viewModel::requestWriteNfc,
                onRequestClearNfc = viewModel::requestClearNfc
            )
            TilesFunctionCard(
                mirrorData = uiState.value.tilesMirrorData,
                onChanged = viewModel::updateTilesMirrorData,
                onRequestAddTiles = { viewModel.requestAddTiles(context) },
                onSave = viewModel::saveTilesMirrorData
            )
            HuaweiRedirectFunctionCard(
                redirectData = uiState.value.huaweiRedirectData,
                onChanged = viewModel::updateHuaweiRedirectData,
                onSave = viewModel::saveHuaweiRedirectData
            )
        }
    }
    EventHandler(
        snackBarHostState = snackBarHostState,
        viewModel = viewModel
    )
    uiState.value.ndefWriteDialogData?.let {
        NdefWriterDialog(
            ndefData = it,
            onOpenReader = viewModel::onOpenNFCReader,
            onCloseReader = viewModel::onCloseNfcReader,
            onDismissRequest = viewModel::cancelWriteNfc
        )
    }
    uiState.value.miLinkPackageDialogData?.let {
        MiLinkVersionDialog(
            dialogData = it,
            onDismissRequest = viewModel::closeMiLinkVersionDialog
        )
    }
    if (uiState.value.showNotSupportedOSDialog) {
        NotSupportedOSDialog(
            onConfirmed = viewModel::confirmNotSupportedOS,
            onExit = onExit
        )
    }
}

@Composable
private fun EventHandler(
    snackBarHostState: SnackbarHostState,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(snackBarHostState) {
        viewModel.snackbarMsg.collectLatest {
            snackBarHostState.showSnackbar(message = context.getString(it.resId))
        }
    }
}

@Composable
private fun TopBar() {
    var openAboutDialog by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                openAboutDialog = true
            }) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(id = R.string.about))
            }
        }
    )

    if (openAboutDialog) {
        AboutDialog {
            openAboutDialog = false
        }
    }
}

@Composable
private fun TestScreenMirrorFunctionCard(
    mirrorData: XiaomiMirrorData,
    onOpenMiLinkVersionDialog: () -> Unit,
    onSendScreenMirror: (XiaomiMirrorData) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var editMirrorData by rememberSaveable { mutableStateOf(mirrorData) }

    FunctionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        icon = Icons.Default.BluetoothSearching,
        iconDescription = stringResource(id = R.string.bt_screen_mirror),
        helpIcon = Icons.Default.HelpOutline,
        helpIconDescription = stringResource(id = R.string.local_app_versions),
        onClickHelpIcon = onOpenMiLinkVersionDialog,
        title = stringResource(id = R.string.bt_screen_mirror),
        description = stringResource(id = R.string.bt_screen_mirror_desc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MirrorDataController(
                modifier = Modifier.fillMaxWidth(),
                mirrorData = editMirrorData,
                onChanged = { editMirrorData = it },
            )
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.screen_mirror),
                icon = Icons.Default.Cast,
                onClick = {
                    focusManager.clearFocus()
                    onSendScreenMirror(editMirrorData.copy())
                }
            )
        }
    }
}

@Composable
private fun WriteNfcFunctionCard(
    nfcTagData: XiaomiNFCTagData,
    onRequestWriteNfc: (XiaomiNFCTagData) -> Unit,
    onRequestClearNfc: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var readOnlyAlert by remember { mutableStateOf(false) }
    var editNfcTagData by rememberSaveable { mutableStateOf(nfcTagData) }

    FunctionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        icon = Icons.Default.Nfc,
        iconDescription = stringResource(id = R.string.write_nfc),
        title = stringResource(id = R.string.write_nfc),
        description = stringResource(id = R.string.write_nfc_desc)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SelectorTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .padding(bottom = 10.dp),
                label = stringResource(id = R.string.nfc_xiaomi_device_type),
                selectKey = editNfcTagData.deviceType.name,
                keyTextMap = XiaomiDeviceType.entries.associate { it.name to stringResource(id = it.resId) },
                onKeySelected = {
                    editNfcTagData = editNfcTagData.copy(deviceType = XiaomiDeviceType.valueOf(it))
                }
            )
            MacAddressTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                value = editNfcTagData.btMac,
                onValueChange = { editNfcTagData = editNfcTagData.copy(btMac = it) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = editNfcTagData.enableLyra,
                    onCheckedChange = { editNfcTagData = editNfcTagData.copy(enableLyra = it) }
                )
                Text(text = stringResource(id = R.string.enable_lyra_ability))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
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
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End),
            ) {
                TextButton(onClick = { onRequestClearNfc() }) {
                    Text(text = stringResource(id = R.string.clear))
                }
                IconTextButton(
                    text = stringResource(id = R.string.write_tag),
                    icon = Icons.Default.Code,
                    onClick = {
                        focusManager.clearFocus()
                        onRequestWriteNfc(editNfcTagData.copy())
                        editNfcTagData = editNfcTagData.copy(readOnly = false)
                    }
                )
            }
        }
    }
    if (readOnlyAlert) {
        AlertDialog(
            onDismissRequest = { readOnlyAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(id = R.string.dangerous_action_alert),
                    tint = Color.Red
                )
            },
            title = {
                Text(text = stringResource(id = R.string.dangerous_action_alert))
            },
            text = {
                Text(text = stringResource(id = R.string.set_nfc_read_only_desc))
            },
            confirmButton = {
                Button(
                    onClick = {
                        editNfcTagData = editNfcTagData.copy(readOnly = true)
                        readOnlyAlert = false
                    }
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { readOnlyAlert = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TilesFunctionCard(
    mirrorData: XiaomiMirrorData,
    onChanged: (XiaomiMirrorData) -> Unit,
    onRequestAddTiles: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    FunctionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        icon = Icons.Default.AppShortcut,
        iconDescription = stringResource(id = R.string.tiles_screen_mirror),
        title = stringResource(id = R.string.tiles_screen_mirror),
        description = stringResource(id = R.string.tiles_screen_mirror_desc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MirrorDataController(
                modifier = Modifier.fillMaxWidth(),
                mirrorData = mirrorData,
                onChanged = onChanged
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    IconTextButton(
                        text = stringResource(id = R.string.add_tiles),
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            focusManager.clearFocus()
                            onRequestAddTiles()
                        }
                    )
                }
                IconTextButton(
                    text = stringResource(id = R.string.save),
                    icon = Icons.Default.Save,
                    onClick = {
                        focusManager.clearFocus()
                        onSave()
                    }
                )
            }
        }
    }
}

@Composable
private fun HuaweiRedirectFunctionCard(
    redirectData: HuaweiRedirectData,
    onChanged: (HuaweiRedirectData) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = LocalAppThemeColorScheme.current

    FunctionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        icon = Icons.Default.Transform,
        iconDescription = stringResource(id = R.string.huawei_nfc_redirect),
        title = stringResource(id = R.string.huawei_nfc_redirect),
        description = stringResource(id = R.string.huawei_nfc_redirect_desc)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconTextButton(
                text = stringResource(id = R.string.open_notification_service_settings),
                icon = Icons.Default.OpenInNew,
                onClick = { context.openNotificationServiceSettings(NfcNotificationListenerService::class.java.name) }
            )
            IconTextButton(
                text = stringResource(id = R.string.open_app_settings_for_autostart),
                icon = Icons.Default.OpenInNew,
                onClick = { context.openAppSettings() }
            )
            Divider(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp), color = colorScheme.onSurfaceVariant)
            MiConnectActionSettings(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                deviceType = redirectData.deviceType,
                mirrorType = redirectData.mirrorType,
                onDeviceTypeChanged = { onChanged(redirectData.copy(deviceType = it)) },
                onMirrorTypeChanged = { onChanged(redirectData.copy(mirrorType = it)) }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = redirectData.enableLyra,
                    onCheckedChange = { onChanged(redirectData.copy(enableLyra = it)) }
                )
                Text(text = stringResource(id = R.string.enable_lyra_ability))
            }
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.save),
                icon = Icons.Default.Save,
                onClick = onSave
            )
        }
    }
}
