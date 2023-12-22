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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import tool.xfy9326.milink.nfc.data.HuaweiRedirect
import tool.xfy9326.milink.nfc.data.NdefWriteData
import tool.xfy9326.milink.nfc.data.ScreenMirror
import tool.xfy9326.milink.nfc.service.NfcNotificationListenerService
import tool.xfy9326.milink.nfc.ui.common.FunctionCard
import tool.xfy9326.milink.nfc.ui.common.IconTextButton
import tool.xfy9326.milink.nfc.ui.common.MacAddressTextField
import tool.xfy9326.milink.nfc.ui.common.MiConnectActionSettings
import tool.xfy9326.milink.nfc.ui.common.ScreenMirrorController
import tool.xfy9326.milink.nfc.ui.common.SelectorTextField
import tool.xfy9326.milink.nfc.ui.dialog.MiLinkVersionDialog
import tool.xfy9326.milink.nfc.ui.dialog.NfcReadOnlyAlertDialog
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeColorScheme
import tool.xfy9326.milink.nfc.ui.vm.ScreenMirrorViewModel
import tool.xfy9326.milink.nfc.utils.openAppSettings
import tool.xfy9326.milink.nfc.utils.openNotificationServiceSettings

const val ScreenMirrorRoute = "screen_mirror"

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenMirrorScreen(
            onRequestWriteNfc = {},
            onNavBack = {}
        )
    }
}

@Composable
fun ScreenMirrorScreen(
    viewModel: ScreenMirrorViewModel = viewModel(),
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
                title = { Text(text = stringResource(id = R.string.xiaomi_screen_mirror_nfc)) },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.nav_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openMiLinkVersionDialog(context) }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = stringResource(id = R.string.local_app_versions)
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
            TestScreenMirrorFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                screenMirror = uiState.value.testScreenMirror,
                onSendScreenMirror = { viewModel.sendScreenMirror(context, it) }
            )
            WriteNfcFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                nfcTagData = uiState.value.nfcTag,
                onRequestWriteNfc = { viewModel.requestWriteNfc(it, onRequestWriteNfc) }
            )
            TilesScreenMirrorFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                screenMirror = uiState.value.tilesScreenMirror,
                onChanged = viewModel::updateTilesScreenMirror,
                onRequestAddTiles = { viewModel.requestAddTiles(context) },
                onSave = viewModel::saveTilesScreenMirror
            )
            HuaweiRedirectFunctionCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                redirectData = uiState.value.huaweiRedirect,
                onChanged = viewModel::updateHuaweiRedirect,
                onSave = viewModel::saveHuaweiRedirect
            )
        }
    }
    EventHandler(
        snackBarHostState = snackBarHostState,
        viewModel = viewModel
    )
    uiState.value.miLinkPackageDialogData?.let {
        MiLinkVersionDialog(
            dialogData = it,
            onDismissRequest = viewModel::closeMiLinkVersionDialog
        )
    }
}

@Composable
private fun EventHandler(
    snackBarHostState: SnackbarHostState,
    viewModel: ScreenMirrorViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(snackBarHostState) {
        viewModel.instantMsg.collectLatest {
            snackBarHostState.showSnackbar(message = context.getString(it.resId))
        }
    }
}

@Composable
private fun TestScreenMirrorFunctionCard(
    modifier: Modifier,
    screenMirror: ScreenMirror,
    onSendScreenMirror: (ScreenMirror) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var editScreenMirror by rememberSaveable { mutableStateOf(screenMirror) }

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Outlined.ScreenShare,
        title = stringResource(id = R.string.test_screen_mirror),
        description = stringResource(id = R.string.test_screen_mirror_desc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenMirrorController(
                modifier = Modifier.fillMaxWidth(),
                screenMirror = editScreenMirror,
                onChanged = { editScreenMirror = it },
            )
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
                text = stringResource(id = R.string.screen_mirror),
                icon = Icons.Default.Cast,
                onClick = {
                    focusManager.clearFocus()
                    onSendScreenMirror(editScreenMirror.copy())
                }
            )
        }
    }
}

@Composable
private fun WriteNfcFunctionCard(
    modifier: Modifier,
    nfcTagData: ScreenMirrorViewModel.NFCTag,
    onRequestWriteNfc: (ScreenMirrorViewModel.NFCTag) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var readOnlyAlert by remember { mutableStateOf(false) }
    var editNfcTagData by rememberSaveable { mutableStateOf(nfcTagData) }

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.Nfc,
        title = stringResource(id = R.string.write_screen_mirror_nfc),
        description = stringResource(id = R.string.write_screen_mirror_nfc_desc)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SelectorTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .padding(bottom = 10.dp),
                label = stringResource(id = R.string.handoff_device_type),
                selectKey = editNfcTagData.deviceType.name,
                keyTextMap = ScreenMirror.DeviceType.entries.associate {
                    it.name to stringResource(
                        id = it.resId
                    )
                },
                onKeySelected = {
                    editNfcTagData =
                        editNfcTagData.copy(deviceType = ScreenMirror.DeviceType.valueOf(it))
                }
            )
            MacAddressTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                title = stringResource(id = R.string.enter_bluetooth_mac_address),
                value = editNfcTagData.bluetoothMac,
                upperCase = true,
                onValueChange = { editNfcTagData = editNfcTagData.copy(bluetoothMac = it) }
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
            IconTextButton(
                modifier = Modifier.align(Alignment.End),
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

@Composable
private fun TilesScreenMirrorFunctionCard(
    modifier: Modifier,
    screenMirror: ScreenMirror,
    onChanged: (ScreenMirror) -> Unit,
    onRequestAddTiles: () -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.AppShortcut,
        title = stringResource(id = R.string.tiles_screen_mirror),
        description = stringResource(id = R.string.tiles_screen_mirror_desc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScreenMirrorController(
                modifier = Modifier.fillMaxWidth(),
                screenMirror = screenMirror,
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
    modifier: Modifier,
    redirectData: HuaweiRedirect,
    onChanged: (HuaweiRedirect) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = LocalAppThemeColorScheme.current

    FunctionCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Default.Transform,
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
            Divider(
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                color = colorScheme.onSurfaceVariant
            )
            MiConnectActionSettings(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                deviceType = redirectData.deviceType,
                actionIntentType = redirectData.actionIntentType,
                onDeviceTypeChanged = { onChanged(redirectData.copy(deviceType = it)) },
                onMirrorTypeChanged = { onChanged(redirectData.copy(actionIntentType = it)) }
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
