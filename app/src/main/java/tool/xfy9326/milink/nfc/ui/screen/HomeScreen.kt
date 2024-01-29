package tool.xfy9326.milink.nfc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ScreenShare
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.common.EntryCard
import tool.xfy9326.milink.nfc.ui.common.SlideAnimationNavHost
import tool.xfy9326.milink.nfc.ui.dialog.AboutDialog
import tool.xfy9326.milink.nfc.ui.dialog.MessageAlertDialog
import tool.xfy9326.milink.nfc.ui.dialog.NdefWriterDialog
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.MainViewModel
import tool.xfy9326.milink.nfc.utils.showToast

const val HomeRoute = "home"

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        HomeScreen(
            onNavToXiaomiNfcReader = {},
            onRequestWriteNdefBin = {},
            onExit = {}
        )
    }
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel(),
    onNavToXiaomiNfcReader: () -> Unit,
    onRequestWriteNdefBin: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val navController = rememberNavController()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    SlideAnimationNavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable(HomeRoute) {
            Content(
                onNavToMiTapSoundBox = {
                    navController.navigate(
                        MiTapSoundBoxRoute,
                        navOptions { launchSingleTop = true }
                    )
                },
                onNavToCirculate = {
                    navController.navigate(
                        CirculateRoute,
                        navOptions { launchSingleTop = true }
                    )
                },
                onNavToScreenMirror = {
                    navController.navigate(
                        ScreenMirrorRoute,
                        navOptions { launchSingleTop = true }
                    )
                },
                onNavToXiaomiNfcReader = onNavToXiaomiNfcReader,
                onNavToSettings = {
                    navController.navigate(
                        SettingsRoute,
                        navOptions { launchSingleTop = true }
                    )
                },
                onRequestClearNfc = viewModel::requestClearNdefWriteDialog,
                onRequestWriteNdefBin = onRequestWriteNdefBin,
                onRequestFormatXiaomiTap = viewModel::requestFormatXiaomiTapNdefWriteDialog
            )
        }
        composable(MiTapSoundBoxRoute) {
            MiTapSoundBox(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack(HomeRoute, false)
                }
            )
        }
        composable(CirculateRoute) {
            CirculateScreen(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack(HomeRoute, false)
                }
            )
        }
        composable(ScreenMirrorRoute) {
            ScreenMirrorScreen(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack(HomeRoute, false)
                }
            )
        }
        composable(SettingsRoute) {
            SettingsScreen(
                onNavBack = {
                    navController.popBackStack(HomeRoute, false)
                }
            )
        }
    }

    if (uiState.value.showNotSupportedOSDialog) {
        MessageAlertDialog(
            title = stringResource(id = R.string.not_supported_os),
            message = stringResource(id = R.string.not_supported_os_desc),
            icon = Icons.Default.Warning,
            iconTint = Color.Red,
            onConfirm = viewModel::confirmNotSupportedOS,
            showCancel = true,
            addMessagePrefixSpaces = true,
            cancelText = stringResource(id = R.string.exit),
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = onExit
        )
    }

    uiState.value.ndefWriteDialogData?.let {
        NdefWriterDialog(
            ndefWriteData = it,
            onOpenReader = viewModel::openNFCWriter,
            onCloseReader = viewModel::closeNfcWriter,
            onNfcDeviceUsing = { context.showToast(context.getString(R.string.nfc_using_conflict)) },
            onDismissRequest = viewModel::cancelNdefWriteDialog
        )
    }
}

@Composable
private fun Content(
    onNavToMiTapSoundBox: () -> Unit,
    onNavToCirculate: () -> Unit,
    onNavToScreenMirror: () -> Unit,
    onNavToXiaomiNfcReader: () -> Unit,
    onNavToSettings: () -> Unit,
    onRequestClearNfc: () -> Unit,
    onRequestWriteNdefBin: () -> Unit,
    onRequestFormatXiaomiTap: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                onNavToSettings = onNavToSettings
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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntryCard(
                icon = Icons.Default.BluetoothAudio,
                title = stringResource(id = R.string.mi_tap_sound_box_nfc),
                summary = stringResource(id = R.string.mi_tap_sound_box_nfc_summary),
                onClick = onNavToMiTapSoundBox
            )
            EntryCard(
                icon = Icons.Default.Devices,
                title = stringResource(id = R.string.circulate_nfc),
                summary = stringResource(id = R.string.circulate_nfc_summary),
                onClick = onNavToCirculate
            )
            EntryCard(
                icon = Icons.AutoMirrored.Outlined.ScreenShare,
                title = stringResource(id = R.string.xiaomi_screen_mirror_nfc),
                summary = stringResource(id = R.string.xiaomi_screen_mirror_nfc_summary),
                onClick = onNavToScreenMirror
            )
            Divider(modifier = Modifier.fillMaxWidth())
            EntryCard(
                icon = Icons.Default.DataObject,
                title = stringResource(id = R.string.nfc_read_xiaomi_ndef),
                summary = stringResource(id = R.string.nfc_read_xiaomi_ndef_summary),
                onClick = onNavToXiaomiNfcReader
            )
            EntryCard(
                icon = Icons.Default.Nfc,
                title = stringResource(id = R.string.nfc_format_xiaomi_tap),
                summary = stringResource(id = R.string.nfc_format_xiaomi_tap_summary),
                onClick = onRequestFormatXiaomiTap
            )
            EntryCard(
                icon = Icons.Outlined.FileCopy,
                title = stringResource(id = R.string.nfc_write_bin_ndef),
                summary = stringResource(id = R.string.nfc_write_bin_ndef_summary),
                onClick = onRequestWriteNdefBin
            )
            EntryCard(
                icon = Icons.Default.ClearAll,
                title = stringResource(id = R.string.nfc_clear_ndef),
                summary = stringResource(id = R.string.nfc_clear_ndef_summary),
                onClick = onRequestClearNfc
            )
            Divider(modifier = Modifier.fillMaxWidth())
            EntryCard(
                icon = Icons.Default.GroupAdd,
                title = stringResource(id = R.string.add_qq_group),
                summary = stringResource(id = R.string.add_qq_group_desc),
                onClick = {
                    uriHandler.openUri(context.getString(R.string.qq_group_url))
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavToSettings: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var openAboutDialog by remember { mutableStateOf(false) }

    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                uriHandler.openUri(context.getString(R.string.app_releases_url))
            }) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = stringResource(id = R.string.check_update)
                )
            }
            IconButton(onClick = onNavToSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
            IconButton(onClick = { openAboutDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(id = R.string.about)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )

    if (openAboutDialog) {
        AboutDialog {
            openAboutDialog = false
        }
    }
}
