package tool.xfy9326.milink.nfc.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.AppSettings
import tool.xfy9326.milink.nfc.data.settings.SettingsGroup
import tool.xfy9326.milink.nfc.data.settings.SettingsItem
import tool.xfy9326.milink.nfc.ui.common.settings.SettingsCategory
import tool.xfy9326.milink.nfc.ui.common.updateBy
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.vm.SettingsViewModel

const val SettingsRoute = "settings"

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        SettingsScreen(
            onNavBack = {}
        )
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavBack: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .displayCutoutPadding()
        ) {
            SettingsCategory(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp),
                innerPadding = innerPadding.updateBy(vertical = 10.dp, horizontal = 22.dp),
                group = SettingsGroup(
                    title = stringResource(id = R.string.settings_ndef_writing),
                    items = getSettingsItems(viewModel, uiState.value.appSettings)
                )
            )
        }
    }
}

@Composable
private fun getSettingsItems(viewModel: SettingsViewModel, appSettings: AppSettings) = listOf(
    SettingsItem.Switch(
        title = stringResource(id = R.string.settings_shrink_ndef_message),
        summary = stringResource(id = R.string.settings_shrink_ndef_message_summary),
        checked = appSettings.shrinkNdefMsg,
        onValueChanged = {
            viewModel.updateAppSettings(appSettings.copy(shrinkNdefMsg = it))
        }
    )
)