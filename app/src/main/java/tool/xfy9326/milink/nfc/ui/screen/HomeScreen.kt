package tool.xfy9326.milink.nfc.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.common.EntryCard
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
        HomeScreen {}
    }
}

private const val NAV_ANIMATION_DURATION = 500

@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel(),
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val navController = rememberNavController()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = HomeRoute,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIMATION_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAV_ANIMATION_DURATION),
                targetOffset = { it / 4 }
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIMATION_DURATION),
                initialOffset = { it / 4 }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAV_ANIMATION_DURATION)
            )
        }
    ) {
        composable(HomeRoute) {
            Content(
                onNavToMiPlay = {
                    navController.navigate(MiPlayRoute)
                },
                onNavToMiCirculate = {
                    navController.navigate(MiCirculateRoute)
                },
                onNavToScreenMirror = {
                    navController.navigate(ScreenMirrorRoute)
                },
                onRequestClearNfc = viewModel::requestClearNdefWriteDialog
            )
        }
        composable(MiPlayRoute) {
            MiPlayScreen(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(MiCirculateRoute) {
            MiCirculateScreen(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(ScreenMirrorRoute) {
            ScreenMirrorScreen(
                onRequestWriteNfc = viewModel::requestNdefWriteDialog,
                onNavBack = {
                    navController.popBackStack()
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
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            onDismissRequest = onExit
        )
    }

    uiState.value.ndefWriteDialogData?.let {
        NdefWriterDialog(
            ndefData = it,
            onOpenReader = viewModel::openNFCWriter,
            onCloseReader = viewModel::closeNfcWriter,
            onNfcDeviceUsing = { context.showToast(context.getString(R.string.nfc_using_conflict)) },
            onDismissRequest = viewModel::cancelNdefWriteDialog
        )
    }
}

@Composable
private fun Content(
    onNavToMiPlay: () -> Unit,
    onNavToMiCirculate: () -> Unit,
    onNavToScreenMirror: () -> Unit,
    onRequestClearNfc: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() },
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
                icon = Icons.Default.TapAndPlay,
                title = stringResource(id = R.string.mi_play_nfc),
                summary = stringResource(id = R.string.mi_play_nfc_summary),
                onClick = onNavToMiPlay
            )
            EntryCard(
                icon = Icons.Default.Devices,
                title = stringResource(id = R.string.mi_circulate_nfc),
                summary = stringResource(id = R.string.mi_circulate_nfc_summary),
                onClick = onNavToMiCirculate
            )
            EntryCard(
                icon = Icons.Outlined.ScreenShare,
                title = stringResource(id = R.string.xiaomi_screen_mirror_nfc),
                summary = stringResource(id = R.string.xiaomi_screen_mirror_nfc_summary),
                onClick = onNavToScreenMirror
            )
            Divider(modifier = Modifier.fillMaxWidth())
            EntryCard(
                icon = Icons.Default.Nfc,
                title = stringResource(id = R.string.nfc_clear_ndef),
                summary = stringResource(id = R.string.nfc_clear_ndef_summary),
                onClick = onRequestClearNfc
            )
        }
    }
}

@Composable
private fun TopBar() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var openAboutDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                uriHandler.openUri(context.getString(R.string.app_releases_url))
            }) {
                Icon(imageVector = Icons.Default.Update, contentDescription = stringResource(id = R.string.check_update))
            }
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
